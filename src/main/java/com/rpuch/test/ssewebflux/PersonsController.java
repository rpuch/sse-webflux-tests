package com.rpuch.test.ssewebflux;

import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class PersonsController {
    private final Service service;

    public PersonsController(Service service) {
        this.service = service;
    }

    @GetMapping(path = "/persons", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<Object>> persons() {
        return service.persons()
                .map(this::personToSse)
                .onErrorResume(e -> Mono.just(throwableToSse(e)));
    }

    private ServerSentEvent<Object> personToSse(Person person) {
        return ServerSentEvent.builder().data(person).build();
    }

    private ServerSentEvent<Object> throwableToSse(Throwable e) {
        return ServerSentEvent.builder().event("internal-error").data(e.getMessage()).build();
    }
}
