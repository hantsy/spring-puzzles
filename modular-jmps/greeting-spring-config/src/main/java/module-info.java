module greeting.spring.config {
    requires greeting.library;
    requires spring.context;
    requires spring.boot.autoconfigure;

    opens com.example.greeting.config to spring.core, spring.context;
    exports com.example.greeting.config;
}