module greeting.service.impl {
    requires greeting.service;

    provides com.example.greeting.service.api.GreetingService
            with com.example.greeting.service.impl.DefaultGreetingService;
    exports com.example.greeting.service.impl;
}