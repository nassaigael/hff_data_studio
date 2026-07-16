package com.henri_fraise.hff_data_studio.repository;


import com.henri_fraise.hff_data_studio.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {



}
