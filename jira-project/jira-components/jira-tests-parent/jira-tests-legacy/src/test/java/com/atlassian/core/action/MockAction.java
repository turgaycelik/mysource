package com.atlassian.core.action;

import webwork.action.ActionSupport;

public class MockAction extends ActionSupport
{
    String foo;

    public String getFoo()
    {
        return foo;
    }

    public void setFoo(String foo)
    {
        this.foo = foo;
    }

}