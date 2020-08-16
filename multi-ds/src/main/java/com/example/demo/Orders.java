package com.example.demo;

import org.springframework.beans.factory.annotation.Qualifier;

import java.lang.annotation.*;


//@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
//@Retention(RetentionPolicy.RUNTIME)
//@Documented
//@Inherited
@Qualifier("orders")
public @interface Orders {
}
