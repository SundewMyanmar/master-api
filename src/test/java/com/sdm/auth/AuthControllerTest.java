package com.sdm.auth;

import com.github.javafaker.Faker;
import com.sdm.auth.model.request.AnonymousRequest;
import com.sdm.auth.model.request.AuthRequest;
import com.sdm.core.DefaultTest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import java.util.Properties;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthControllerTest extends DefaultTest {
    private final Faker faker;

    public AuthControllerTest() {
        faker = new Faker();
    }

    @Test
    @Order(1)
    public void registration() throws Exception {
        String displayName = faker.name().fullName();
        String emailAccount = faker.internet().emailAddress();
        String phoneNumber = faker.phoneNumber().phoneNumber();

        Properties register=new Properties();
        register.put("deviceId", this.deviceId);
        register.put("deviceOS", this.deviceOs);
        register.put("phoneNumber", phoneNumber);
        register.put("email", emailAccount);
        register.put("displayName", displayName);
        register.put("password", faker.regexify("[A-Za-z0-9]{12,25}"));

        System.setProperties(register);

        this.mockMvc.perform(MockMvcRequestBuilders
                .post("/auth/register")
                .header("user-agent",faker.internet().userAgentAny())
                .header("x-forwarded-for",faker.internet().ipV4Address())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(register)))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isCreated());
    }

    @Test
    @Order(2)
    public void authWithEmail() throws Exception {
        AuthRequest request = new AuthRequest();
        request.setUser(System.getProperty("email").toString());
        request.setPassword(System.getProperty("password").toString());
        request.setDeviceId(System.getProperty("deviceId").toString());
        request.setDeviceOS(System.getProperty("deviceOS").toString());
        this.mockMvc.perform(MockMvcRequestBuilders
                .post("/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .header("user-agent",faker.internet().userAgentAny())
                .header("x-forwarded-for",faker.internet().ipV4Address())
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());;
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
