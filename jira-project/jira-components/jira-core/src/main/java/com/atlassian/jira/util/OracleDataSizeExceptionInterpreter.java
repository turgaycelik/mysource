package com.atlassian.jira.util;

import com.atlassian.jira.util.system.ExtendedSystemInfoUtils;
import com.atlassian.jira.web.util.ExternalLinkUtilImpl;

/**
 * This implementation of ExceptionInterpreter shows links to confluence howto's on how
 * to fix the oracle large string problem. This looks for the oracle error code
 * ORA-01461 as evidence that we have hit the problem.
 */
public class OracleDataSizeExceptionInterpreter extends ExceptionInterpreter
{
    private static final String EXCEPTION_MSG = "ORA-01461";
    private static final String INTERPRETED_MSG = "It is likely that you have encountered a problem that Jira has with the way Oracle handles large strings. This is a known issue and there is an easy work-around. You can find the documentation to fix this problem here:<br><a href='" + ExternalLinkUtilImpl.getInstance().getProperty("external.link.jira.confluence.doc.oracle.large.text") + "'>" + ExternalLinkUtilImpl.getInstance().getProperty("external.link.jira.confluence.doc.oracle.large.text") + "</a><br><a href='" + ExternalLinkUtilImpl.getInstance().getProperty("external.link.jira.confluence.doc.oracle.workflow.store.on.disk") + "'>" + ExternalLinkUtilImpl.getInstance().getProperty("external.link.jira.confluence.doc.oracle.workflow.store.on.disk") + "</a>";

    protected String handleInvoke(ExtendedSystemInfoUtils extendedSystemInfoUtils, String exceptionMessage) throws Exception
    {
        if ("oracle".equals(extendedSystemInfoUtils.getSystemInfoUtils().getDatabaseType())
                && exceptionMessage != null && (exceptionMessage.indexOf(EXCEPTION_MSG) != -1))
        {
            return INTERPRETED_MSG;
        }
        return null;
    }
}
