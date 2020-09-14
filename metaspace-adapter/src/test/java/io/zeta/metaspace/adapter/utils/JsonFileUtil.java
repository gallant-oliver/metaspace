package io.zeta.metaspace.adapter.utils;

import com.google.gson.Gson;
import io.zeta.metaspace.model.datasource.DataSourceInfo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;


public class JsonFileUtil {
    private final static Gson gson = new Gson();

    public static DataSourceInfo readDataSourceInfoJson(String path) {
        String json = null;
        try {
            json = Files.lines(Paths.get(path), StandardCharsets.UTF_8).collect(Collectors.joining("\n"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return gson.fromJson(json, DataSourceInfo.class);
    }
}
