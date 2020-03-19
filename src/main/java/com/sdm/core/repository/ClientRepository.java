package com.sdm.core.repository;

import com.sdm.core.model.ClientInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<ClientInfo, String> {
    Optional<ClientInfo> findOneByRemoteAddress(String remoteAddress);
}
