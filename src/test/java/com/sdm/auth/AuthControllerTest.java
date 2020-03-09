package com.sdm.auth;

import com.github.javafaker.Faker;
import com.github.javafaker.service.FakeValuesService;
import com.github.javafaker.service.RandomService;
import com.sdm.auth.controller.AuthController;
import com.sdm.auth.model.request.AnonymousRequest;
import com.sdm.auth.model.request.AuthRequest;
import com.sdm.core.DefaultTest;
import com.sdm.core.util.Globalizer;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AuthControllerTest extends DefaultTest {
    private final Faker faker;

    public AuthControllerTest(){
        faker = new Faker();
    }

    @Test
    public void registration() throws Exception {
        String displayName = faker.name().fullName();
        String emailAccount = faker.internet().emailAddress();
        String phoneNumber = faker.phoneNumber().phoneNumber();

        Map<String, Object> register = new HashMap<>();
        register.put("deviceId", "test-device-id");
        register.put("deviceOS", "TestOS");
        register.put("phoneNumber", phoneNumber);
        register.put("email", emailAccount);
        register.put("displayName", displayName);
        register.put("password", "p@$$W0rd");

        this.mockMvc.perform(MockMvcRequestBuilders
                .post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .header("User-Agent", "spring-boot-testing")
                .content(objectMapper.writeValueAsString(register)))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isCreated());
}

    @Test
    public void authWithEmail() throws Exception {
        AuthRequest request = new AuthRequest();
        request.setUser("blink.hack@gmail.com");
        request.setPassword("htoonlin");
        request.setDeviceId("test-device-id");
        request.setDeviceOS("TestOS");

        this.mockMvc.perform(MockMvcRequestBuilders
                .post("/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .header("User-Agent", "spring-boot-testing")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

    }

    @Test
    public void anonymousAuth() {
        AnonymousRequest request = new AnonymousRequest();
    }

    @Test
    public void facebookAuth() {

    }

    @Test
    public void forgetPassword() {

    }

    @Test
    public void otpActivateByGet() {

    }

    @Test
    public void otpActivateByPost() {

    }

    @Test
    public void resetPasswordWithOtp() {

    }
}
