package com.sdm.inventory.repository;

import com.sdm.core.db.repository.DefaultRepository;
import com.sdm.inventory.model.Attribute;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttributeRepository extends DefaultRepository<Attribute, Integer> {
    @Query("SELECT a FROM #{#entityName} a WHERE a.guild=:guild order by a.id ASC")
    List<Attribute> findAttributeByGuild(@Param("guild") String guild);

    @Query("SELECT a FROM #{#entityName} a order by a.id,a.guild ASC")
    List<Attribute> findAllAttribute();
}
