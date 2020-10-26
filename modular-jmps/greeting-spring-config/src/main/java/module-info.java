module greeting.spring.config {
    requires greeting.library;
    requires spring.context;
    requires spring.boot.autoconfigure;

    exports com.example.greeting.config;
}