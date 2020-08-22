package com.example.demo.ejb;

import com.example.demo.model.Post;

import java.util.List;


public interface EjbPostRepository {
    List<Post> findAll();
}
