package com.example.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter(value = "/greet/*", initParams = {@WebInitParam(name = "to", value = "Filter")})
@RequiredArgsConstructor
@Slf4j
public class GreetingFilter implements Filter {
    private final GreetingService greetingService;
    private final ObjectMapper objectMapper;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        var to = filterConfig.getInitParameter("to");
        log.debug("The init parameter (to) in filter: {}", to);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest req && response instanceof HttpServletResponse resp) {
            var name = req.getParameter("name");
            log.debug("request parameter name: {}", name);
            var greeting = greetingService.greetTo(name);
            resp.setContentType("application/json");
            resp.getWriter().write(objectMapper.writeValueAsString(greeting));
            resp.getWriter().flush();
        } else {
            chain.doFilter(request, response);
        }
    }
}
