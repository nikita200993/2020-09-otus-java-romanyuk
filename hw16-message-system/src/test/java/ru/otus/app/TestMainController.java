package ru.otus.app;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import ru.otus.app.dto.UserDto;
import ru.otus.app.model.Role;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
@WebAppConfiguration
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestMainController {

    private final WebApplicationContext context;
    private final PostgreSQLContainer<?> postgreSQLContainer;
    private MockMvc mvc;

    private final Gson gson = new GsonBuilder().serializeNulls().create();
    private final List<UserDto> inserted = new ArrayList<>();

    @BeforeEach
    public void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    public TestMainController(
            final @Autowired PostgreSQLContainer<?> postgreSQLContainer,
            final @Autowired WebApplicationContext context) {
        this.postgreSQLContainer = postgreSQLContainer;
        this.context = context;
    }

    @Test
    void testInsert() throws Exception {

        final var petr = new UserDto("Petr", "123", Role.USER);
        final var alex = new UserDto("Alex", "1234", Role.USER);
        final var cory = new UserDto("Cory", "fdsf", Role.ADMIN);
        mvc.perform(postBuilder(petr))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));
        inserted.add(petr);
        mvc.perform(postBuilder(alex))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));
        inserted.add(alex);
        mvc.perform(postBuilder(cory))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));
        inserted.add(cory);
        mvc.perform(get("/api/user"))
                .andExpect(status().isOk())
                .andExpect(content().string(gson.toJson(inserted)));
    }

    @AfterAll
    public void tearDown() {
        postgreSQLContainer.close();
    }

    private MockHttpServletRequestBuilder postBuilder(final UserDto userDto) {
        return post("/api/user")
                .content(gson.toJson(userDto))
                .contentType(MediaType.APPLICATION_JSON);
    }
}
