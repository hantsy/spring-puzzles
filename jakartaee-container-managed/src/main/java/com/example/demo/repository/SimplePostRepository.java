package com.example.demo.repository;

import com.example.demo.model.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SimplePostRepository {

    private final EntityManager entityManager;

    public List<Post> findAll() {
        var cb = this.entityManager.getCriteriaBuilder();
        var query = cb.createQuery(Post.class);
        var root = query.from(Post.class);

        return this.entityManager.createQuery(query).getResultList();
    }

}
