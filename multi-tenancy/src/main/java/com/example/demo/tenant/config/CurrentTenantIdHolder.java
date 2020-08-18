package com.example.demo.tenant.config;

class CurrentTenantIdHolder {
    private final static ThreadLocal<String> TENANT_STORAGE = new ThreadLocal<String>();

    public static void setTenantId(String id) {
        TENANT_STORAGE.set(id);
    }

    public static String getTenantId() {
        return TENANT_STORAGE.get();
    }
}
