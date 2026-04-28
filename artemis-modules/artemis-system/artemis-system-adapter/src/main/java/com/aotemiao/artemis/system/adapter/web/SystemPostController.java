package com.aotemiao.artemis.system.adapter.web;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.framework.core.domain.R;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.adapter.web.dto.SystemDepartmentDTO;
import com.aotemiao.artemis.system.adapter.web.dto.SystemPostDTO;
import com.aotemiao.artemis.system.adapter.web.dto.SystemPostRequest;
import com.aotemiao.artemis.system.app.command.post.CreateSystemPostCmd;
import com.aotemiao.artemis.system.app.command.post.CreateSystemPostCmdExe;
import com.aotemiao.artemis.system.app.command.post.DeleteSystemPostCmd;
import com.aotemiao.artemis.system.app.command.post.DeleteSystemPostCmdExe;
import com.aotemiao.artemis.system.app.command.post.UpdateSystemPostCmd;
import com.aotemiao.artemis.system.app.command.post.UpdateSystemPostCmdExe;
import com.aotemiao.artemis.system.app.query.department.ListSystemDepartmentQry;
import com.aotemiao.artemis.system.app.query.department.ListSystemDepartmentQryExe;
import com.aotemiao.artemis.system.app.query.post.FindSystemPostByIdQry;
import com.aotemiao.artemis.system.app.query.post.FindSystemPostByIdQryExe;
import com.aotemiao.artemis.system.app.query.post.ListSystemPostQry;
import com.aotemiao.artemis.system.app.query.post.ListSystemPostQryExe;
import com.aotemiao.artemis.system.app.query.post.SystemPostPageQry;
import com.aotemiao.artemis.system.app.query.post.SystemPostPageQryExe;
import com.aotemiao.artemis.system.domain.model.department.SystemDepartment;
import com.aotemiao.artemis.system.domain.model.post.SystemPost;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 系统岗位 REST API。 */
@RestController
@RequestMapping(SystemPostController.BASE_PATH)
public class SystemPostController {

    public static final String BASE_PATH = "/api/posts";

    private final CreateSystemPostCmdExe createSystemPostCmdExe;
    private final UpdateSystemPostCmdExe updateSystemPostCmdExe;
    private final DeleteSystemPostCmdExe deleteSystemPostCmdExe;
    private final FindSystemPostByIdQryExe findSystemPostByIdQryExe;
    private final SystemPostPageQryExe systemPostPageQryExe;
    private final ListSystemPostQryExe listSystemPostQryExe;
    private final ListSystemDepartmentQryExe listSystemDepartmentQryExe;

    public SystemPostController(
            CreateSystemPostCmdExe createSystemPostCmdExe,
            UpdateSystemPostCmdExe updateSystemPostCmdExe,
            DeleteSystemPostCmdExe deleteSystemPostCmdExe,
            FindSystemPostByIdQryExe findSystemPostByIdQryExe,
            SystemPostPageQryExe systemPostPageQryExe,
            ListSystemPostQryExe listSystemPostQryExe,
            ListSystemDepartmentQryExe listSystemDepartmentQryExe) {
        this.createSystemPostCmdExe = createSystemPostCmdExe;
        this.updateSystemPostCmdExe = updateSystemPostCmdExe;
        this.deleteSystemPostCmdExe = deleteSystemPostCmdExe;
        this.findSystemPostByIdQryExe = findSystemPostByIdQryExe;
        this.systemPostPageQryExe = systemPostPageQryExe;
        this.listSystemPostQryExe = listSystemPostQryExe;
        this.listSystemDepartmentQryExe = listSystemDepartmentQryExe;
    }

    @PostMapping
    public R<SystemPostDTO> create(@Valid @RequestBody SystemPostRequest request) {
        SystemPost systemPost = createSystemPostCmdExe.execute(new CreateSystemPostCmd(
                request.deptId(),
                request.postCode(),
                request.postCategory(),
                request.postName(),
                request.sortOrder(),
                request.status(),
                request.remarks()));
        return R.ok(toDTO(systemPost));
    }

    @PutMapping("/{id}")
    public R<SystemPostDTO> update(@PathVariable Long id, @Valid @RequestBody SystemPostRequest request) {
        SystemPost systemPost = updateSystemPostCmdExe.execute(new UpdateSystemPostCmd(
                id,
                request.deptId(),
                request.postCode(),
                request.postCategory(),
                request.postName(),
                request.sortOrder(),
                request.status(),
                request.remarks()));
        return R.ok(toDTO(systemPost));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        deleteSystemPostCmdExe.execute(new DeleteSystemPostCmd(id));
        return R.ok();
    }

    @GetMapping("/{id}")
    public R<SystemPostDTO> getById(@PathVariable Long id) {
        if (id == null || id <= 0) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Invalid id: " + id);
        }
        SystemPost systemPost = findSystemPostByIdQryExe
                .execute(new FindSystemPostByIdQry(id))
                .orElseThrow(() -> new BizException(CommonErrorCode.NOT_FOUND, "Post not found: " + id));
        return R.ok(toDTO(systemPost));
    }

    @GetMapping
    public R<PageResult<SystemPostDTO>> page(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        PageResult<SystemPost> pr = systemPostPageQryExe.execute(new SystemPostPageQry(new PageRequest(page, size)));
        return R.ok(
                PageResult.of(pr.total(), pr.content().stream().map(this::toDTO).toList(), pr.totalPages()));
    }

    @GetMapping("/select")
    public R<List<SystemPostDTO>> select() {
        return R.ok(listSystemPostQryExe.execute(new ListSystemPostQry()).stream()
                .map(this::toDTO)
                .toList());
    }

    @GetMapping("/departments/tree")
    public R<List<SystemDepartmentDTO>> departmentTree() {
        return R.ok(toDepartmentTree(listSystemDepartmentQryExe.execute(new ListSystemDepartmentQry(null)), 0L));
    }

    private List<SystemDepartmentDTO> toDepartmentTree(List<SystemDepartment> departments, Long parentId) {
        List<SystemDepartmentDTO> result = new ArrayList<>();
        for (SystemDepartment department : departments) {
            if (parentId.equals(department.getParentId())) {
                result.add(toDepartmentDTO(department, toDepartmentTree(departments, department.getId())));
            }
        }
        return result;
    }

    private SystemDepartmentDTO toDepartmentDTO(SystemDepartment department, List<SystemDepartmentDTO> children) {
        return new SystemDepartmentDTO(
                department.getId(),
                department.getParentId(),
                department.getAncestors(),
                department.getDeptName(),
                department.getDeptCategory(),
                department.getSortOrder(),
                department.getLeaderUserId(),
                department.getPhone(),
                department.getEmail(),
                department.getStatus(),
                department.getRemarks(),
                children);
    }

    private SystemPostDTO toDTO(SystemPost systemPost) {
        return new SystemPostDTO(
                systemPost.getId(),
                systemPost.getDeptId(),
                systemPost.getPostCode(),
                systemPost.getPostCategory(),
                systemPost.getPostName(),
                systemPost.getSortOrder(),
                systemPost.getStatus(),
                systemPost.getRemarks());
    }
}
