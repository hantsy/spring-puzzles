package com.example.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

//see: https://stackoverflow.com/questions/8498413/accessing-jackson-object-mapper-in-resteasy
@Component
@Provider
@RequiredArgsConstructor
public class ObjectMapperContextResolver implements ContextResolver<ObjectMapper> {

    private final ObjectMapper objectMapper;

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return this.objectMapper;
    }
}
