package io.zeta.metaspace.model.desensitization;

import lombok.Data;

import java.util.List;

@Data
public class DesensitizationAlgorithmTest {

    private DesensitizationAlgorithm type;
    private List<String> params;
    private String field;
}
