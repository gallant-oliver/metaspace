package io.zeta.metaspace.web.util;
public class CustomStringUtils {

    public static String handleExcelName(String name){
        if(name==null || "".equals(name)){
            return null;
        }
        return name.replaceAll("\\*|/|\\\\|\\?|\"|:|<|>|\\[|\\]|\\|","_");
    }
}
