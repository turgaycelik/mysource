package com.atlassian.jira.issue.fields.rest;

/**
 * @since v5.0
 */
// TODO JRADEV-7099 should be in API??
public enum StandardOperation
{
    ADD("add"), SET("set"), REMOVE("remove"), EDIT("edit");

    private String name;

    StandardOperation(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }
}