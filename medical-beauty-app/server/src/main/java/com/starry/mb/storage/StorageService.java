package com.starry.mb.storage;

import java.io.InputStream;

/**
 * 对象存储抽象层（一期：后端中转上传 + 服务端签名 URL）。
 *
 * <p>未来若量级 / 体验需要可平滑升级为 STS 直传：
 * 给接口加 `issueUploadCredential(prefix, suggestedKey)`，
 * 业务层无感切换。</p>
 */
public interface StorageService {

    /**
     * 把字节流上传到指定 key，返回最终 object_key。
     */
    String upload(String objectKey, InputStream in, long contentLength, String contentType);

    /**
     * 为私有桶对象生成临时签名 URL。
     */
    String signedGetUrl(String objectKey, long ttlSeconds);

    /**
     * 删除对象（用于硬删调度任务）。
     */
    void deleteObject(String objectKey);

    /**
     * 是否真实可用（false 时调用任何方法应当抛出业务异常）。
     */
    boolean isReady();
}
