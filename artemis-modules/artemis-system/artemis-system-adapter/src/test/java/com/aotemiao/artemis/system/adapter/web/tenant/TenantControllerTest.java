package com.aotemiao.artemis.system.adapter.web.tenant;

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
import com.aotemiao.artemis.system.app.command.tenant.CreateTenantCmd;
import com.aotemiao.artemis.system.app.command.tenant.CreateTenantCmdExe;
import com.aotemiao.artemis.system.app.command.tenant.DeleteTenantCmdExe;
import com.aotemiao.artemis.system.app.command.tenant.UpdateTenantCmd;
import com.aotemiao.artemis.system.app.command.tenant.UpdateTenantCmdExe;
import com.aotemiao.artemis.system.app.command.tenant.UpdateTenantStatusCmd;
import com.aotemiao.artemis.system.app.command.tenant.UpdateTenantStatusCmdExe;
import com.aotemiao.artemis.system.app.query.tenant.FindTenantByIdQryExe;
import com.aotemiao.artemis.system.app.query.tenant.ListEnabledTenantsQryExe;
import com.aotemiao.artemis.system.app.query.tenant.TenantPageQryExe;
import com.aotemiao.artemis.system.domain.model.tenant.Tenant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class TenantControllerTest {

    private MockMvc mockMvc;

    private CreateTenantCmdExe createTenantCmdExe;
    private UpdateTenantCmdExe updateTenantCmdExe;
    private UpdateTenantStatusCmdExe updateTenantStatusCmdExe;
    private DeleteTenantCmdExe deleteTenantCmdExe;
    private FindTenantByIdQryExe findTenantByIdQryExe;
    private TenantPageQryExe tenantPageQryExe;
    private ListEnabledTenantsQryExe listEnabledTenantsQryExe;

    @BeforeEach
    void setUp() {
        createTenantCmdExe = mock(CreateTenantCmdExe.class);
        updateTenantCmdExe = mock(UpdateTenantCmdExe.class);
        updateTenantStatusCmdExe = mock(UpdateTenantStatusCmdExe.class);
        deleteTenantCmdExe = mock(DeleteTenantCmdExe.class);
        findTenantByIdQryExe = mock(FindTenantByIdQryExe.class);
        tenantPageQryExe = mock(TenantPageQryExe.class);
        listEnabledTenantsQryExe = mock(ListEnabledTenantsQryExe.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new TenantController(
                        createTenantCmdExe,
                        updateTenantCmdExe,
                        updateTenantStatusCmdExe,
                        deleteTenantCmdExe,
                        findTenantByIdQryExe,
                        tenantPageQryExe,
                        listEnabledTenantsQryExe))
                .build();
    }

    @Test
    void create_returnsCreatedTenant() throws Exception {
        when(createTenantCmdExe.execute(any(CreateTenantCmd.class))).thenReturn(sampleTenant());

        mockMvc.perform(post(TenantController.BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "companyName": "阿特米斯科技",
                                  "contactName": "张三",
                                  "contactPhone": "13800000000",
                                  "socialCreditCode": "91310000MA1",
                                  "address": "上海市",
                                  "domain": "demo.artemis.com",
                                  "intro": "示例租户",
                                  "packageId": 1,
                                  "expireTime": "2030-01-01T00:00:00",
                                  "userLimit": 100,
                                  "remarks": "基础说明"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.companyName").value("阿特米斯科技"))
                .andExpect(jsonPath("$.data.tenantNo").value("123456"));
    }

    @Test
    void update_returnsUpdatedTenant() throws Exception {
        Tenant tenant = sampleTenant();
        tenant.setContactName("李四");
        when(updateTenantCmdExe.execute(any(UpdateTenantCmd.class))).thenReturn(tenant);

        mockMvc.perform(put(TenantController.BASE_PATH + "/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "companyName": "阿特米斯科技",
                                  "contactName": "李四",
                                  "contactPhone": "13900000000",
                                  "socialCreditCode": "91310000MA2",
                                  "address": "上海市浦东新区",
                                  "domain": "tenant.demo.artemis.com",
                                  "intro": "更新说明",
                                  "expireTime": "2030-02-01T00:00:00",
                                  "userLimit": 200,
                                  "remarks": "备注"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.contactName").value("李四"));
    }

    @Test
    void updateStatus_returnsUpdatedTenant() throws Exception {
        Tenant tenant = sampleTenant();
        tenant.setStatus("DISABLED");
        when(updateTenantStatusCmdExe.execute(any(UpdateTenantStatusCmd.class))).thenReturn(tenant);

        mockMvc.perform(put(TenantController.BASE_PATH + "/{id}/status", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "DISABLED"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DISABLED"));
    }

    @Test
    void getById_returnsTenant() throws Exception {
        when(findTenantByIdQryExe.execute(any())).thenReturn(Optional.of(sampleTenant()));

        mockMvc.perform(get(TenantController.BASE_PATH + "/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.companyName").value("阿特米斯科技"));
    }

    @Test
    void page_returnsTenantPage() throws Exception {
        when(tenantPageQryExe.execute(any())).thenReturn(PageResult.of(1, List.of(sampleTenant()), 1));

        mockMvc.perform(get(TenantController.BASE_PATH).param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.content[0].tenantNo").value("123456"));
    }

    @Test
    void select_returnsEnabledTenants() throws Exception {
        when(listEnabledTenantsQryExe.execute(any())).thenReturn(List.of(sampleTenant()));

        mockMvc.perform(get(TenantController.BASE_PATH + "/select"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].tenantNo").value("123456"));
    }

    @Test
    void delete_returnsOk() throws Exception {
        mockMvc.perform(delete(TenantController.BASE_PATH + "/{id}", 1L)).andExpect(status().isOk());
    }

    private static Tenant sampleTenant() {
        Tenant tenant = new Tenant();
        tenant.setId(1L);
        tenant.setTenantNo("123456");
        tenant.setCompanyName("阿特米斯科技");
        tenant.setContactName("张三");
        tenant.setContactPhone("13800000000");
        tenant.setSocialCreditCode("91310000MA1");
        tenant.setAddress("上海市");
        tenant.setDomain("demo.artemis.com");
        tenant.setIntro("示例租户");
        tenant.setPackageId(1L);
        tenant.setExpireTime(LocalDateTime.parse("2030-01-01T00:00:00"));
        tenant.setUserLimit(100);
        tenant.setStatus("NORMAL");
        tenant.setRemarks("基础说明");
        return tenant;
    }
}
