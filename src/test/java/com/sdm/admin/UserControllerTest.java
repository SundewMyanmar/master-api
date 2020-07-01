package com.sdm.admin;

import com.sdm.admin.model.User;
import com.sdm.core.DefaultReadWriteTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.HashMap;
import java.util.Map;

public class UserControllerTest extends DefaultReadWriteTest {

    @Override
    protected Map<String, Object> createFakeData() {
        Map<String, Object> user = new HashMap<>();
        user.put("email", faker.internet().emailAddress());
        user.put("phoneNumber", faker.phoneNumber().phoneNumber());
        user.put("password", faker.regexify("[A-Za-z0-9]{12,25}"));
        user.put("displayName", faker.name().fullName());

        Map<String, String> extras = new HashMap<>();
        extras.put("company", faker.company().name());
        extras.put("title", faker.name().title());
        extras.put("bloodGroup", faker.name().bloodGroup());
        extras.put("favBook", faker.book().title());
        extras.put("favColor", faker.color().hex());

        user.put("extras", extras);
        user.put("status", User.Status.ACTIVE);

        return user;
    }

    @Override
    protected Map<String, Object> updateFakeData() {
        currentData.put("displayName", faker.name().fullName());
        currentData.put("status", User.Status.PENDING);
        return currentData;
    }

    @Override
    protected Map<String, Object> partialUpdateFakeData() {
        return Map.of("name", faker.name().fullName());
    }

    @Override
    protected String getUrl() {
        return "/admin/users";
    }

    @Test
    public void resetPasswordByUserId() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("user", faker.internet().emailAddress());
        request.put("oldPassword", faker.regexify("[A-Za-z0-9]{12,25}"));
        request.put("newPassword", faker.regexify("[A-Za-z0-9]{12,25}"));

        String url = getUrl() + "/resetPassword/" + 1;
        this.test(url, HttpMethod.PUT, request)
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void cleanTokenByUserId() throws Exception {
        String url = getUrl() + "/cleanToken/" + 1;
        this.test(url, HttpMethod.DELETE, null)
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}
