package com.example.demo;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ReactiveAdapterRegistry;
import org.springframework.core.ReactiveTypeDescriptor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

// Spring 5.3.10 adds SmallRye Mutiny support officially.
// see: https://github.com/spring-projects/spring-framework/pull/27331
@Component
@RequiredArgsConstructor
@Slf4j
public class MutinyAdapter {
    private final ReactiveAdapterRegistry registry;

    @PostConstruct
    public void registerAdapters(){
        log.debug("registering MutinyAdapter");
        registry.registerReactiveType(
            ReactiveTypeDescriptor.singleOptionalValue(Uni.class, ()-> Uni.createFrom().nothing()),
            uni ->((Uni<?>)uni).convert().toPublisher(),
            publisher ->  Uni.createFrom().publisher(publisher)
        );

        registry.registerReactiveType(
            ReactiveTypeDescriptor.multiValue(Multi.class, ()-> Multi.createFrom().empty()),
            multi -> (Multi<?>) multi,
            publisher-> Multi.createFrom().publisher(publisher));
    }
}
