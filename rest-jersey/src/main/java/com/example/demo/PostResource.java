package com.example.demo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/posts")
@Component
@RequiredArgsConstructor
public class PostResource {

    private final PostRepository posts;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response all() {
        return Response.ok(this.posts.findAll()).build();
    }
}
