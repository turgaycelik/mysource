package com.atlassian.jira.util;

import java.io.IOException;
import java.util.Enumeration;

public interface ResourceLoader
{
    public Enumeration getResources(String resourceName, Class callingClass) throws IOException;
}
