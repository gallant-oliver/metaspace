package io.zeta.metaspace.model.usergroup.result;

import io.zeta.metaspace.model.user.User;
import lombok.Data;

import java.util.List;

/**
 * @author huangrongwen
 * @Description: 用户组以及其用户邮箱
 * @date 2022/7/2615:16
 */
@Data
public class GroupAccountDTO {
    /**
     * 用户组id
     */
    private String groupId;
    /**
     * 用户组名字
     */
    private String groupName;
    /**
     * 用户组下的用户的account
     */
    private List<User> userList;
}
