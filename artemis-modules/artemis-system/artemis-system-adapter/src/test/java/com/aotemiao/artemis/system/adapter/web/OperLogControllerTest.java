package com.aotemiao.artemis.system.adapter.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.system.app.command.audit.ClearOperLogCmdExe;
import com.aotemiao.artemis.system.app.command.audit.DeleteOperLogCmdExe;
import com.aotemiao.artemis.system.app.query.audit.FindOperLogByIdQryExe;
import com.aotemiao.artemis.system.app.query.audit.OperLogPageQryExe;
import com.aotemiao.artemis.system.domain.model.audit.OperLog;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class OperLogControllerTest {

    private MockMvc mockMvc;

    private OperLogPageQryExe operLogPageQryExe;
    private FindOperLogByIdQryExe findOperLogByIdQryExe;

    @BeforeEach
    void setUp() {
        operLogPageQryExe = mock(OperLogPageQryExe.class);
        findOperLogByIdQryExe = mock(FindOperLogByIdQryExe.class);
        DeleteOperLogCmdExe deleteOperLogCmdExe = mock(DeleteOperLogCmdExe.class);
        ClearOperLogCmdExe clearOperLogCmdExe = mock(ClearOperLogCmdExe.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new OperLogController(
                        operLogPageQryExe, findOperLogByIdQryExe, deleteOperLogCmdExe, clearOperLogCmdExe))
                .build();
    }

    @Test
    void page_returnsOperLogPage() throws Exception {
        when(operLogPageQryExe.execute(any())).thenReturn(PageResult.of(1, List.of(sampleOperLog()), 1));

        mockMvc.perform(get(OperLogController.BASE_PATH).param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.content[0].title").value("用户管理"));
    }

    @Test
    void getById_returnsOperLog() throws Exception {
        when(findOperLogByIdQryExe.execute(any())).thenReturn(Optional.of(sampleOperLog()));

        mockMvc.perform(get(OperLogController.BASE_PATH + "/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SUCCESS"));
    }

    @Test
    void delete_returnsOk() throws Exception {
        mockMvc.perform(delete(OperLogController.BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ids": [1, 2]
                                }
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void clear_returnsOk() throws Exception {
        mockMvc.perform(post(OperLogController.BASE_PATH + "/clear")).andExpect(status().isOk());
    }

    private static OperLog sampleOperLog() {
        OperLog operLog = new OperLog();
        operLog.setId(1L);
        operLog.setTitle("用户管理");
        operLog.setBusinessType("INSERT");
        operLog.setMethod("SystemUserController.create(..)");
        operLog.setRequestMethod("POST");
        operLog.setOperatorType("MANAGE");
        operLog.setOperName("admin");
        operLog.setDeptName("研发部");
        operLog.setOperUrl("/api/users");
        operLog.setOperIp("127.0.0.1");
        operLog.setOperLocation("未知");
        operLog.setOperParam("{}");
        operLog.setJsonResult("{\"code\":0}");
        operLog.setStatus("SUCCESS");
        operLog.setCostTime(12L);
        operLog.setOperTime(LocalDateTime.now());
        return operLog;
    }
}
