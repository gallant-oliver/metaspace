package org.apache.atlas.model.notification;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.instance.debezium.RdbmsMessage;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.regex.Pattern;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

@JsonAutoDetect(getterVisibility=PUBLIC_ONLY, setterVisibility=PUBLIC_ONLY, fieldVisibility=NONE)
@JsonIgnoreProperties(ignoreUnknown=true)
public class RdbmsNotification extends  Notification implements Serializable {
    private static final long serialVersionUID = 1L;
    public enum RdbmsNotificationType {
        /**
         * 创建库，表（视图，列，主键，索引，外键）
         */
        NEW("n"),
        /**
         * 更新库，表（视图，列，主键，索引，外键）
         */
        ALTER("a"),
        /**
         * 删除库，表（视图，列，主键，索引，外键）
         */
        DROP("drop"),
        /**
         * 插入数据
         */
        CREATE("c"),
        /**
         * 更新数据
         */
        UPDATE("u"),
        /**
         * 删除数据
         */
        DELETE("d");
        RdbmsNotificationType(String code){
            this.code = code;
        }
        private String code;

        private String getCode(){
            return this.code;
        }

        public static RdbmsNotificationType getTypeByCode(String code){
            if(StringUtils.isBlank(code)){
                throw new AtlasBaseException("rdbms操作类型代号不能为空");
            }
            for (RdbmsNotificationType type: RdbmsNotificationType.values()) {
                if(type.getCode().equalsIgnoreCase(code)){
                    return type;
                }
            }
            throw new AtlasBaseException("不识别的rdbms操作类型，code = " + code);
        }

    }



    private RdbmsNotificationType type;

    private RdbmsMessage rdbmsMessage;

    public RdbmsNotification() {
        super();
    }
    public RdbmsNotification(RdbmsNotificationType type) {
        super();
        this.type = type;
    }
    public RdbmsNotification(RdbmsNotificationType type, String user) {
        super(user);
        this.type = type;
    }

    public RdbmsNotificationType getType() {
        return type;
    }

    public void setType(RdbmsNotificationType type) {
        this.type = type;
    }

    public RdbmsMessage getRdbmsMessage() {
        return rdbmsMessage;
    }

    public void setRdbmsMessage(RdbmsMessage rdbmsMessage) {
        this.rdbmsMessage = rdbmsMessage;
    }

    @Override
    public String getTypeName() {
        if(type != null){
            return type.name();
        }
        return "";
    }

    public static RdbmsNotificationType getTypeBySql(String sql) {
        if(StringUtils.isBlank(sql)){
            throw new AtlasBaseException("sql语句不能为空");
        }
        String firstWord = sql.trim().substring(0, sql.indexOf(" ")).toLowerCase();
        switch (firstWord){
            case "create" :
                return RdbmsNotificationType.NEW;
            case "drop" :
                return RdbmsNotificationType.DROP;
            case "truncate" :
                return RdbmsNotificationType.DELETE;
            case "alter" :
                return RdbmsNotificationType.ALTER;
        }
        throw new AtlasBaseException("不识别的rdbms操作,sql = " + sql);
    }
}
