package com.example.demo.master;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "TENANT_DS_CONFIGS")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantDataSourceConfigEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private Long id;

    @Column(name = "TENANT_ID")
    private String tenantId;

    @Column(name = "URL")
    private String url;

    @Column(name = "USERNAME")
    private String username;

    @Column(name = "PASSWORD")
    private String password;

    @Column(name = "DRIVER_CLASS_NAME")
    private String driverClassName;
}
