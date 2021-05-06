package com.example.demo.jpa;

import com.example.demo.Post;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Component
public class JpaPostRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true, transactionManager = "jpaTransactionManager")
    public List<Post> findAll() {
        var cb = this.entityManager.getCriteriaBuilder();
        var query = cb.createQuery(Post.class);
        var root = query.from(Post.class);
        return this.entityManager.createQuery(query).getResultList();
    }
}
