package com.example.demo.tenant.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.*;

import java.io.Serializable;


@Table(value = "CUSTOMERS")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Customer implements Serializable {

    @Id
    @Column(value = "ID")
    private Integer id;

    @Column(value = "FIRST_NAME")
    private String firstName;

    @Column(value = "LAST_NAME")
    private String lastName;
}
