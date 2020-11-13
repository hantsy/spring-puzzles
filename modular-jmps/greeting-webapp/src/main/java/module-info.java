//import com.example.greeting.app.GreetingApplication;

module greeting.webapp {
    requires greeting.library;
    requires greeting.spring.config;
    requires spring.boot;
    requires spring.web;
    requires spring.boot.autoconfigure;
    requires spring.boot.starter.web;

    exports com.example.greeting.app;
    opens com.example.greeting.app;
    //uses GreetingApplication;
}