package com.starry.mb.storage;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.model.GeneratePresignedUrlRequest;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import com.starry.mb.common.exception.BizException;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.net.URL;
import java.util.Date;

/**
 * 腾讯云 COS 实现：
 *   - 上传：后端中转，用永久密钥 PutObject 写入私有桶（一期）
 *   - 读取：临时签名 URL（5 min 过期）
 *
 * 安全策略：
 *   - 桶为私有桶，所有访问都必须签名
 *   - object key 由后端拼接（含 tenant + customer 前缀），客户端无法越权写
 *   - 上传前后端校验当前 principal 与 key 前缀一致
 */
@Slf4j
public class TencentCosStorageService implements StorageService {

    private final StorageProperties props;
    private final COSClient cosClient;

    public TencentCosStorageService(StorageProperties props) {
        this.props = props;
        BasicCOSCredentials cred = new BasicCOSCredentials(props.getSecretId(), props.getSecretKey());
        ClientConfig cfg = new ClientConfig(new Region(props.getRegion()));
        cfg.setHttpProtocol(HttpProtocol.https);
        this.cosClient = new COSClient(cred, cfg);
    }

    @PreDestroy
    public void shutdown() {
        try { cosClient.shutdown(); } catch (Exception ignore) {}
    }

    @Override
    public String upload(String objectKey, InputStream in, long contentLength, String contentType) {
        try {
            ObjectMetadata meta = new ObjectMetadata();
            if (contentLength > 0) meta.setContentLength(contentLength);
            if (contentType != null) meta.setContentType(contentType);

            PutObjectRequest req = new PutObjectRequest(props.getBucket(), objectKey, in, meta);
            cosClient.putObject(req);
            return objectKey;
        } catch (Exception e) {
            log.error("upload failed for key={}", objectKey, e);
            throw new BizException(50002, "上传失败");
        }
    }

    @Override
    public String signedGetUrl(String objectKey, long ttlSeconds) {
        if (objectKey == null || objectKey.isBlank()) return null;
        try {
            Date expire = new Date(System.currentTimeMillis() + ttlSeconds * 1000L);
            GeneratePresignedUrlRequest req = new GeneratePresignedUrlRequest(props.getBucket(), objectKey);
            req.setExpiration(expire);
            URL url = cosClient.generatePresignedUrl(req);
            return url.toString();
        } catch (Exception e) {
            log.error("signedGetUrl failed for {}", objectKey, e);
            throw new BizException(50003, "生成访问链接失败");
        }
    }

    @Override
    public void deleteObject(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) return;
        try {
            cosClient.deleteObject(props.getBucket(), objectKey);
        } catch (Exception e) {
            log.error("deleteObject failed for {}", objectKey, e);
        }
    }

    @Override
    public boolean isReady() { return true; }
}
