package com.example.demo.cdi;

import com.example.demo.ejb.EjbPostRepository;
import com.example.demo.repository.PostRepository;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@RequestScoped
@Path("/")
public class PostResources {

    @Inject
    private CdiPostRepository posts;

    @Inject
    private EjbPostRepository ejbPostRepository;

    @Inject
    private PostRepository springPostRepository;

    @GET
    @Produces(APPLICATION_JSON)
    public Response allPosts() {
        return Response.ok(this.posts.findAll()).build();
    }

    @GET
    @Path("ejb")
    @Produces(APPLICATION_JSON)
    public Response allEjbPosts() {
        return Response.ok(this.ejbPostRepository.findAll()).build();
    }

    @GET
    @Path("spring")
    @Produces(APPLICATION_JSON)
    public Response allSpringDataPosts() {
        return Response.ok(this.springPostRepository.findAll()).build();
    }
}
