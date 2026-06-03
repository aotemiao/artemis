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
import com.aotemiao.artemis.system.app.command.tenant.CreateTenantPackageCmdExe;
import com.aotemiao.artemis.system.app.command.tenant.DeleteTenantPackageCmdExe;
import com.aotemiao.artemis.system.app.command.tenant.TenantPackageCmd;
import com.aotemiao.artemis.system.app.command.tenant.UpdateTenantPackageCmd;
import com.aotemiao.artemis.system.app.command.tenant.UpdateTenantPackageCmdExe;
import com.aotemiao.artemis.system.app.command.tenant.UpdateTenantPackageStatusCmd;
import com.aotemiao.artemis.system.app.command.tenant.UpdateTenantPackageStatusCmdExe;
import com.aotemiao.artemis.system.app.query.tenant.FindTenantPackageByIdQryExe;
import com.aotemiao.artemis.system.app.query.tenant.ListEnabledTenantPackagesQryExe;
import com.aotemiao.artemis.system.app.query.tenant.TenantPackagePageQryExe;
import com.aotemiao.artemis.system.domain.model.tenant.TenantPackage;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class TenantPackageControllerTest {

    private MockMvc mockMvc;

    private CreateTenantPackageCmdExe createTenantPackageCmdExe;
    private UpdateTenantPackageCmdExe updateTenantPackageCmdExe;
    private UpdateTenantPackageStatusCmdExe updateTenantPackageStatusCmdExe;
    private DeleteTenantPackageCmdExe deleteTenantPackageCmdExe;
    private FindTenantPackageByIdQryExe findTenantPackageByIdQryExe;
    private TenantPackagePageQryExe tenantPackagePageQryExe;
    private ListEnabledTenantPackagesQryExe listEnabledTenantPackagesQryExe;

    @BeforeEach
    void setUp() {
        createTenantPackageCmdExe = mock(CreateTenantPackageCmdExe.class);
        updateTenantPackageCmdExe = mock(UpdateTenantPackageCmdExe.class);
        updateTenantPackageStatusCmdExe = mock(UpdateTenantPackageStatusCmdExe.class);
        deleteTenantPackageCmdExe = mock(DeleteTenantPackageCmdExe.class);
        findTenantPackageByIdQryExe = mock(FindTenantPackageByIdQryExe.class);
        tenantPackagePageQryExe = mock(TenantPackagePageQryExe.class);
        listEnabledTenantPackagesQryExe = mock(ListEnabledTenantPackagesQryExe.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new TenantPackageController(
                        createTenantPackageCmdExe,
                        updateTenantPackageCmdExe,
                        updateTenantPackageStatusCmdExe,
                        deleteTenantPackageCmdExe,
                        findTenantPackageByIdQryExe,
                        tenantPackagePageQryExe,
                        listEnabledTenantPackagesQryExe))
                .build();
    }

    @Test
    void create_returnsCreatedPackage() throws Exception {
        when(createTenantPackageCmdExe.execute(any(TenantPackageCmd.class))).thenReturn(samplePackage());

        mockMvc.perform(post(TenantPackageController.BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "packageName": "标准版",
                                  "menuCheckStrictly": true,
                                  "enabled": true,
                                  "menuIds": [1, 2]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.packageName").value("标准版"))
                .andExpect(jsonPath("$.data.menuIds[1]").value(2));
    }

    @Test
    void update_returnsUpdatedPackage() throws Exception {
        TenantPackage tenantPackage = samplePackage();
        tenantPackage.setPackageName("专业版");
        when(updateTenantPackageCmdExe.execute(any(UpdateTenantPackageCmd.class)))
                .thenReturn(tenantPackage);

        mockMvc.perform(put(TenantPackageController.BASE_PATH + "/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "packageName": "专业版",
                                  "menuCheckStrictly": false,
                                  "enabled": true,
                                  "menuIds": [1]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.packageName").value("专业版"));
    }

    @Test
    void updateStatus_returnsUpdatedPackage() throws Exception {
        TenantPackage tenantPackage = samplePackage();
        tenantPackage.setEnabled(false);
        when(updateTenantPackageStatusCmdExe.execute(any(UpdateTenantPackageStatusCmd.class)))
                .thenReturn(tenantPackage);

        mockMvc.perform(put(TenantPackageController.BASE_PATH + "/{id}/status", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "enabled": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.enabled").value(false));
    }

    @Test
    void getById_returnsPackage() throws Exception {
        when(findTenantPackageByIdQryExe.execute(any())).thenReturn(Optional.of(samplePackage()));

        mockMvc.perform(get(TenantPackageController.BASE_PATH + "/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.packageName").value("标准版"));
    }

    @Test
    void page_returnsPackagePage() throws Exception {
        when(tenantPackagePageQryExe.execute(any())).thenReturn(PageResult.of(1, List.of(samplePackage()), 1));

        mockMvc.perform(get(TenantPackageController.BASE_PATH)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.content[0].packageName").value("标准版"));
    }

    @Test
    void select_returnsEnabledPackages() throws Exception {
        when(listEnabledTenantPackagesQryExe.execute(any())).thenReturn(List.of(samplePackage()));

        mockMvc.perform(get(TenantPackageController.BASE_PATH + "/select"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].packageName").value("标准版"));
    }

    @Test
    void delete_returnsOk() throws Exception {
        mockMvc.perform(delete(TenantPackageController.BASE_PATH + "/{id}", 1L)).andExpect(status().isOk());
    }

    private static TenantPackage samplePackage() {
        TenantPackage tenantPackage = new TenantPackage();
        tenantPackage.setId(1L);
        tenantPackage.setPackageName("标准版");
        tenantPackage.setMenuCheckStrictly(true);
        tenantPackage.setEnabled(true);
        tenantPackage.setMenuIds(List.of(1L, 2L));
        return tenantPackage;
    }
}
