package com.example.demo.cdi;

import com.example.demo.model.Post;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.List;

@ApplicationScoped
public class CdiPostRepository {

    @Inject
    private EntityManager entityManager;

    public List<Post> findAll() {
        var cb = this.entityManager.getCriteriaBuilder();
        var query = cb.createQuery(Post.class);
        var root = query.from(Post.class);

        return this.entityManager.createQuery(query).getResultList();
    }
}
