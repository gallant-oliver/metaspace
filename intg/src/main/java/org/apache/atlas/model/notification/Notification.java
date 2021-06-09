package org.apache.atlas.model.notification;

import org.apache.commons.lang.StringUtils;

import java.io.Serializable;

public class Notification implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String UNKNOW_USER = "UNKNOWN";

    protected String user;

    public Notification() {
    }

    public Notification(String user) {
        this.user = user;
    }

    public String getUser() {
        if (StringUtils.isEmpty(user)) {
            return UNKNOW_USER;
        }
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getTypeName() {
        return "";
    }

    public void normalize() {
    }
}