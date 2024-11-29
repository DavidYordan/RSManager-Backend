package com.rsmanager.repository.local;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.rsmanager.dto.application.ApplicationResponseDTO;
import com.rsmanager.dto.application.ApplicationSearchDTO;

public interface ApplicationProcessRecordRepositoryCustom {

    Page<ApplicationResponseDTO> searchApplications(ApplicationSearchDTO request, Pageable pageable);
}
