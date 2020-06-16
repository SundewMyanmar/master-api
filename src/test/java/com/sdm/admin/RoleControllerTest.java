package com.sdm.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sdm.admin.model.Role;
import com.sdm.core.DefaultReadWriteTest;

import java.util.ArrayList;
import java.util.List;

public class RoleControllerTest /*extends DefaultReadWriteTest */{
    /*@Override
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
    protected String getMultiFakeCreate() {
        List<Role> entities=new ArrayList<>();
        entities.add(generateRole());
        entities.add(generateRole());

        try {
            return objectMapper.writeValueAsString(entities);
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
    protected String getMultiFakeUpdate() {
        List<Role> entities=new ArrayList<>();
        Role entity1=generateRole();
        entity1.setId(Integer.parseInt(System.getProperty(suffix()+"ID2")));
        Role entity2=generateRole();
        entity2.setId(Integer.parseInt(System.getProperty(suffix()+"ID3")));
        entities.add(entity1);
        entities.add(entity2);

        try {
            return objectMapper.writeValueAsString(entities);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected String getUrl() {
        return "/roles/";
    }

    @Override
    protected String getDefaultId() {
        return "1";
    }*/
}
