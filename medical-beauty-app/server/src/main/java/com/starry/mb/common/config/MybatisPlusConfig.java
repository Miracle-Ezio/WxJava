package com.starry.mb.common.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.starry.mb.common.context.PrincipalContext;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

/**
 * 自动填充 created_at / updated_at / created_by / tenant_id / store_id。
 * 业务代码只关心业务字段。
 */
@Configuration
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor i = new MybatisPlusInterceptor();
        i.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return i;
    }

    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler() {
            @Override
            public void insertFill(MetaObject metaObject) {
                LocalDateTime now = LocalDateTime.now();
                strictInsertFill(metaObject, "createdAt", LocalDateTime.class, now);
                strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, now);

                Long tid = PrincipalContext.tenantId();
                if (tid != null) {
                    strictInsertFill(metaObject, "tenantId", Long.class, tid);
                }
                Long sid = PrincipalContext.storeId();
                if (sid != null) {
                    strictInsertFill(metaObject, "storeId", Long.class, sid);
                }

                Long uid = PrincipalContext.employeeId() != null
                        ? PrincipalContext.employeeId()
                        : PrincipalContext.customerId();
                if (uid != null) {
                    strictInsertFill(metaObject, "createdBy", Long.class, uid);
                }
            }

            @Override
            public void updateFill(MetaObject metaObject) {
                strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
            }
        };
    }
}
