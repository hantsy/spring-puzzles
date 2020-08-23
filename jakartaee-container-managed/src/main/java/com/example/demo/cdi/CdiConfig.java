package com.example.demo.cdi;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@ApplicationScoped
public class CdiConfig {

    @Produces
    @Dependent
    @PersistenceContext(unitName = "blogPU")
    private EntityManager entityManager;
}
