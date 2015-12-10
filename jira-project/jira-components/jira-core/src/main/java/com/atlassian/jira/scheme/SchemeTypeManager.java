package com.atlassian.jira.scheme;

import java.util.Map;

public interface SchemeTypeManager<T>
{
    T getSchemeType(String id);

    Map<String, T> getSchemeTypes();

    void setSchemeTypes(Map<String, T> schemeType);

    Map<String, T> getTypes();
}
