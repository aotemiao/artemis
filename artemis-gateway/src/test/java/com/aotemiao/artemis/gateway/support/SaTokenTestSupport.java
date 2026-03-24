package com.aotemiao.artemis.gateway.support;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.context.SaTokenContext;
import cn.dev33.satoken.context.model.SaRequest;
import cn.dev33.satoken.context.model.SaResponse;
import cn.dev33.satoken.context.model.SaStorage;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 为 gateway 单元测试提供最小 Sa-Token 上下文。 */
public final class SaTokenTestSupport {

    private SaTokenTestSupport() {}

    public static SaTokenContext installContext() {
        SaTokenContext previousSaTokenContext = SaManager.getSaTokenContext();
        SaManager.setSaTokenContext(new TestSaTokenContext());
        return previousSaTokenContext;
    }

    private static final class TestSaTokenContext implements SaTokenContext {

        private final SaRequest request = new TestSaRequest();
        private final SaResponse response = new TestSaResponse();
        private final SaStorage storage = new TestSaStorage();

        @Override
        public SaRequest getRequest() {
            return request;
        }

        @Override
        public SaResponse getResponse() {
            return response;
        }

        @Override
        public SaStorage getStorage() {
            return storage;
        }

        @Override
        public boolean matchPath(String pattern, String path) {
            return pattern != null && pattern.equals(path);
        }

        @Override
        public boolean isValid() {
            return true;
        }
    }

    private static final class TestSaStorage implements SaStorage {

        private final Map<String, Object> data = new HashMap<>();

        @Override
        public Object getSource() {
            return data;
        }

        @Override
        public Object get(String key) {
            return data.get(key);
        }

        @Override
        public SaStorage set(String key, Object value) {
            data.put(key, value);
            return this;
        }

        @Override
        public SaStorage delete(String key) {
            data.remove(key);
            return this;
        }
    }

    private static final class TestSaRequest implements SaRequest {

        @Override
        public Object getSource() {
            return this;
        }

        @Override
        public String getParam(String name) {
            return null;
        }

        @Override
        public List<String> getParamNames() {
            return Collections.emptyList();
        }

        @Override
        public Map<String, String> getParamMap() {
            return Collections.emptyMap();
        }

        @Override
        public String getHeader(String name) {
            return null;
        }

        @Override
        public String getCookieValue(String name) {
            return null;
        }

        @Override
        public String getCookieFirstValue(String name) {
            return null;
        }

        @Override
        public String getCookieLastValue(String name) {
            return null;
        }

        @Override
        public String getRequestPath() {
            return "/";
        }

        @Override
        public String getUrl() {
            return "/";
        }

        @Override
        public String getMethod() {
            return "GET";
        }

        @Override
        public Object forward(String path) {
            return null;
        }
    }

    private static final class TestSaResponse implements SaResponse {

        @Override
        public Object getSource() {
            return this;
        }

        @Override
        public SaResponse setStatus(int sc) {
            return this;
        }

        @Override
        public SaResponse setHeader(String name, String value) {
            return this;
        }

        @Override
        public SaResponse addHeader(String name, String value) {
            return this;
        }

        @Override
        public Object redirect(String url) {
            return null;
        }
    }
}
