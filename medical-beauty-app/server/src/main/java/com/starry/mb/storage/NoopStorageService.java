package com.starry.mb.storage;

import com.starry.mb.common.exception.BizException;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;

/**
 * COS 未配置时的兜底实现，确保 Spring 容器能启动。
 * 任何实际调用都会抛 BizException 提示先配置 COS。
 */
@Slf4j
public class NoopStorageService implements StorageService {

    public NoopStorageService() {
        log.warn("StorageService running in NOOP mode — COS credentials not configured. "
                + "Photo upload / signed URL will fail at runtime.");
    }

    @Override
    public String upload(String objectKey, InputStream in, long contentLength, String contentType) {
        throw new BizException(50001, "对象存储未配置，请联系管理员开通 COS");
    }

    @Override
    public String signedGetUrl(String objectKey, long ttlSeconds) {
        throw new BizException(50001, "对象存储未配置，请联系管理员开通 COS");
    }

    @Override
    public void deleteObject(String objectKey) {
        log.warn("[NoopStorage] deleteObject ignored: {}", objectKey);
    }

    @Override
    public boolean isReady() { return false; }
}
