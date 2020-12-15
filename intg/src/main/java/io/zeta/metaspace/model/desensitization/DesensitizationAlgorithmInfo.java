package io.zeta.metaspace.model.desensitization;

import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class DesensitizationAlgorithmInfo {

    private String type;
    private List<Algorithm> algorithms;

    @Data
    public static class Algorithm {
        private String name;
        private String type;
        private String[] paramNames;

        public Algorithm(DesensitizationAlgorithm desensitizationAlgorithm) {
            this.name = desensitizationAlgorithm.getName();
            this.type = desensitizationAlgorithm.toString();
            this.paramNames = desensitizationAlgorithm.getParamNames();
        }
    }

    public DesensitizationAlgorithmInfo(String type, List<DesensitizationAlgorithm> algorithms) {
        this.type = type;
        this.algorithms = algorithms.stream().map(Algorithm::new).collect(Collectors.toList());
    }
}
