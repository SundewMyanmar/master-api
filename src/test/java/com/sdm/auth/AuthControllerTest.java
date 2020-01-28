package com.sdm.auth;

import com.sdm.auth.model.request.AnonymousRequest;
import com.sdm.auth.model.request.AuthRequest;
import com.sdm.core.DefaultTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.HashMap;
import java.util.Map;

public class AuthControllerTest extends DefaultTest {

    @Test
    public void registration() throws Exception {
        Map<String, Object> register = new HashMap<>();
        register.put("deviceId", "test-device-id");
        register.put("deviceOS", "TestOS");
        register.put("user", "+95(9)123456789");
        register.put("email", "info@sundewmyanmar.com");
        register.put("password", "htoonlin");
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
        AuthRequest request = new AuthRequest("info@sundewmyanmar.com", "htoonlin", "test-device-id", "TestOS", null);
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
