package com.atlassian.jira.action.admin.export;

import org.ofbiz.core.entity.GenericValue;

import java.io.PrintWriter;

public interface EntityXmlWriter
{
    public void writeXmlText(GenericValue entity, PrintWriter writer);

}
