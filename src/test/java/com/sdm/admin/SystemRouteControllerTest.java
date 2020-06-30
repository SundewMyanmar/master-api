package com.sdm.admin;

import com.sdm.core.DefaultReadTest;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SystemRouteControllerTest extends DefaultReadTest {
    private Map<String, Object> createSystemRoute() {
        Map<String, Object> route = new HashMap<>();
        route.put("pattern", faker.internet().domainSuffix());
        route.put("module", faker.lorem().word() + "-controller");
        route.put("httpMethod", faker.regexify("(get|post|put|delete)"));
        return route;
    }

    @Override
    protected Integer getId() {
        return 1;
    }

    @Override
    protected String getUrl() {
        return "/admin/routes";
    }

    @Test
    @Order(2)
    public void savePermission() throws Exception {
        List permissions = List.of(createSystemRoute());
        String url = getUrl() + "/role/" + RoleControllerTest.SKIP_ROLE_ID;
        this.test(url, HttpMethod.POST, permissions)
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isCreated());
    }

    @Test
    @Order(3)
    public void getByRole() throws Exception {
        String url = getUrl() + "/role/" + RoleControllerTest.SKIP_ROLE_ID;
        this.test(url, HttpMethod.GET, null)
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}
