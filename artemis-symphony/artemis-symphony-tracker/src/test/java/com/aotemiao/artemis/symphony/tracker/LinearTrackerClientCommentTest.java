package com.aotemiao.artemis.symphony.tracker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class LinearTrackerClientCommentTest {

    @Test
    void createIssueComment_sendsCommentCreateMutation() throws Exception {
        AtomicReference<String> requestBody = new AtomicReference<>();
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/graphql", exchange -> {
            requestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] response = """
                    {
                      "data": {
                        "commentCreate": {
                          "success": true,
                          "comment": { "id": "comment-1" }
                        }
                      }
                    }
                    """.getBytes(StandardCharsets.UTF_8);
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
            TrackerResult<String> result = client.createIssueComment("issue-1", "hello linear");

            assertTrue(result.isSuccess());
            assertEquals("comment-1", result.value());
            assertTrue(requestBody.get().contains("commentCreate"));
            assertTrue(requestBody.get().contains("\"issueId\":\"issue-1\""));
            assertTrue(requestBody.get().contains("\"body\":\"hello linear\""));
        } finally {
            stop(server);
        }
    }

    private static void stop(HttpServer server) throws IOException {
        server.stop(0);
    }
}
