package com.sdm.inventory.controller;

import com.sdm.core.controller.DefaultReadWriteController;
import com.sdm.core.db.repository.DefaultRepository;
import com.sdm.inventory.model.Attribute;
import com.sdm.inventory.repository.AttributeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

@Controller
@RequestMapping("/inventory/attributes")
public class AttributeController extends DefaultReadWriteController<Attribute, Integer> {
    @Autowired
    private AttributeRepository repository;

    @Override
    protected DefaultRepository<Attribute, Integer> getRepository() {
        return this.repository;
    }

    @GetMapping("/types")
    ResponseEntity<List<String>> getAttributeTypes(){
        List<String> result=new ArrayList<>();
        EnumSet.allOf(Attribute.Type.class)
                .forEach(type->result.add(type.toString()));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/all-attrs")
    ResponseEntity<List<Attribute>> getAllAttribute(){
        List<Attribute> result=repository.findAllAttribute();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{guild}/guild")
    ResponseEntity<List<Attribute>> getAttributeByGuild(@PathVariable(value = "guild") String guild){
        List<Attribute> result=repository.findAttributeByGuild(guild);
        return ResponseEntity.ok(result);
    }
}
