package com.starry.mb.common.context;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 请求线程上下文：当前登录主体 + 租户 + 门店。
 * 由 AuthInterceptor 解析 JWT 后写入，业务层只读。
 */
public final class PrincipalContext {

    public enum Type {CUSTOMER, EMPLOYEE}

    private static final ThreadLocal<Principal> HOLDER = new ThreadLocal<>();

    public static void set(Principal p) { HOLDER.set(p); }
    public static Principal get()       { return HOLDER.get(); }
    public static void clear()          { HOLDER.remove(); }

    public static Long tenantId()   { Principal p = get(); return p == null ? null : p.tenantId; }
    public static Long storeId()    { Principal p = get(); return p == null ? null : p.storeId; }
    public static Long customerId() {
        Principal p = get();
        return (p != null && p.type == Type.CUSTOMER) ? p.id : null;
    }
    public static Long employeeId() {
        Principal p = get();
        return (p != null && p.type == Type.EMPLOYEE) ? p.id : null;
    }

    @Data
    @AllArgsConstructor
    public static class Principal {
        private Long id;
        private Type type;
        private Long tenantId;
        private Long storeId;
    }
}
