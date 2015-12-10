package com.atlassian.jira.event.user;

public interface UserEventType
{
    public static final int USER_SIGNUP = 0;
    public static final int USER_CREATED = 1;
    public static final int USER_FORGOTPASSWORD = 2;
    public static final int USER_FORGOTUSERNAME = 3;
    public static final int USER_CANNOTCHANGEPASSWORD = 4;
    public static final int USER_LOGIN = 5;
    public static final int USER_LOGOUT = 6;
}
