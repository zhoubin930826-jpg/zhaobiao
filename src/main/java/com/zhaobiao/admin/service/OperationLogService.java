package com.zhaobiao.admin.service;

import com.zhaobiao.admin.dto.log.OperationLogDto;
import com.zhaobiao.admin.entity.OperationLog;
import com.zhaobiao.admin.mapper.ViewMapper;
import com.zhaobiao.admin.repository.OperationLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OperationLogService {

    private final OperationLogRepository operationLogRepository;
    private final ViewMapper viewMapper;

    public OperationLogService(OperationLogRepository operationLogRepository,
                               ViewMapper viewMapper) {
        this.operationLogRepository = operationLogRepository;
        this.viewMapper = viewMapper;
    }

    @Transactional
    public void save(OperationLog operationLog) {
        operationLogRepository.save(operationLog);
    }

    @Transactional(readOnly = true)
    public List<OperationLogDto> listLogs() {
        return operationLogRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(viewMapper::toOperationLogDto)
                .collect(Collectors.toList());
    }
}
