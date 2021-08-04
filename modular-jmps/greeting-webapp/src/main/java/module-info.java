//import com.example.greeting.app.GreetingApplication;

module greeting.webapp {
    requires greeting.library;
    requires greeting.spring.config;
    requires spring.boot.starter.web;
    requires spring.boot.autoconfigure;
    requires spring.boot;
    requires spring.web;
    requires spring.context;

    exports com.example.greeting.app;
    opens com.example.greeting.app to  spring.context, spring.core, spring.boot, spring.web;
    //uses GreetingApplication;
}