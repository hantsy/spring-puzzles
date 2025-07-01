/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author hantsy
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "")
public class PostController {

    private final PostRepository posts;

    @GetMapping
    public CompletableFuture<List<Post>> all() {
        return this.posts.readAllBy();
    }

}
