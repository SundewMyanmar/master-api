package com.sdm.inventory.repository;

import com.sdm.inventory.model.ProductAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductAttachmentRepository extends JpaRepository<ProductAttachment, Long> {
}
