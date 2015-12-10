/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly.tag;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.jelly.JiraDynaBeanTagSupport;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.XMLOutput;
import org.apache.log4j.Logger;

import java.lang.reflect.Method;

public class LoadManager extends JiraDynaBeanTagSupport
{
    private static final transient Logger log = Logger.getLogger(LoadManager.class);

    public LoadManager()
    {
        super();
    }

    public void doTag(XMLOutput xmlOutput) throws JellyTagException
    {
        try
        {
            String variableName = (String) getProperties().get("var");
            String managerGetFunction = (String) getProperties().get("manager");
            Method method = ManagerFactory.class.getMethod("get" + managerGetFunction);
            Object manager = method.invoke(this);
            getContext().setVariable(variableName, manager);
        }
        catch (Exception e)
        {
            log.error(e, e);
            throw new JellyTagException(e);
        }
    }
}
