package com.aotemiao.artemis.symphony.watch;

import com.aotemiao.artemis.symphony.orchestrator.Orchestrator;
import com.aotemiao.artemis.symphony.orchestrator.SymphonyRuntimeHolder;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

/**
 * 监听 WORKFLOW.md 所在目录的变更；防抖后重载，成功则触发编排器立即 tick。
 */
@Component
public class WorkflowReloadWatcher implements SmartLifecycle {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowReloadWatcher.class);

    private final SymphonyRuntimeHolder holder;
    private final Orchestrator orchestrator;
    private final boolean enabled;
    private final long debounceMs;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private WatchService watchService;
    private Thread watchThread;
    private final ScheduledExecutorService debouncer = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "symphony-workflow-reload-debounce");
        t.setDaemon(true);
        return t;
    });
    private final Object debounceLock = new Object();
    private volatile ScheduledFuture<?> pendingReload;

    public WorkflowReloadWatcher(
            SymphonyRuntimeHolder holder,
            Orchestrator orchestrator,
            @Value("${symphony.workflow-watch.enabled:true}") boolean enabled,
            @Value("${symphony.workflow-watch.debounce-ms:400}") long debounceMs) {
        this.holder = holder;
        this.orchestrator = orchestrator;
        this.enabled = enabled;
        this.debounceMs = debounceMs <= 0 ? 400 : debounceMs;
    }

    @Override
    public void start() {
        if (!enabled || !running.compareAndSet(false, true)) {
            return;
        }
        Path workflow = holder.getWorkflowPath();
        Path dir = workflow.getParent();
        if (dir == null) {
            LOGGER.warn("workflow path has no parent; skipping file watch path={}", workflow);
            running.set(false);
            return;
        }
        try {
            watchService = FileSystems.getDefault().newWatchService();
            dir.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE);
        } catch (Exception e) {
            LOGGER.warn("注册工作流目录监听失败 dir={} reason={}", dir, e.toString());
            running.set(false);
            return;
        }

        Path fileName = workflow.getFileName();
        watchThread = new Thread(() -> watchLoop(dir, fileName), "symphony-workflow-watch");
        watchThread.setDaemon(true);
        watchThread.start();
        LOGGER.info("工作流文件监听已启动 dir={} file={}", dir, fileName);
    }

    private void watchLoop(Path dir, Path workflowFileName) {
        while (running.get()) {
            WatchKey key;
            try {
                key = watchService.take();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                LOGGER.warn("WatchService 异常: {}", e.toString());
                break;
            }
            try {
                for (WatchEvent<?> ev : key.pollEvents()) {
                    if (!running.get()) {
                        break;
                    }
                    if (ev.kind() == StandardWatchEventKinds.OVERFLOW) {
                        scheduleReloadDebounced();
                        continue;
                    }
                    @SuppressWarnings("unchecked")
                    WatchEvent<Path> pathEv = (WatchEvent<Path>) ev;
                    Path name = pathEv.context();
                    if (name != null && workflowFileName.equals(name)) {
                        scheduleReloadDebounced();
                    }
                }
            } finally {
                if (!key.reset()) {
                    LOGGER.debug("监听键已失效 dir={}", dir);
                    break;
                }
            }
        }
    }

    private void scheduleReloadDebounced() {
        synchronized (debounceLock) {
            if (pendingReload != null) {
                pendingReload.cancel(false);
            }
            pendingReload = debouncer.schedule(this::reloadWorkflow, debounceMs, TimeUnit.MILLISECONDS);
        }
    }

    private void reloadWorkflow() {
        try {
            boolean ok = holder.tryReloadFromDisk();
            if (ok) {
                LOGGER.info("action=workflow_reload outcome=success path={}", holder.getWorkflowPath());
                orchestrator.requestImmediateTick();
            }
        } catch (Exception e) {
            LOGGER.warn("action=workflow_reload outcome=error reason={}", e.toString(), e);
        }
    }

    @Override
    public void stop() {
        stopInternal();
    }

    @Override
    public void stop(Runnable callback) {
        try {
            stopInternal();
        } finally {
            callback.run();
        }
    }

    private void stopInternal() {
        running.set(false);
        synchronized (debounceLock) {
            if (pendingReload != null) {
                pendingReload.cancel(false);
            }
        }
        debouncer.shutdown();
        try {
            if (!debouncer.awaitTermination(2, TimeUnit.SECONDS)) {
                debouncer.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            debouncer.shutdownNow();
        }
        if (watchThread != null) {
            watchThread.interrupt();
        }
        if (watchService != null) {
            try {
                watchService.close();
            } catch (Exception e) {
                LOGGER.debug("关闭工作流监听器失败", e);
            }
        }
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }
}
