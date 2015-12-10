package com.atlassian.jira.util;

import com.atlassian.core.util.ClassLoaderUtils;

import java.io.IOException;
import java.util.Enumeration;

public class ClasspathResourceLoader implements ResourceLoader
{
    public Enumeration getResources(String resourceName, Class callingClass) throws IOException
    {
        return ClassLoaderUtils.getResources(resourceName, callingClass);
    }
}
