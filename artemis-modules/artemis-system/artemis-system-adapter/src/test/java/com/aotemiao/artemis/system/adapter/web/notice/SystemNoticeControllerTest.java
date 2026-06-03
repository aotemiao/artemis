package com.aotemiao.artemis.system.adapter.web.notice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.app.command.notice.CreateSystemNoticeCmdExe;
import com.aotemiao.artemis.system.app.command.notice.DeleteSystemNoticeCmdExe;
import com.aotemiao.artemis.system.app.command.notice.UpdateSystemNoticeCmdExe;
import com.aotemiao.artemis.system.app.query.notice.FindSystemNoticeByIdQryExe;
import com.aotemiao.artemis.system.app.query.notice.SystemNoticePageQryExe;
import com.aotemiao.artemis.system.domain.model.notice.SystemNotice;
import jakarta.servlet.ServletException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class SystemNoticeControllerTest {

    private MockMvc mockMvc;

    private CreateSystemNoticeCmdExe createSystemNoticeCmdExe;
    private UpdateSystemNoticeCmdExe updateSystemNoticeCmdExe;
    private DeleteSystemNoticeCmdExe deleteSystemNoticeCmdExe;
    private FindSystemNoticeByIdQryExe findSystemNoticeByIdQryExe;
    private SystemNoticePageQryExe systemNoticePageQryExe;

    @BeforeEach
    void setUp() {
        createSystemNoticeCmdExe = mock(CreateSystemNoticeCmdExe.class);
        updateSystemNoticeCmdExe = mock(UpdateSystemNoticeCmdExe.class);
        deleteSystemNoticeCmdExe = mock(DeleteSystemNoticeCmdExe.class);
        findSystemNoticeByIdQryExe = mock(FindSystemNoticeByIdQryExe.class);
        systemNoticePageQryExe = mock(SystemNoticePageQryExe.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new SystemNoticeController(
                        createSystemNoticeCmdExe,
                        updateSystemNoticeCmdExe,
                        deleteSystemNoticeCmdExe,
                        findSystemNoticeByIdQryExe,
                        systemNoticePageQryExe))
                .build();
    }

    @Test
    void create_returnsCreatedNotice() throws Exception {
        when(createSystemNoticeCmdExe.execute(any())).thenReturn(sampleNotice());

        mockMvc.perform(post(SystemNoticeController.BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "noticeTitle": "维护通知",
                                  "noticeType": "NOTICE",
                                  "noticeContent": "今晚维护",
                                  "status": "NORMAL",
                                  "remarks": "提前通知"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.noticeTitle").value("维护通知"))
                .andExpect(jsonPath("$.data.noticeType").value("NOTICE"));
    }

    @Test
    void update_returnsUpdatedNotice() throws Exception {
        SystemNotice updated = sampleNotice();
        updated.setStatus("CLOSED");
        when(updateSystemNoticeCmdExe.execute(any())).thenReturn(updated);

        mockMvc.perform(put(SystemNoticeController.BASE_PATH + "/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "noticeTitle": "维护通知",
                                  "noticeType": "NOTICE",
                                  "noticeContent": "今晚维护",
                                  "status": "CLOSED",
                                  "remarks": "提前通知"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CLOSED"));
    }

    @Test
    void getById_returnsNotice() throws Exception {
        when(findSystemNoticeByIdQryExe.execute(any())).thenReturn(Optional.of(sampleNotice()));

        mockMvc.perform(get(SystemNoticeController.BASE_PATH + "/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.noticeTitle").value("维护通知"));
    }

    @Test
    void getById_whenMissing_throwsBizException() {
        when(findSystemNoticeByIdQryExe.execute(any())).thenReturn(Optional.empty());

        ServletException exception = assertThrows(
                ServletException.class, () -> mockMvc.perform(get(SystemNoticeController.BASE_PATH + "/{id}", 99L)));

        assertThat(exception.getCause()).isInstanceOf(BizException.class);
    }

    @Test
    void page_returnsNoticePage() throws Exception {
        when(systemNoticePageQryExe.execute(any())).thenReturn(PageResult.of(1, List.of(sampleNotice()), 1));

        mockMvc.perform(get(SystemNoticeController.BASE_PATH).param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.content[0].noticeTitle").value("维护通知"));
    }

    @Test
    void delete_returnsOk() throws Exception {
        mockMvc.perform(delete(SystemNoticeController.BASE_PATH + "/{id}", 1L)).andExpect(status().isOk());
    }

    private static SystemNotice sampleNotice() {
        SystemNotice systemNotice = new SystemNotice();
        systemNotice.setId(1L);
        systemNotice.setNoticeTitle("维护通知");
        systemNotice.setNoticeType("NOTICE");
        systemNotice.setNoticeContent("今晚维护");
        systemNotice.setStatus("NORMAL");
        systemNotice.setRemarks("提前通知");
        return systemNotice;
    }
}
