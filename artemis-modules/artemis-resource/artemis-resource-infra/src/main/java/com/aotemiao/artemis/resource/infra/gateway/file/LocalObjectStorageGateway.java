package com.aotemiao.artemis.resource.infra.gateway.file;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.resource.domain.gateway.file.ObjectStorageGateway;
import com.aotemiao.artemis.resource.domain.model.file.DownloadedObject;
import com.aotemiao.artemis.resource.domain.model.file.StoredObject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.HexFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** 本地文件系统对象存储适配器，作为 OSS 最小闭环默认实现。 */
@Component
public class LocalObjectStorageGateway implements ObjectStorageGateway {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final Path storageRoot;
    private final String publicBaseUrl;

    public LocalObjectStorageGateway(
            @Value("${artemis.resource.storage.local.root:storage/resource}") String storageRoot,
            @Value("${artemis.resource.storage.public-base-url:/api/resource/oss-files}") String publicBaseUrl) {
        this.storageRoot = Paths.get(storageRoot).toAbsolutePath().normalize();
        this.publicBaseUrl = trimTrailingSlash(publicBaseUrl);
    }

    @Override
    public StoredObject store(String originalFileName, byte[] content) {
        String suffix = extractSuffix(originalFileName);
        String fileName = randomFileName(suffix);
        String objectKey = LocalDate.now() + "/" + fileName;
        Path target = resolveObjectPath(objectKey);
        Path parent = target.getParent();
        if (parent == null) {
            throw new BizException(CommonErrorCode.INTERNAL_ERROR, "Invalid storage path: " + objectKey);
        }
        try {
            Files.createDirectories(parent);
            Files.write(target, content);
        } catch (IOException ex) {
            throw new BizException(CommonErrorCode.INTERNAL_ERROR, "Failed to store object: " + objectKey);
        }
        return new StoredObject(fileName, suffix, publicBaseUrl + "/" + objectKey, objectKey, content.length);
    }

    @Override
    public DownloadedObject load(String objectKey) {
        Path target = resolveObjectPath(objectKey);
        if (!Files.exists(target)) {
            throw new BizException(CommonErrorCode.NOT_FOUND, "Stored object not found: " + objectKey);
        }
        Path fileName = target.getFileName();
        if (fileName == null) {
            throw new BizException(CommonErrorCode.INTERNAL_ERROR, "Invalid storage path: " + objectKey);
        }
        try {
            return new DownloadedObject(fileName.toString(), Files.readAllBytes(target));
        } catch (IOException ex) {
            throw new BizException(CommonErrorCode.INTERNAL_ERROR, "Failed to read object: " + objectKey);
        }
    }

    @Override
    public void delete(String objectKey) {
        try {
            Files.deleteIfExists(resolveObjectPath(objectKey));
        } catch (IOException ex) {
            throw new BizException(CommonErrorCode.INTERNAL_ERROR, "Failed to delete object: " + objectKey);
        }
    }

    private Path resolveObjectPath(String objectKey) {
        Path target = storageRoot.resolve(objectKey).normalize();
        if (!target.startsWith(storageRoot)) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Invalid object key: " + objectKey);
        }
        return target;
    }

    private String randomFileName(String suffix) {
        byte[] bytes = new byte[16];
        RANDOM.nextBytes(bytes);
        String name = HexFormat.of().formatHex(bytes);
        return suffix == null ? name : name + "." + suffix;
    }

    private String extractSuffix(String originalFileName) {
        if (originalFileName == null || originalFileName.isBlank()) {
            return null;
        }
        Path fileName = Paths.get(originalFileName).getFileName();
        if (fileName == null) {
            return null;
        }
        String normalized = fileName.toString();
        int index = normalized.lastIndexOf('.');
        if (index < 0 || index == normalized.length() - 1) {
            return null;
        }
        return normalized.substring(index + 1).toLowerCase();
    }

    private String trimTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String normalized = value.strip();
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }
}
