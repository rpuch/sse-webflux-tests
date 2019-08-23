package com.rpuch.test.ssewebflux;

import reactor.core.publisher.Flux;

public interface Service {
    Flux<Person> persons();
}
