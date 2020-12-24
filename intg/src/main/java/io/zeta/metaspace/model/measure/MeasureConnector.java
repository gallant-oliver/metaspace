package io.zeta.metaspace.model.measure;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class MeasureConnector {
    public String type = "JDBC";
    public Config config;

    @Data
    public static class Config{
        public String driver;
        public String url;
        public String user;
        public String password;
        public String database;
        public String tablename;
        @SerializedName("table.name")
        public String hiveTableName;
        public String[] fields;
        public String[] orderFields;

        public Config(String driver, String url, String user, String password, String database, String tablename, String[] fields, String[] orderFields) {
            this.driver = driver;
            this.url = url;
            this.user = user;
            this.password = password;
            this.database = database;
            this.tablename = tablename;
            this.fields = fields;
            this.orderFields = orderFields;
        }

        public Config(String database, String hiveTableName) {
            this.database = database;
            this.hiveTableName = hiveTableName;
        }
    }

    public MeasureConnector(Config config) {
        this.config = config;
    }
}
