package com.aotemiao.artemis.symphony.tracker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class LinearTrackerClientCommentTest {

    @Test
    void createIssueComment_sendsCommentCreateMutation() {
        AtomicReference<String> requestBody = new AtomicReference<>();
        LinearTrackerClient client =
                new LinearTrackerClient("http://linear.test/graphql", "k", null, (endpoint, apiKey, body) -> {
                    requestBody.set(body);
                    return new LinearTrackerClient.GraphqlHttpResponse(200, """
                    {
                      "data": {
                        "commentCreate": {
                          "success": true,
                          "comment": { "id": "comment-1" }
                        }
                      }
                    }
                    """);
                });
        TrackerResult<String> result = client.createIssueComment("issue-1", "hello linear");

        assertTrue(result.isSuccess());
        assertEquals("comment-1", result.value());
        assertTrue(requestBody.get().contains("commentCreate"));
        assertTrue(requestBody.get().contains("\"issueId\":\"issue-1\""));
        assertTrue(requestBody.get().contains("\"body\":\"hello linear\""));
    }
}
