package com.sdm.auth;

import com.sdm.auth.model.request.AuthRequest;
import com.sdm.auth.model.request.ForgetPasswordRequest;
import com.sdm.auth.model.request.RegistrationRequest;
import com.sdm.core.DefaultTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.HashMap;
import java.util.Map;

public class AuthControllerTest extends DefaultTest {

    private static String displayName;
    private static String email;
    private static String phone;
    private static String password;

    @BeforeAll
    public static void fakeUser() {
        displayName = faker.name().fullName();
        email = faker.internet().emailAddress();
        phone = faker.phoneNumber().phoneNumber();
        password = faker.regexify("[A-Za-z0-9]{12,25}");
    }

    @AfterAll
    public static void clearUser() {
        displayName = null;
        email = null;
        phone = null;
        password = null;
    }

    /**
     * User Registration on System
     *
     * @throws Exception
     */
    @Test
    @Order(1)
    public void registration() throws Exception {
        RegistrationRequest request = new RegistrationRequest();
        request.setDisplayName(displayName);
        request.setPhoneNumber(phone);
        request.setEmail(email);
        request.setPassword(password);
        request.setDeviceId(DEVICE_ID);
        request.setDeviceOS(DEVICE_OS);
        this.test("/auth/register", HttpMethod.POST, request)
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isCreated());
    }

    /**
     * User login by registered email/phone and password.
     *
     * @throws Exception
     */
    @Test
    @Order(2)
    public void authWithEmail() throws Exception {
        AuthRequest request = new AuthRequest();
        request.setUser(email);
        request.setPassword(password);
        request.setDeviceId(DEVICE_ID);
        request.setDeviceOS(DEVICE_OS);
        this.test("/auth", HttpMethod.POST, request)
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void facebookAuthTest() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("accessToken", faker.regexify("[A-Za-z0-9]{32}"));
        request.put("deviceId", DEVICE_ID);
        request.put("deviceOS", DEVICE_OS);

        this.test("/auth/activate", HttpMethod.POST, request)
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }

    /**
     * Request reset password link.
     *
     * @throws Exception
     */
    @Test
    public void forgetPassword() throws Exception {
        ForgetPasswordRequest request = new ForgetPasswordRequest();
        request.setPhoneNumber(phone);
        request.setEmail(email);
        request.setCallback(faker.internet().url());
        this.test("/auth/forgetPassword", HttpMethod.POST, request)
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void activateUser() throws Exception {
        this.test("/auth/activate", HttpMethod.GET, null)
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void resetPasswordWithOtp() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("user", faker.internet().emailAddress());
        request.put("oldPassword", faker.regexify("[A-Za-z0-9]{12,25}"));
        request.put("newPassword", faker.regexify("[A-Za-z0-9]{12,25}"));

        this.test("/auth/activate", HttpMethod.POST, request)
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }
}
