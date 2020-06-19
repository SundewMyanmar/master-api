package com.sdm.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sdm.admin.model.Role;
import com.sdm.core.DefaultReadWriteTest;

import java.util.ArrayList;
import java.util.List;

public class RoleControllerTest extends DefaultReadWriteTest{
    @Override
    protected String suffix() {
        return "ROLE";
    }

    public Role generateRole(){
        Role entity=new Role();
        entity.setName(FAKER().name().fullName()+FAKER().number().randomNumber());
        entity.setDescription(FAKER().company().catchPhrase());
        return entity;
    }

    @Override
    protected String getFakeCreate() {
        try {
            return objectMapper.writeValueAsString(generateRole());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected String getFakeUpdate() {
        Role entity=generateRole();
        entity.setId(Integer.parseInt(System.getProperty(suffix()+"ID")));
        try {
            return objectMapper.writeValueAsString(entity);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected String getUrl() {
        return "/admin/roles/";
    }

    @Override
    protected String getDefaultId() {
        return "1";
    }
}
