package com.aotemiao.artemis.system.adapter.web.department;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.domain.R;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.adapter.web.dto.department.SystemDepartmentDTO;
import com.aotemiao.artemis.system.adapter.web.dto.department.SystemDepartmentRequest;
import com.aotemiao.artemis.system.app.command.department.CreateSystemDepartmentCmd;
import com.aotemiao.artemis.system.app.command.department.CreateSystemDepartmentCmdExe;
import com.aotemiao.artemis.system.app.command.department.DeleteSystemDepartmentCmd;
import com.aotemiao.artemis.system.app.command.department.DeleteSystemDepartmentCmdExe;
import com.aotemiao.artemis.system.app.command.department.UpdateSystemDepartmentCmd;
import com.aotemiao.artemis.system.app.command.department.UpdateSystemDepartmentCmdExe;
import com.aotemiao.artemis.system.app.query.department.FindSystemDepartmentByIdQry;
import com.aotemiao.artemis.system.app.query.department.FindSystemDepartmentByIdQryExe;
import com.aotemiao.artemis.system.app.query.department.ListSystemDepartmentQry;
import com.aotemiao.artemis.system.app.query.department.ListSystemDepartmentQryExe;
import com.aotemiao.artemis.system.domain.model.department.SystemDepartment;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 系统部门 REST API。 */
@RestController
@RequestMapping(SystemDepartmentController.BASE_PATH)
public class SystemDepartmentController {

    public static final String BASE_PATH = "/api/departments";

    private final CreateSystemDepartmentCmdExe createSystemDepartmentCmdExe;
    private final UpdateSystemDepartmentCmdExe updateSystemDepartmentCmdExe;
    private final DeleteSystemDepartmentCmdExe deleteSystemDepartmentCmdExe;
    private final FindSystemDepartmentByIdQryExe findSystemDepartmentByIdQryExe;
    private final ListSystemDepartmentQryExe listSystemDepartmentQryExe;

    public SystemDepartmentController(
            CreateSystemDepartmentCmdExe createSystemDepartmentCmdExe,
            UpdateSystemDepartmentCmdExe updateSystemDepartmentCmdExe,
            DeleteSystemDepartmentCmdExe deleteSystemDepartmentCmdExe,
            FindSystemDepartmentByIdQryExe findSystemDepartmentByIdQryExe,
            ListSystemDepartmentQryExe listSystemDepartmentQryExe) {
        this.createSystemDepartmentCmdExe = createSystemDepartmentCmdExe;
        this.updateSystemDepartmentCmdExe = updateSystemDepartmentCmdExe;
        this.deleteSystemDepartmentCmdExe = deleteSystemDepartmentCmdExe;
        this.findSystemDepartmentByIdQryExe = findSystemDepartmentByIdQryExe;
        this.listSystemDepartmentQryExe = listSystemDepartmentQryExe;
    }

    @PostMapping
    public R<SystemDepartmentDTO> create(@Valid @RequestBody SystemDepartmentRequest request) {
        SystemDepartment systemDepartment = createSystemDepartmentCmdExe.execute(new CreateSystemDepartmentCmd(
                request.parentId(),
                request.deptName(),
                request.deptCategory(),
                request.sortOrder(),
                request.leaderUserId(),
                request.phone(),
                request.email(),
                request.status(),
                request.remarks()));
        return R.ok(toDTO(systemDepartment, List.of()));
    }

    @PutMapping("/{id}")
    public R<SystemDepartmentDTO> update(@PathVariable Long id, @Valid @RequestBody SystemDepartmentRequest request) {
        SystemDepartment systemDepartment = updateSystemDepartmentCmdExe.execute(new UpdateSystemDepartmentCmd(
                id,
                request.parentId(),
                request.deptName(),
                request.deptCategory(),
                request.sortOrder(),
                request.leaderUserId(),
                request.phone(),
                request.email(),
                request.status(),
                request.remarks()));
        return R.ok(toDTO(systemDepartment, List.of()));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        deleteSystemDepartmentCmdExe.execute(new DeleteSystemDepartmentCmd(id));
        return R.ok();
    }

    @GetMapping("/{id}")
    public R<SystemDepartmentDTO> getById(@PathVariable Long id) {
        if (id == null || id <= 0) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Invalid id: " + id);
        }
        SystemDepartment systemDepartment = findSystemDepartmentByIdQryExe
                .execute(new FindSystemDepartmentByIdQry(id))
                .orElseThrow(() -> new BizException(CommonErrorCode.NOT_FOUND, "Department not found: " + id));
        return R.ok(toDTO(systemDepartment, List.of()));
    }

    @GetMapping
    public R<List<SystemDepartmentDTO>> list() {
        return R.ok(listSystemDepartmentQryExe.execute(new ListSystemDepartmentQry(null)).stream()
                .map(dept -> toDTO(dept, List.of()))
                .toList());
    }

    @GetMapping("/tree")
    public R<List<SystemDepartmentDTO>> tree() {
        return R.ok(toTree(listSystemDepartmentQryExe.execute(new ListSystemDepartmentQry(null)), 0L));
    }

    @GetMapping("/tree/exclude/{id}")
    public R<List<SystemDepartmentDTO>> treeExclude(@PathVariable Long id) {
        return R.ok(toTree(listSystemDepartmentQryExe.execute(new ListSystemDepartmentQry(id)), 0L));
    }

    @GetMapping("/select")
    public R<List<SystemDepartmentDTO>> select() {
        return tree();
    }

    private List<SystemDepartmentDTO> toTree(List<SystemDepartment> departments, Long parentId) {
        List<SystemDepartmentDTO> result = new ArrayList<>();
        for (SystemDepartment department : departments) {
            if (parentId.equals(department.getParentId())) {
                result.add(toDTO(department, toTree(departments, department.getId())));
            }
        }
        return result;
    }

    private SystemDepartmentDTO toDTO(SystemDepartment systemDepartment, List<SystemDepartmentDTO> children) {
        return new SystemDepartmentDTO(
                systemDepartment.getId(),
                systemDepartment.getParentId(),
                systemDepartment.getAncestors(),
                systemDepartment.getDeptName(),
                systemDepartment.getDeptCategory(),
                systemDepartment.getSortOrder(),
                systemDepartment.getLeaderUserId(),
                systemDepartment.getPhone(),
                systemDepartment.getEmail(),
                systemDepartment.getStatus(),
                systemDepartment.getRemarks(),
                children);
    }
}
