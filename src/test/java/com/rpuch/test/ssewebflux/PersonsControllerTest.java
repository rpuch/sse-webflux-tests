package com.rpuch.test.ssewebflux;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Config.class)
@AutoConfigureWebMvc
@AutoConfigureMockMvc
class PersonsControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private Service service;

    @Test
    void streamsPersons() throws Exception {
        when(service.persons())
                .thenReturn(Flux.just(new Person("John", "Smith"), new Person("Jane", "Doe")));

        String responseText = mockMvc.perform(get("/persons").accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().string(not(isEmptyString())))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThatJohnAndJaneAreReturned(responseText);
    }

    private void assertThatJohnAndJaneAreReturned(String responseText) {
        List<Person> persons = new NaiveTextEventStream(responseText).dataStream()
                .map(this::parsePerson)
                .collect(Collectors.toList());
        assertThat(persons, hasSize(2));
        assertThat(persons.get(0).getFirstName(), is("John"));
        assertThat(persons.get(1).getFirstName(), is("Jane"));
    }

    private Person parsePerson(String data) {
        try {
            return objectMapper.readerFor(Person.class).readValue(data);
        } catch (IOException e) {
            throw new RuntimeException("Cannot parse", e);
        }
    }

    @Test
    void handlesExceptionDuringStreaming() throws Exception {
        when(service.persons())
                .thenReturn(Flux.error(new RuntimeException("Oops!")));

        String responseText = mockMvc.perform(get("/persons").accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(status().is2xxSuccessful())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(responseText, is("event:internal-error\ndata:Oops!\n\n"));
    }

//    @Test
    void manyTimes() throws Exception {
        for (int i = 0; i < 1000; i++) {
            streamsPersons();
            handlesExceptionDuringStreaming();
        }
    }
}