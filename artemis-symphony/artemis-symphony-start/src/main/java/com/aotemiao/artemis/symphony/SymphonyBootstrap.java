package com.aotemiao.artemis.symphony;

import com.aotemiao.artemis.symphony.config.DispatchPreflight;
import com.aotemiao.artemis.symphony.config.ServiceConfig;
import com.aotemiao.artemis.symphony.config.WorkflowLoadResult;
import com.aotemiao.artemis.symphony.config.WorkflowLoader;
import com.aotemiao.artemis.symphony.core.validation.DispatchValidation;
import com.aotemiao.artemis.symphony.orchestrator.AgentRunner;
import com.aotemiao.artemis.symphony.orchestrator.Orchestrator;
import com.aotemiao.artemis.symphony.tracker.LinearTrackerClient;
import com.aotemiao.artemis.symphony.workspace.WorkspaceManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;

@Configuration
public class SymphonyBootstrap {

    private static final Logger log = LoggerFactory.getLogger(SymphonyBootstrap.class);

    @Bean
    public ServiceConfig serviceConfig(@Value("${symphony.workflow-path:./WORKFLOW.md}") String workflowPath) {
        Path path = Path.of(workflowPath).toAbsolutePath().normalize();
        WorkflowLoadResult result = WorkflowLoader.load(path);
        if (result instanceof WorkflowLoadResult.Error err) {
            throw new IllegalStateException("Workflow load failed: " + err.code() + " - " + err.message());
        }
        return new ServiceConfig(((WorkflowLoadResult.Success) result).definition());
    }

    @Bean
    public LinearTrackerClient linearTrackerClient(ServiceConfig config) {
        return new LinearTrackerClient(config.getTrackerEndpoint(), config.getTrackerApiKey());
    }

    @Bean
    public WorkspaceManager workspaceManager(ServiceConfig config) {
        return new WorkspaceManager(config);
    }

    @Bean
    public AgentRunner agentRunner(ServiceConfig config, WorkspaceManager workspaceManager, LinearTrackerClient tracker) {
        return new AgentRunner(config, workspaceManager, tracker);
    }

    @Bean
    public Orchestrator orchestrator(ServiceConfig config, LinearTrackerClient tracker, WorkspaceManager workspaceManager, AgentRunner agentRunner) {
        return new Orchestrator(config, tracker, workspaceManager, agentRunner);
    }

    @Bean
    public ApplicationRunner orchestratorStarter(Orchestrator orchestrator, ServiceConfig config) {
        return args -> {
            DispatchValidation validation = DispatchPreflight.validate(config);
            if (!validation.ok()) {
                log.error("Dispatch validation failed: {}", validation.errors());
                throw new IllegalStateException("Dispatch validation failed: " + validation.errors());
            }
            orchestrator.start();
            log.info("Symphony orchestrator started");
        };
    }
}
