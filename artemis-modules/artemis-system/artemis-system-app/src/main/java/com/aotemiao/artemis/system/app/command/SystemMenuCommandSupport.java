package com.aotemiao.artemis.system.app.command;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.domain.gateway.SystemMenuGateway;
import com.aotemiao.artemis.system.domain.model.SystemMenu;
import java.util.Locale;

final class SystemMenuCommandSupport {

    private SystemMenuCommandSupport() {}

    static Long normalizeParentId(Long parentId) {
        if (parentId == null) {
            return 0L;
        }
        if (parentId < 0) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Invalid parentId: " + parentId);
        }
        return parentId;
    }

    static Integer normalizeSortOrder(Integer sortOrder) {
        if (sortOrder == null) {
            return Integer.valueOf(0);
        }
        return sortOrder;
    }

    static String normalizeText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.strip();
    }

    static String normalizeRequiredText(String value, String fieldName) {
        String normalized = normalizeText(value);
        if (normalized == null) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, fieldName + " must not be blank");
        }
        return normalized;
    }

    static String normalizeMenuType(String menuType) {
        String normalized = normalizeRequiredText(menuType, "menuType").toUpperCase(Locale.ROOT);
        if (!SystemMenu.TYPE_DIRECTORY.equals(normalized)
                && !SystemMenu.TYPE_MENU.equals(normalized)
                && !SystemMenu.TYPE_BUTTON.equals(normalized)) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Unsupported menuType: " + menuType);
        }
        return normalized;
    }

    static void validateParent(SystemMenuGateway systemMenuGateway, Long parentId, Long currentMenuId) {
        if (parentId == 0L) {
            return;
        }
        if (parentId.equals(currentMenuId)) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Menu cannot use itself as parent: " + currentMenuId);
        }
        systemMenuGateway
                .findById(parentId)
                .orElseThrow(() -> new BizException(CommonErrorCode.NOT_FOUND, "Parent menu not found: " + parentId));
    }

    static void validateUniqueName(
            SystemMenuGateway systemMenuGateway, Long parentId, String menuName, Long currentMenuId) {
        systemMenuGateway.findByParentIdAndMenuName(parentId, menuName).ifPresent(existing -> {
            if (!sameId(existing.getId(), currentMenuId)) {
                throw new BizException(CommonErrorCode.BAD_REQUEST, "Menu name already exists: " + menuName);
            }
        });
    }

    static void validateUniquePath(
            SystemMenuGateway systemMenuGateway, String menuType, String path, Long currentMenuId) {
        if (SystemMenu.TYPE_BUTTON.equals(menuType) || path == null) {
            return;
        }
        systemMenuGateway.findByPath(path).ifPresent(existing -> {
            if (!sameId(existing.getId(), currentMenuId)) {
                throw new BizException(CommonErrorCode.BAD_REQUEST, "Menu path already exists: " + path);
            }
        });
    }

    private static boolean sameId(Long left, Long right) {
        return left != null && left.equals(right);
    }
}
