package com.example.demo.master;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantDataSourceConfigRepository extends JpaRepository<TenantDataSourceConfigEntity, Long> {
}
