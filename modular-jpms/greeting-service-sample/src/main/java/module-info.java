open module greeting.service.sample {
    requires greeting.service;
    requires greeting.service.impl;

    uses com.example.greeting.service.api.GreetingService;
    //opens com.example.greeting.service.sample;
    exports com.example.greeting.service.sample;
}