package com.atlassian.jira.issue.customfields.persistence;

public final class PersistenceFieldType
{
    public static final PersistenceFieldType TYPE_LIMITED_TEXT = new PersistenceFieldType();
    public static final PersistenceFieldType TYPE_UNLIMITED_TEXT = new PersistenceFieldType();
    public static final PersistenceFieldType TYPE_DATE = new PersistenceFieldType();
    public static final PersistenceFieldType TYPE_DECIMAL = new PersistenceFieldType();

    private PersistenceFieldType()
    {
    }

    public String toString()
    {
        return "PersistenceFieldType";
    }
}
