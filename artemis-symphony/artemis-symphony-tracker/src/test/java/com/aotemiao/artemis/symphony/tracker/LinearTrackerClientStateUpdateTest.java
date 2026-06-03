package com.aotemiao.artemis.symphony.tracker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class LinearTrackerClientStateUpdateTest {

    @Test
    void updateIssueState_resolvesStateIdThenSendsIssueUpdateMutation() {
        List<String> requestBodies = new ArrayList<>();
        LinearTrackerClient client =
                new LinearTrackerClient("http://linear.test/graphql", "k", null, (endpoint, apiKey, body) -> {
                    requestBodies.add(body);
                    if (body.contains("ResolveIssueState")) {
                        return new LinearTrackerClient.GraphqlHttpResponse(200, """
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
                        """);
                    }
                    return new LinearTrackerClient.GraphqlHttpResponse(200, """
                        {
                          "data": {
                            "issueUpdate": {
                              "success": true
                            }
                          }
                        }
                        """);
                });
        TrackerResult<Void> result = client.updateIssueState("issue-1", "In Progress");

        assertTrue(result.isSuccess());
        assertEquals(2, requestBodies.size());
        assertTrue(requestBodies.get(0).contains("ResolveIssueState"));
        assertTrue(requestBodies.get(0).contains("\"issueId\":\"issue-1\""));
        assertTrue(requestBodies.get(0).contains("\"stateName\":\"In Progress\""));
        assertTrue(requestBodies.get(1).contains("issueUpdate"));
        assertTrue(requestBodies.get(1).contains("\"stateId\":\"state-1\""));
    }
}
