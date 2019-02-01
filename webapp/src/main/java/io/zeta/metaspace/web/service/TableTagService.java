package io.zeta.metaspace.web.service;

import org.springframework.stereotype.Service;
import java.util.UUID;
@Service
public class TableTagService {

    public void addTag(String tagName){
        String tagId = UUID.randomUUID().toString();

    }
}
