package com.aotemiao.artemis.system.adapter.web;

import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.app.command.CreateLookupTypeCmdExe;
import com.aotemiao.artemis.system.app.command.DeleteLookupTypeCmdExe;
import com.aotemiao.artemis.system.app.command.UpdateLookupTypeCmdExe;
import com.aotemiao.artemis.system.app.query.FindLookupTypeByIdQryExe;
import com.aotemiao.artemis.system.app.query.GetLookupItemsByTypeCodeQryExe;
import com.aotemiao.artemis.system.app.query.LookupTypePageQryExe;
import com.aotemiao.artemis.system.domain.model.LookupItem;
import com.aotemiao.artemis.system.domain.model.LookupType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import jakarta.servlet.ServletException;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 独立 MockMvc 测试，不依赖 @WebMvcTest / @SpringBootConfiguration，
 * 避免 adapter 模块无 start 时无法解析配置、且 Controller 映射可稳定生效。
 * <p>抛异常场景（NOT_FOUND/BAD_REQUEST）在 standalone 下无 GlobalExceptionHandler 故返回 500；
 * 实际 404/400 由完整上下文中的 GlobalExceptionHandler 提供，可在集成测试中验证。</p>
 */
class LookupTypeControllerTest {

    private MockMvc mockMvc;

    private FindLookupTypeByIdQryExe findLookupTypeByIdQryExe;
    private GetLookupItemsByTypeCodeQryExe getLookupItemsByTypeCodeQryExe;

    @BeforeEach
    void setUp() {
        findLookupTypeByIdQryExe = mock(FindLookupTypeByIdQryExe.class);
        getLookupItemsByTypeCodeQryExe = mock(GetLookupItemsByTypeCodeQryExe.class);
        LookupTypeController controller = new LookupTypeController(
                mock(CreateLookupTypeCmdExe.class),
                mock(UpdateLookupTypeCmdExe.class),
                mock(DeleteLookupTypeCmdExe.class),
                mock(LookupTypePageQryExe.class),
                findLookupTypeByIdQryExe,
                getLookupItemsByTypeCodeQryExe
        );
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getById_returns200_andLookupTypeDTO() throws Exception {
        Long id = 1L;
        LookupType type = new LookupType();
        type.setId(id);
        type.setCode("GENDER");
        type.setName("Gender");
        type.setDescription("User gender");
        when(findLookupTypeByIdQryExe.execute(any())).thenReturn(Optional.of(type));

        mockMvc.perform(get(LookupTypeController.BASE_PATH + "/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.code").value("GENDER"))
                .andExpect(jsonPath("$.data.name").value("Gender"));
    }

    @Test
    void getById_whenNotFound_throwsBizException() {
        when(findLookupTypeByIdQryExe.execute(any())).thenReturn(Optional.empty());
        ServletException ex = assertThrows(ServletException.class,
                () -> mockMvc.perform(get(LookupTypeController.BASE_PATH + "/{id}", 999L)));
        assertThat(ex.getCause()).isInstanceOf(BizException.class);
    }

    @Test
    void getById_whenInvalidId_throwsBizException() {
        ServletException ex = assertThrows(ServletException.class,
                () -> mockMvc.perform(get(LookupTypeController.BASE_PATH + "/{id}", 0)));
        assertThat(ex.getCause()).isInstanceOf(BizException.class);
    }

    @Test
    void getItemsByTypeCode_returns200_andItemList() throws Exception {
        String typeCode = "GENDER";
        LookupItem item = new LookupItem();
        item.setId(10L);
        item.setValue("1");
        item.setLabel("Male");
        item.setSortOrder(1);
        when(getLookupItemsByTypeCodeQryExe.execute(any())).thenReturn(List.of(item));

        mockMvc.perform(get(LookupTypeController.BASE_PATH + "/{typeCode}/items", typeCode))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].value").value("1"))
                .andExpect(jsonPath("$.data[0].label").value("Male"))
                .andExpect(jsonPath("$.data[0].sortOrder").value(1));
    }

    @Test
    void getItemsByTypeCode_whenEmpty_returns200AndEmptyArray() throws Exception {
        when(getLookupItemsByTypeCodeQryExe.execute(any())).thenReturn(List.of());

        mockMvc.perform(get(LookupTypeController.BASE_PATH + "/{typeCode}/items", "UNKNOWN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    void getItemsByTypeCode_whenTypeCodeBlank_throwsBizException() {
        ServletException ex = assertThrows(ServletException.class,
                () -> mockMvc.perform(get(LookupTypeController.BASE_PATH + "/{typeCode}/items", " ")));
        assertThat(ex.getCause()).isInstanceOf(BizException.class);
    }
}
