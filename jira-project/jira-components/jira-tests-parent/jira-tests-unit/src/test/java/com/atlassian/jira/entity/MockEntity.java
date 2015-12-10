package com.atlassian.jira.entity;


public class MockEntity {

    private final long id;
    private final String field;

    public MockEntity(long id, String field)
    {
        this.id = id;
        this.field = field;
    }

    public long getId() {
        return id;
    }

    public String getField() {
        return field;
    }
}
