package com.aotemiao.artemis.symphony.tracker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.net.httpserver.HttpServer;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class LinearTrackerClientStateUpdateTest {

    @Test
    void updateIssueState_resolvesStateIdThenSendsIssueUpdateMutation() throws Exception {
        List<String> requestBodies = new ArrayList<>();
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/graphql", exchange -> {
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            requestBodies.add(requestBody);
            byte[] response;
            if (requestBody.contains("ResolveIssueState")) {
                response = """
                        {
                          "data": {
                            "issue": {
                              "team": {
                                "states": {
                                  "nodes": [
                                    { "id": "state-1" }
                                  ]
                                }
                              }
                            }
                          }
                        }
                        """.getBytes(StandardCharsets.UTF_8);
            } else {
                response = """
                        {
                          "data": {
                            "issueUpdate": {
                              "success": true
                            }
                          }
                        }
                        """.getBytes(StandardCharsets.UTF_8);
            }
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        });
        server.start();
        try {
            LinearTrackerClient client = new LinearTrackerClient(
                    "http://127.0.0.1:" + server.getAddress().getPort() + "/graphql", "k");
            TrackerResult<Void> result = client.updateIssueState("issue-1", "In Progress");

            assertTrue(result.isSuccess());
            assertEquals(2, requestBodies.size());
            assertTrue(requestBodies.get(0).contains("ResolveIssueState"));
            assertTrue(requestBodies.get(0).contains("\"issueId\":\"issue-1\""));
            assertTrue(requestBodies.get(0).contains("\"stateName\":\"In Progress\""));
            assertTrue(requestBodies.get(1).contains("issueUpdate"));
            assertTrue(requestBodies.get(1).contains("\"stateId\":\"state-1\""));
        } finally {
            server.stop(0);
        }
    }
}
