package com.starry.mb.storage;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "starry.cos")
public class StorageProperties {
    /** 永久密钥 SecretId（用于生成 STS 临时凭证 / 签名 URL） */
    private String secretId;
    /** 永久密钥 SecretKey */
    private String secretKey;
    /** 区域，如 ap-beijing */
    private String region;
    /** 桶，如 starry-private-1300000000 */
    private String bucket;
    /** 自定义域名（可选）；为空时使用 COS 默认域名 */
    private String endpoint;
    /** 签名 URL 有效期（秒） */
    private long signedUrlTtlSeconds = 300;
    /** STS 临时凭证有效期（秒），最大 7200 */
    private long stsDurationSeconds = 1800;
}
