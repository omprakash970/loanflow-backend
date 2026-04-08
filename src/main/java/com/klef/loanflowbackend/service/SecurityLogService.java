package com.klef.loanflowbackend.service;

import com.klef.loanflowbackend.entity.SecurityLog;
import com.klef.loanflowbackend.repository.SecurityLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service

@RequiredArgsConstructor


public class SecurityLogService {




    private final SecurityLogRepository securityLogRepository;

    public List<SecurityLog> getAll() {
        return securityLogRepository.findByOrderByTimestampDesc();
    }
}

