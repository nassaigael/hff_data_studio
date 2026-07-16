package com.henri_fraise.hff_data_studio.repository;

import com.henri_fraise.hff_data_studio.entity.Export;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ExportRepository extends JpaRepository<Export, UUID> {




}
