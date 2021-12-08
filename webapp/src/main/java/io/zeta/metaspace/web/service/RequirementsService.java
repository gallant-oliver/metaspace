package io.zeta.metaspace.web.service;

import io.zeta.metaspace.web.dao.requirements.RequirementsApiMapper;
import io.zeta.metaspace.web.dao.requirements.RequirementsMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RequirementsService {
    
    @Autowired
    private RequirementsMapper requirementsMapper;
    @Autowired
    private RequirementsApiMapper requirementsApiMapper;
    
}
