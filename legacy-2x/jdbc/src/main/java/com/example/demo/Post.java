package com.example.demo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "POSTS")
@EntityListeners(AuditingEntityListener.class)

// for data jdbc
@org.springframework.data.relational.core.mapping.Table("POSTS")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "POSTS_SEQ")
    @SequenceGenerator(sequenceName = "POSTS_SEQ", allocationSize = 1, name = "POSTS_SEQ")
    @Column(name = "ID")

    // for data jdbc
    @org.springframework.data.annotation.Id
    @org.springframework.data.relational.core.mapping.Column("ID")
    private Long id;

    @Column(name = "TITLE")
    @org.springframework.data.relational.core.mapping.Column("TITLE")
    private String title;

    @Column(name = "BODY")
    @org.springframework.data.relational.core.mapping.Column("BODY")
    private String body;

    @Column(name = "CREATED_AT")
    @org.springframework.data.relational.core.mapping.Column("CREATED_AT")
    @CreatedDate
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT")
    @org.springframework.data.relational.core.mapping.Column("UPDATED_AT")
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
