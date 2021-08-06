package io.zeta.metaspace.model.source;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class Base64Info {
    private String prefix;
    private String value;

    public Base64Info getInstance(String fileType,String base64String){
        this.prefix = getBase64StringByPrefix(fileType);
        this.value = base64String;

        return this;
    }
    public String getBase64StringByPrefix(String fileType){
        return Base64Format.getBase64StringByPrefix(fileType);
    }
}

class Base64Format{
    private static Map<String,String> map = new HashMap<String,String>(){{
        put("doc","data:application/msword;base64,");
        put("docx","data:application/vnd.openxmlformats-officedocument.wordprocessingml.document;base64,");
        put("xls","data:application/vnd.ms-excel;base64,");
        put("xlsx","data:application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;base64,");
        put("pdf","data:application/pdf;base64,");
        put("ppt","data:application/vnd.ms-powerpoint;base64,");
        put("pptx","data:application/vnd.openxmlformats-officedocument.presentationml.presentation;base64,");
        put("png","data:image/png;base64,");
        put("jpg","data:image/jpeg;base64,");
        put("gif","data:image/gif;base64,");
        put("svg","data:image/svg+xml;base64,");
        put("ico","data:image/x-icon;base64,");
    }};

    public static  String getBase64StringByPrefix(String prefix){
        return map.getOrDefault(prefix,"");
    }
}
