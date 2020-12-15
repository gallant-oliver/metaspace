package io.zeta.metaspace.model.ip.restriction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiIpRestriction {
    private IpRestrictionType type;
    private List<String> ipRestrictionIds;
    private List<String> ipRestrictionNames;

}
