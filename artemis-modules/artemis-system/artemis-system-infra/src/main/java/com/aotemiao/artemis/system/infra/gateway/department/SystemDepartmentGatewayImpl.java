package com.aotemiao.artemis.system.infra.gateway.department;

import com.aotemiao.artemis.system.domain.gateway.department.SystemDepartmentGateway;
import com.aotemiao.artemis.system.domain.model.department.SystemDepartment;
import com.aotemiao.artemis.system.infra.converter.department.SystemDepartmentConverter;
import com.aotemiao.artemis.system.infra.repository.department.SystemDepartmentRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SystemDepartmentGatewayImpl implements SystemDepartmentGateway {

    private final SystemDepartmentRepository repository;

    public SystemDepartmentGatewayImpl(SystemDepartmentRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SystemDepartment save(SystemDepartment systemDepartment) {
        return SystemDepartmentConverter.toDomain(repository.save(SystemDepartmentConverter.toDO(systemDepartment)));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<SystemDepartment> saveAll(List<SystemDepartment> systemDepartments) {
        List<SystemDepartment> saved = new ArrayList<>();
        repository
                .saveAll(systemDepartments.stream()
                        .map(SystemDepartmentConverter::toDO)
                        .toList())
                .forEach(d -> saved.add(SystemDepartmentConverter.toDomain(d)));
        return saved;
    }

    @Override
    public Optional<SystemDepartment> findById(Long id) {
        return repository.findById(id).filter(d -> d.getDeleted() == 0).map(SystemDepartmentConverter::toDomain);
    }

    @Override
    public Optional<SystemDepartment> findByParentIdAndDeptName(Long parentId, String deptName) {
        return repository
                .findByParentIdAndDeptNameAndDeleted(parentId, deptName, 0)
                .map(SystemDepartmentConverter::toDomain);
    }

    @Override
    public List<SystemDepartment> findAll() {
        return repository.findAllByDeletedOrderBySortOrderAscIdAsc(0).stream()
                .map(SystemDepartmentConverter::toDomain)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        repository.findById(id).ifPresent(entity -> {
            entity.setDeleted(1);
            repository.save(entity);
        });
    }
}
