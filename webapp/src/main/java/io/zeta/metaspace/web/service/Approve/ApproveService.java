package io.zeta.metaspace.web.service.Approve;


import io.zeta.metaspace.web.dao.ApproveDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ApproveService implements ApproveServiceImp{

    @Autowired
    private ApproveDAO approveDao;


}
