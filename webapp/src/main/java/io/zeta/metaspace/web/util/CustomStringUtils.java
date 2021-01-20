package io.zeta.metaspace.web.util;
public class CustomStringUtils {

    public static String handleExcelName(String name){
        return name.replaceAll("\\*|/|\\\\|\\?|\"|:|<|>|\\[|\\]|\\|","_");
    }
}
