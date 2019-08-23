package com.rpuch.test.ssewebflux;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

class NaiveTextEventStream {
    private static final String DATA_PREFIX = "data:";

    private final String wholeContent;

    NaiveTextEventStream(String wholeContent) {
        Objects.requireNonNull(wholeContent, "wholeContent cannot be null");

        this.wholeContent = wholeContent;
    }

    Stream<String> dataStream() {
        return Arrays.stream(wholeContent.split("\n"))
                .filter(line -> line.startsWith(DATA_PREFIX))
                .map(line -> line.substring(DATA_PREFIX.length()));
    }
}
