package io.zeta.metaspace.web.service.sourceinfo;

import io.zeta.metaspace.model.sourceinfo.Annex;
import io.zeta.metaspace.web.dao.sourceinfo.AnnexDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Service
public class AnnexService {
    @Autowired
    private AnnexDAO annexDAO;

    @Transactional
    public int saveRecord(Annex item){
        try{
            annexDAO.save(item);
            return 1;
        }catch (Exception e){
            throw e;
        }
    }

    public Annex findByAnnexId(String annexId){
        return annexDAO.selectByAnnexId(annexId);
    }

}
