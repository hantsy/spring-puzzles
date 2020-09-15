package com.example.demo.master;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.*;

import java.io.Serializable;

@Table("TENANT_CONFIGS")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantConfigEntity implements Serializable {

    @Id
    @Column("ID")
    private Integer id;

    @Column("TENANT_ID")
    private String tenantId;

    @Column("URL")
    private String url;
}
