package com.aotemiao.artemis.symphony;

import com.aotemiao.artemis.symphony.config.DispatchPreflight;
import com.aotemiao.artemis.symphony.config.WorkflowLoadResult;
import com.aotemiao.artemis.symphony.config.WorkflowLoader;
import com.aotemiao.artemis.symphony.core.validation.DispatchValidation;
import com.aotemiao.artemis.symphony.orchestrator.AgentRunner;
import com.aotemiao.artemis.symphony.orchestrator.Orchestrator;
import com.aotemiao.artemis.symphony.orchestrator.SymphonyRuntimeHolder;
import com.aotemiao.artemis.symphony.workspace.WorkspaceManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;

/** Symphony Spring 装配：运行时快照、编排器与各 Bean 的依赖关系。 */
@Configuration
public class SymphonyBootstrap {

    private static final Logger log = LoggerFactory.getLogger(SymphonyBootstrap.class);

    @Bean
    public SymphonyRuntimeHolder symphonyRuntimeHolder(
            @Value("${symphony.workflow-path:./WORKFLOW.md}") String workflowPath) {
        Path path = Path.of(workflowPath).toAbsolutePath().normalize();
        WorkflowLoadResult result = WorkflowLoader.load(path);
        if (result instanceof WorkflowLoadResult.Error err) {
            throw new IllegalStateException("工作流加载失败: " + err.code() + " - " + err.message());
        }
        var definition = ((WorkflowLoadResult.Success) result).definition();
        return new SymphonyRuntimeHolder(path, SymphonyRuntimeHolder.buildSnapshot(definition));
    }

    @Bean
    public WorkspaceManager workspaceManager(SymphonyRuntimeHolder holder) {
        return new WorkspaceManager(() -> holder.get().config());
    }

    @Bean
    public AgentRunner agentRunner(SymphonyRuntimeHolder holder, WorkspaceManager workspaceManager) {
        return new AgentRunner(() -> holder.get().config(), workspaceManager, () -> holder.get().trackerClient());
    }

    @Bean
    public Orchestrator orchestrator(
            SymphonyRuntimeHolder holder, WorkspaceManager workspaceManager, AgentRunner agentRunner) {
        return new Orchestrator(holder, workspaceManager, agentRunner);
    }

    @Bean
    public ApplicationRunner orchestratorStarter(Orchestrator orchestrator, SymphonyRuntimeHolder holder) {
        return args -> {
            DispatchValidation validation = DispatchPreflight.validate(holder.get().config());
            if (!validation.ok()) {
                log.error("调度预检未通过: {}", validation.errors());
                throw new IllegalStateException("调度预检未通过: " + validation.errors());
            }
            orchestrator.start();
            log.info("Symphony 编排器已启动 workflow_path={}", holder.getWorkflowPath());
        };
    }
}
