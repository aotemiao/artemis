package com.aotemiao.artemis.resource.adapter.web.message;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.resource.app.command.message.MarkSystemMessageReadCmdExe;
import com.aotemiao.artemis.resource.app.command.message.PublishSystemMessageCmdExe;
import com.aotemiao.artemis.resource.app.query.message.SystemMessageInboxQryExe;
import com.aotemiao.artemis.resource.domain.model.message.SystemMessage;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class SystemMessageControllerTest {

    private MockMvc mockMvc;
    private PublishSystemMessageCmdExe publishSystemMessageCmdExe;
    private MarkSystemMessageReadCmdExe markSystemMessageReadCmdExe;
    private SystemMessageInboxQryExe systemMessageInboxQryExe;

    @BeforeEach
    void setUp() {
        publishSystemMessageCmdExe = mock(PublishSystemMessageCmdExe.class);
        markSystemMessageReadCmdExe = mock(MarkSystemMessageReadCmdExe.class);
        systemMessageInboxQryExe = mock(SystemMessageInboxQryExe.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new SystemMessageController(
                        publishSystemMessageCmdExe, markSystemMessageReadCmdExe, systemMessageInboxQryExe))
                .build();
    }

    @Test
    void publishToUser_returnsMessage() throws Exception {
        when(publishSystemMessageCmdExe.execute(any())).thenReturn(List.of(sampleMessage()));

        mockMvc.perform(post(SystemMessageController.BASE_PATH + "/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Hi"));
    }

    @Test
    void publishToAll_returnsMessages() throws Exception {
        when(publishSystemMessageCmdExe.execute(any())).thenReturn(List.of(sampleMessage()));

        mockMvc.perform(post(SystemMessageController.BASE_PATH + "/broadcast")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].recipientUserId").value(1));
    }

    @Test
    void inbox_returnsMessages() throws Exception {
        when(systemMessageInboxQryExe.execute(any())).thenReturn(PageResult.of(1, List.of(sampleMessage()), 1));

        mockMvc.perform(get(SystemMessageController.BASE_PATH + "/inbox").param("recipientUserId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].content").value("Welcome"));
    }

    @Test
    void markRead_returnsMessage() throws Exception {
        SystemMessage message = sampleMessage();
        message.setReadFlag(1);
        when(markSystemMessageReadCmdExe.execute(any())).thenReturn(message);

        mockMvc.perform(put(SystemMessageController.BASE_PATH + "/{id}/read", 1L)
                        .param("recipientUserId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.readFlag").value(1));
    }

    private static String requestJson() {
        return """
                {
                  "title":"Hi",
                  "content":"Welcome",
                  "sender":"system",
                  "recipientUserId":1,
                  "recipientUserIds":[1,2],
                  "extJson":"{}"
                }
                """;
    }

    private static SystemMessage sampleMessage() {
        SystemMessage message = new SystemMessage();
        message.setId(1L);
        message.setTitle("Hi");
        message.setContent("Welcome");
        message.setSender("system");
        message.setRecipientUserId(1L);
        message.setBroadcastFlag(0);
        message.setReadFlag(0);
        message.setExtJson("{}");
        return message;
    }
}
