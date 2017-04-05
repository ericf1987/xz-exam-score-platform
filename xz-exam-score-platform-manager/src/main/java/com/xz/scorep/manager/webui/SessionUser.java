package com.xz.scorep.manager.webui;

import java.util.UUID;

/**
 * 当前访问的用户
 *
 * @author yidin
 */
public class SessionUser {

    public static SessionUser newAnonymousUser() {
        return new SessionUser(UUID.randomUUID().toString(), "Anonymous");
    }

    private boolean loggedIn;

    private String userId;

    private String userName;

    public SessionUser() {
    }

    public SessionUser(String userId, String userName) {
        this.userId = userId;
        this.userName = userName;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
