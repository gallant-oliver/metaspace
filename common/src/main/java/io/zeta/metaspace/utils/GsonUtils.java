package io.zeta.metaspace.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonUtils {

    private static class SingletonHolder {
        private static final Gson INSTANCE = getDefaultBuilder().create();

        private static GsonBuilder getDefaultBuilder() {
            return new GsonBuilder();
        }
    }

    public static Gson getInstance() {
        return SingletonHolder.INSTANCE;
    }
}
