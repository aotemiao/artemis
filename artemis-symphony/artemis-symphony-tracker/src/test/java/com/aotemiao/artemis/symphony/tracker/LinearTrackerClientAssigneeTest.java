package com.aotemiao.artemis.symphony.tracker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.aotemiao.artemis.symphony.core.model.Issue;
import com.sun.net.httpserver.HttpServer;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

class LinearTrackerClientAssigneeTest {

    @Test
    void fetchCandidateIssues_marksIssuesOutsideConfiguredAssigneeAsUnrouted() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/graphql", exchange -> {
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            byte[] response;
            if (requestBody.contains("SymphonyLinearViewer")) {
                response = """
                        { "data": { "viewer": { "id": "viewer-1" } } }
                        """.getBytes(StandardCharsets.UTF_8);
            } else {
                response = """
                        {
                          "data": {
                            "issues": {
                              "nodes": [
                                {
                                  "id": "issue-1",
                                  "identifier": "ART-1",
                                  "title": "Assigned to me",
                                  "description": "A",
                                  "priority": 1,
                                  "state": { "name": "Todo" },
                                  "branchName": null,
                                  "url": "https://linear.app/example/ART-1",
                                  "assignee": { "id": "viewer-1" },
                                  "labels": { "nodes": [] },
                                  "inverseRelations": { "nodes": [] },
                                  "createdAt": "2026-03-25T01:00:00Z",
                                  "updatedAt": "2026-03-25T01:10:00Z"
                                },
                                {
                                  "id": "issue-2",
                                  "identifier": "ART-2",
                                  "title": "Assigned elsewhere",
                                  "description": "B",
                                  "priority": 1,
                                  "state": { "name": "Todo" },
                                  "branchName": null,
                                  "url": "https://linear.app/example/ART-2",
                                  "assignee": { "id": "viewer-2" },
                                  "labels": { "nodes": [] },
                                  "inverseRelations": { "nodes": [] },
                                  "createdAt": "2026-03-25T01:00:00Z",
                                  "updatedAt": "2026-03-25T01:10:00Z"
                                }
                              ],
                              "pageInfo": {
                                "hasNextPage": false,
                                "endCursor": null
                              }
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
                    "http://127.0.0.1:" + server.getAddress().getPort() + "/graphql", "k", "me");

            TrackerResult<List<Issue>> result = client.fetchCandidateIssues("artemis", List.of("Todo"));

            assertTrue(result.isSuccess());
            assertEquals(2, result.value().size());
            assertTrue(result.value().get(0).assignedToWorker());
            assertEquals("viewer-1", result.value().get(0).assigneeId());
            assertFalse(result.value().get(1).assignedToWorker());
            assertEquals("viewer-2", result.value().get(1).assigneeId());
        } finally {
            server.stop(0);
        }
    }
}
