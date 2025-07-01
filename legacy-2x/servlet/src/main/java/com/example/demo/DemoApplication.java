package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@ServletComponentScan
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	//similar to the servlet `ServletContainerInitializer`, but managed by Spring.
	@Bean
	public ServletContextInitializer servletContextInitializer() {
		return servletContext -> servletContext.setInitParameter("hello", "world");
	}

}
