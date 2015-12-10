package com.atlassian.jira.web;

import com.atlassian.core.util.StringUtils;
import com.atlassian.jira.component.ComponentAccessor;
import webwork.multipart.PellMultiPartRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Wrapper class so that we can parse and replace special characters in the request params.
 */
public class JiraMultipartRequestWrapper extends PellMultiPartRequest
{
    Map parameterValueCache = new HashMap();
    Map parameterMap = null;

    /**
     * @param maxSize        maximum size post allowed
     * @param saveDir        the directory to save off the file
     * @param servletRequest the request containing the multipart
     */
    public JiraMultipartRequestWrapper(HttpServletRequest servletRequest, String saveDir, int maxSize)
            throws IOException
    {
        super(servletRequest, saveDir, maxSize);
    }

    public String getParameter(String name)
    {
        return parseParameterValue(super.getParameter(name));
    }

    public String[] getParameterValues(String name)
    {
        return this.retrieveParameterValues(name);
    }

    private String parseParameterValue(String value)
    {
        String [] retVal = this.retrieveParameterValues(value);
        return (retVal != null && retVal.length >= 1) ? retVal[0] : null;
    }

    private String [] retrieveParameterValues(String string)
    {
        if (string == null)
        {
            return null;
        }
        String[] returnValue = (String[]) parameterValueCache.get(string);

        //if we haven't yet cached this - look it up.
        if (returnValue == null)
        {
            String[] parameterValues = super.getParameterValues(string);
            //values could be null - don't bother converting them
            if (parameterValues == null)
            {
                return null;
            }

            for (int i = 0; i < parameterValues.length; i++)
            {
                parameterValues[i] = StringUtils.escapeCP1252(parameterValues[i], getJiraEncoding());
            }
            parameterValueCache.put(string, parameterValues);
            returnValue = parameterValues;
        }
        return returnValue;
    }

    private String getJiraEncoding()
    {
        return ComponentAccessor.getApplicationProperties().getEncoding();
    }

}
