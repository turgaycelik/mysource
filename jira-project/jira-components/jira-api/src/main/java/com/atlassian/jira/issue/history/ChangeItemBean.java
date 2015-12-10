/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.history;

import com.atlassian.annotations.PublicApi;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.sql.Timestamp;

/**
 * A very simple bean to hold change items.
 */
@PublicApi
public class ChangeItemBean
{
    public static final String STATIC_FIELD = "jira";
    public static final String CUSTOM_FIELD = "custom";

    private Timestamp created;
    private String fieldType;
    private String field;
    private String from;
    private String fromString;
    private String to;
    private String toString;

    public ChangeItemBean()
    {
    }

    public ChangeItemBean(String fieldType, String field, String from, String to)
    {
        this(fieldType, field, from, null, to, null);
    }

    public ChangeItemBean(String fieldType, String field, String from, String fromString, String to, String toString)
    {
        this.fieldType = fieldType;
        this.field = field;
        this.from = from;
        this.fromString = fromString;
        this.to = to;
        this.toString = toString;
    }

    public ChangeItemBean(String fieldType, String field, String from, String fromString, String to, String toString, Timestamp created)
    {
        this(fieldType, field, from, fromString, to, toString);
        this.created = created;
    }

    public String getFieldType()
    {
        return fieldType;
    }

    public void setFieldType(String fieldType)
    {
        this.fieldType = fieldType;
    }

    public String getField()
    {
        return field;
    }

    public void setField(String field)
    {
        this.field = field;
    }

    public String getFrom()
    {
        return from;
    }

    public void setFrom(String from)
    {
        this.from = from;
    }

    public String getFromString()
    {
        return fromString;
    }

    public void setFromString(String fromString)
    {
        this.fromString = fromString;
    }

    public String getTo()
    {
        return to;
    }

    public void setTo(String to)
    {
        this.to = to;
    }

    public String getToString()
    {
        return toString;
    }

    public void setToString(String toString)
    {
        this.toString = toString;
    }

    public Timestamp getCreated()
    {
        return created;
    }

    public void setCreated(final Timestamp created)
    {
        this.created = created;
    }

    public String toString()
    {
        return new ToStringBuilder(this).
                append("fieldType", fieldType).
                append("field", field).
                append("from", from).
                append("fromString", fromString).
                append("to", to).
                append("toString", toString).
                append("created", created).toString();
    }

    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (!(o instanceof ChangeItemBean))
            return false;

        ChangeItemBean changeItemBean = (ChangeItemBean) o;

        if (fieldType != null ? !fieldType.equals(changeItemBean.fieldType) : changeItemBean.fieldType != null)
            return false;
        if (field != null ? !field.equals(changeItemBean.field) : changeItemBean.field != null)
            return false;
        if (from != null ? !from.equals(changeItemBean.from) : changeItemBean.from != null)
            return false;
        if (fromString != null ? !fromString.equals(changeItemBean.fromString) : changeItemBean.fromString != null)
            return false;
        if (to != null ? !to.equals(changeItemBean.to) : changeItemBean.to != null)
            return false;
        if (toString != null ? !toString.equals(changeItemBean.toString) : changeItemBean.toString != null)
            return false;
        if (created != null ? !created.equals(changeItemBean.created) : changeItemBean.created != null)
            return false;

        return true;
    }

    public int hashCode()
    {
        int result;
        result = (fieldType != null ? fieldType.hashCode() : 0);
        result = 29 * result + (field != null ? field.hashCode() : 0);
        result = 29 * result + (from != null ? from.hashCode() : 0);
        result = 29 * result + (fromString != null ? fromString.hashCode() : 0);
        result = 29 * result + (to != null ? to.hashCode() : 0);
        result = 29 * result + (toString != null ? toString.hashCode() : 0);
        result = 29 * result + (created != null ? created.hashCode() : 0);
        return result;
    }
}
