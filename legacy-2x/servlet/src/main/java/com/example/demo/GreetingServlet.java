package com.example.demo;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(value = "/hello", initParams = {@WebInitParam(name = "to", value = "Servlet")})
@RequiredArgsConstructor
@Slf4j
public class GreetingServlet extends HttpServlet {
    private final GreetingService greetingService;
    private final ObjectMapper objectMapper;

    @Override
    public void init(ServletConfig config) throws ServletException {
        var to = config.getInitParameter("to");
        log.debug("The init parameter (to) in servlet: {}", to);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handleRequest(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handleRequest(req, resp);
    }

    private void handleRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        var name = req.getParameter("name");
        log.debug("request parameter name: {}", name);
        var greeting = greetingService.greetTo(name);
        resp.setContentType("application/json");
        resp.getWriter().write(objectMapper.writeValueAsString(greeting));
        resp.getWriter().flush();
    }
}
