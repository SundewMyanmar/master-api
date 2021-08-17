package com.sdm.admin;

import com.sdm.admin.model.User;
import com.sdm.core.DefaultReadWriteTest;
import com.sdm.core.model.Contact;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserControllerTest extends DefaultReadWriteTest {
    public static final int SKIP_USER_ID = 1;
    private static final String[] AVAILABLE_CONTACT_TYPES = {"PHONE", "EMAIL", "SMS", "URL", "ADDRESS", "LAT_LON", "MESSAGING_ID", "SOCIAL_ID"};

    @Override
    protected Map<String, Object> createFakeData() {
        List<Contact> contacts = new ArrayList<>();
        for (int i = 0; i < faker.random().nextInt(1, 5); i++) {
            String type = AVAILABLE_CONTACT_TYPES[faker.random().nextInt(0, AVAILABLE_CONTACT_TYPES.length - 1)];
            contacts.add(createContact(Contact.Type.valueOf(type)));
        }

        Map<String, Object> user = new HashMap<>();
        user.put("email", faker.internet().emailAddress());
        user.put("phoneNumber", faker.phoneNumber().phoneNumber());
        user.put("password", faker.regexify("[A-Za-z0-9]{12,25}"));
        user.put("displayName", faker.name().fullName());
        user.put("type", "CUSTOMER");
        user.put("note", faker.lorem().paragraph(3));
        user.put("contacts", contacts);

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

    @Override
    protected List<Serializable> skipRemoveIds() {
        return List.of(SKIP_USER_ID);
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
