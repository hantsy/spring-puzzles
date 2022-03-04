# Using Servlet Components in Spring

In the traditional Spring WebMVC applications, when integrating Servlet, Servlet Listener and Servlet Filter into a Spring application, you have to use `ServletResgistrationBean`, `ListenerRegistrationBean`, `FitlerRegistrationBean` to register the Servlet components as Spring beans.

Spring Boot simplifies the work, and allow you to declare Servlet components  with the original `@WebServlet`, `@WebListener`, `@WebFilter` annotations from Servlet specification but register them as Spring beans.

Open your browser and navigate to [Spring Initialzr](https://start.spring.io web UI,  add *Web*, *Lombok* as dependencies, select Java 17 as language, for other options leave to use the default values. Hit **Generate project** button to download the project skeleton in a zip archive, extract the files and import into your IDE, eg. Intellij IDEA.











