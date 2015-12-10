/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly;

import org.apache.commons.jelly.DynaBeanTagSupport;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.XMLOutput;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

public class NotImplementedTag extends DynaBeanTagSupport
{
    private static final transient Logger log = Logger.getLogger(NotImplementedTag.class);

    public NotImplementedTag()
    {
    }

    public void doTag(XMLOutput output) throws JellyTagException
    {
        log.debug("NotImplementedTag.doTag");
        try
        {
            WebWorkAdaptor.writeErrorToXmlOutput(output, new StringBuffer("Tag Not Implemented Yet"), "NotImplemented", this);
        }
        catch (SAXException e)
        {
            log.error(e, e);
            throw new JellyTagException(e);
        }
    }

    public String[] getRequiredProperties()
    {
        return new String[0]; //To change body of implemented methods use Options | File Templates.
    }

    public String[] getRequiredContextVariablesAfter()
    {
        return new String[0]; //To change body of implemented methods use Options | File Templates.
    }

    public String[] getRequiredContextVariables()
    {
        return new String[0]; //To change body of implemented methods use Options | File Templates.
    }
}
