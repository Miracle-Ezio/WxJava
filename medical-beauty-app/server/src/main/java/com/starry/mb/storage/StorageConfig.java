package com.starry.mb.storage;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageConfig {

    /**
     * COS 凭证齐备时启用真实实现；否则用 NoopStorageService，
     * 让应用能起来便于开发期跑通其它模块。
     */
    @Bean
    public StorageService storageService(StorageProperties props) {
        boolean ready = StringUtils.isNoneBlank(
                props.getSecretId(), props.getSecretKey(),
                props.getRegion(),   props.getBucket());
        return ready ? new TencentCosStorageService(props) : new NoopStorageService();
    }
}
