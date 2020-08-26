package com.example.demo;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Map;

@Component
public class JpaPostRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public List<Post> findAll() {
      var cb = this.entityManager.getCriteriaBuilder();
      var query = cb.createQuery(Post.class);
      var root = query.from(Post.class);
      return this.entityManager.createQuery(query).getResultList();
    }
}
