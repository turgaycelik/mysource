package com.atlassian.jira.mock.servlet;

import java.security.Principal;

/**
 */
public class MockPrincipal implements Principal
{
    private final String name;

    public MockPrincipal()
    {
        this.name = "Principal";
    }

    public MockPrincipal(final String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final MockPrincipal that = (MockPrincipal) o;

        if (!name.equals(that.name))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        return name.hashCode();
    }
}
