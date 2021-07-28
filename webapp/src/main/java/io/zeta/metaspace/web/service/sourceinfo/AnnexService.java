package io.zeta.metaspace.web.service.sourceinfo;

import io.zeta.metaspace.model.sourceinfo.Annex;
import io.zeta.metaspace.web.dao.sourceinfo.AnnexDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AnnexService {
    @Autowired
    private AnnexDAO annexDAO;

    public int saveRecord(Annex item){
        return annexDAO.save(item);
    }

    public Annex findByAnnexId(String annexId){
        return annexDAO.selectByAnnexId(annexId);
    }

}
