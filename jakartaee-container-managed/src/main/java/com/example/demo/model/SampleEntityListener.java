package com.example.demo.model;


import javax.persistence.PrePersist;
import java.time.LocalDateTime;

public class SampleEntityListener {

    @PrePersist
    public void beforePersist(Post post) {
        post.setCreatedAt(LocalDateTime.now());
    }
}
