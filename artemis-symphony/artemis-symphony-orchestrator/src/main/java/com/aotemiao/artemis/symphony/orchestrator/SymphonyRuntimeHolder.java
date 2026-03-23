package com.aotemiao.artemis.symphony.orchestrator;

import com.aotemiao.artemis.symphony.config.ServiceConfig;
import com.aotemiao.artemis.symphony.config.WorkflowLoadResult;
import com.aotemiao.artemis.symphony.config.WorkflowLoader;
import com.aotemiao.artemis.symphony.core.model.WorkflowDefinition;
import com.aotemiao.artemis.symphony.tracker.LinearTrackerClient;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 线程安全地持有当前 {@link SymphonyRuntimeSnapshot}，并支持从磁盘热重载。
 */
public class SymphonyRuntimeHolder {

    private static final Logger LOGGER = LoggerFactory.getLogger(SymphonyRuntimeHolder.class);

    private final Path workflowPath;
    private final AtomicReference<SymphonyRuntimeSnapshot> ref;

    public SymphonyRuntimeHolder(Path workflowPath, SymphonyRuntimeSnapshot initial) {
        this.workflowPath = workflowPath;
        this.ref = new AtomicReference<>(initial);
    }

    public Path getWorkflowPath() {
        return workflowPath;
    }

    public SymphonyRuntimeSnapshot get() {
        SymphonyRuntimeSnapshot s = ref.get();
        if (s == null) {
            throw new IllegalStateException("Symphony 运行时未初始化");
        }
        return s;
    }

    /** 根据已加载的 {@link WorkflowDefinition} 构建快照（用于启动阶段）。 */
    public static SymphonyRuntimeSnapshot buildSnapshot(WorkflowDefinition definition) {
        ServiceConfig config = new ServiceConfig(definition);
        LinearTrackerClient tracker = new LinearTrackerClient(config.getTrackerEndpoint(), config.getTrackerApiKey());
        return new SymphonyRuntimeSnapshot(definition, config, tracker);
    }

    /**
     * 从磁盘重载工作流：成功则替换快照；失败则保留上一份可用快照。
     *
     * @return 重载且解析成功时返回 true（调用方仍可另行记录调度预检告警）
     */
    public boolean tryReloadFromDisk() {
        WorkflowLoadResult result = WorkflowLoader.load(workflowPath);
        if (result instanceof WorkflowLoadResult.Error err) {
            LOGGER.warn(
                    "action=workflow_reload outcome=failed code={} message={} path={}",
                    err.code(),
                    err.message(),
                    workflowPath);
            return false;
        }
        WorkflowDefinition def = ((WorkflowLoadResult.Success) result).definition();
        SymphonyRuntimeSnapshot next = buildSnapshot(def);
        ref.set(next);
        return true;
    }
}
