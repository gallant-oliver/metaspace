package io.zeta.metaspace.model.dto;

import lombok.Data;

@Data
public class MailRequest {
    private String content;
    private String subject;
    private String[] toList;
}
