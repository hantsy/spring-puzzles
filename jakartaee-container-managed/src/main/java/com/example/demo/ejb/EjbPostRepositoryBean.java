package com.example.demo.ejb;

import com.example.demo.model.Post;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Stateless
public class EjbPostRepositoryBean implements EjbPostRepository{

    @PersistenceContext(unitName = "blogPU")
    private EntityManager entityManager;

    public List<Post> findAll() {
        var cb = this.entityManager.getCriteriaBuilder();
        var query = cb.createQuery(Post.class);
        var root = query.from(Post.class);

        return this.entityManager.createQuery(query).getResultList();
    }
}
