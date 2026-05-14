package com.starry.mb.storage;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 预留：未来切换到 STS 直传模式时返回的临时凭证。
 * 一期采用后端中转上传，本类暂未使用。
 */
@Data
@AllArgsConstructor
public class UploadCredential {
    private String tmpSecretId;
    private String tmpSecretKey;
    private String sessionToken;
    private String bucket;
    private String region;
    private String keyPrefix;
    private String suggestedKey;
    private long expiredTime;
}
