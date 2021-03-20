package ru.otus.app;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import ru.otus.app.contollers.UserController;
import ru.otus.app.dto.UserDto;
import ru.otus.app.model.Role;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = TestConfig.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestUserController {

    private final WebApplicationContext context;
    private final PostgreSQLContainer<?> postgreSQLContainer;
    private MockMvc mvc;

    private final Gson gson = new GsonBuilder().serializeNulls().create();
    private final List<UserDto> inserted = new ArrayList<>();

    @BeforeEach
    public void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    public TestUserController(
            final @Autowired PostgreSQLContainer<?> postgreSQLContainer,
            final @Autowired WebApplicationContext context) {
        this.postgreSQLContainer = postgreSQLContainer;
        this.context = context;
    }

    @Test
    void test() throws Exception {

        final var petr = new UserDto("Petr", "123", Role.USER.getRoleName());
        final var alex = new UserDto("Alex", "1234", Role.USER.getRoleName());
        final var cory = new UserDto("Cory", "fdsf", Role.ADMIN.getRoleName());
        mvc.perform(postBuilder(petr))
                .andExpect(status().isOk())
                .andExpect(content().string(gson.toJson(UserController.CREATION_SUCCESS_RESPONSE.getBody())));
        inserted.add(petr);
        mvc.perform(postBuilder(alex))
                .andExpect(status().isOk())
                .andExpect(content().string(gson.toJson(UserController.CREATION_SUCCESS_RESPONSE.getBody())));
        inserted.add(alex);
        mvc.perform(postBuilder(cory))
                .andExpect(status().isOk())
                .andExpect(content().string(gson.toJson(UserController.CREATION_SUCCESS_RESPONSE.getBody())));
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
