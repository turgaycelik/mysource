package com.atlassian.jira.util;

import com.atlassian.jira.util.system.ExtendedSystemInfoUtils;

/**
 */
public class ExceptionInterpreter
{
    /**
     * The sibling of this interceptor
     */
    private ExceptionInterpreter sibling;

    //~ Methods ================================================================

    public void setSibling(ExceptionInterpreter val)
    {
        this.sibling = val;
    }

    public ExceptionInterpreter getSibling()
    {
        return sibling;
    }

    public void append(ExceptionInterpreter val)
    {
        last().setSibling(val);
    }

    public String invoke(ExtendedSystemInfoUtils extendedSystemInfoUtils, String exceptionMessage) throws Exception
    {

        // Do the work of the invoke
        String result = handleInvoke(extendedSystemInfoUtils, exceptionMessage);

        // And do the next sibling, as necessary
        if (result == null && sibling != null)
        {
            result = sibling.invoke(extendedSystemInfoUtils, exceptionMessage);

        }

        return result;
    }

    public ExceptionInterpreter last()
    {
        ExceptionInterpreter result = this;
        if (sibling != null)
        {
            do
            {
                result = result.sibling;
            }
            while (result.sibling != null);
        }

        return result;
    }

    protected String handleInvoke(ExtendedSystemInfoUtils extendedSystemInfoUtils, String exceptionMessage)
            throws Exception
    {
        return null;
    }

}
