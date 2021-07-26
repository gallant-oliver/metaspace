package io.zeta.metaspace.model.sourceinfo;

import io.zeta.metaspace.model.enums.SubmitType;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class CreateRequest {


    private String approveGroupId;

    @NotNull(message = "提交类型不可为空")
    private SubmitType submitType;

    private DatabaseInfo databaseInfo;
}
