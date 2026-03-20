package com.aotemiao.artemis.symphony.api;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** 根路径极简说明页，指向 JSON API。 */
@RestController
public class SymphonyRootController {

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public String index() {
        return """
                <!DOCTYPE html>
                <html><head><meta charset="utf-8"><title>Artemis Symphony</title></head>
                <body>
                <h1>Artemis Symphony</h1>
                <ul>
                  <li><a href="/api/v1/state">GET /api/v1/state</a> — 编排器状态快照</li>
                  <li>POST /api/v1/refresh — 请求立即执行一轮 tick</li>
                  <li>GET /api/v1/issues/{identifier} — 议题工作区 / 重试状态</li>
                </ul>
                </body></html>
                """;
    }
}
