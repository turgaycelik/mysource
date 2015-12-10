package com.atlassian.jira.config.database;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import java.lang.reflect.Field;

/**
 * A toString builder that uses reflection and ignores fields called "password".
 *
 * @since v5.1
 */
class ToStringBuilderExcludingPassword extends ReflectionToStringBuilder
{
    ToStringBuilderExcludingPassword(Object o)
    {
        super(o);
    }

    @Override
    protected boolean accept(Field f)
    {
        return super.accept(f) && !f.getName().equals("password");
    }

    public static String toString(Object o)
    {
        return new ToStringBuilderExcludingPassword(o).toString();
    }
}
