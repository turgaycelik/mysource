package com.atlassian.jira.web.util;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.ExceptionInterpreterUtil;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.system.ExtendedSystemInfoUtils;
import com.atlassian.jira.util.system.ExtendedSystemInfoUtilsImpl;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.annotation.Nullable;
import javax.servlet.ServletException;

public class InternalServerErrorExceptionDataSource
{

    private static final Logger LOG = LoggerFactory.getLogger(InternalServerErrorExceptionDataSource.class);

    private final Throwable exception;
    private final ExtendedSystemInfoUtils extendedSystemInfoUtils;
    private String stacktrace = null;
    private String rootCause = null;

    public InternalServerErrorExceptionDataSource(final Throwable exception, @Nullable final ExtendedSystemInfoUtils extendedSystemInfoUtils) {
        this.exception = exception;
        this.extendedSystemInfoUtils = extendedSystemInfoUtils;
        parseException();
    }

    protected void parseException(){
        if (exception != null)
        {
            Throwable cause = exception;
            if (exception instanceof ServletException)
            {
                Throwable rootCause = ((ServletException) exception).getRootCause();
                if (rootCause != null)
                    cause = rootCause;
            }
            //log exception to the log files, so that it gets captured somewhere.
            //log.error("Exception caught in 500 page " + cause.getMessage(), cause);
            rootCause = cause.toString();
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            cause.printStackTrace(pw);
            stacktrace = sw.toString();
        }
    }

    public String getInterpretedMessage(){
        if(extendedSystemInfoUtils != null){
            String execute = ExceptionInterpreterUtil.execute(extendedSystemInfoUtils, getStacktrace());
            return StringUtils.defaultString(execute);
        }
        return "";
    }


    public String getStacktrace()
    {
        return StringUtils.defaultString(stacktrace);
    }

    public String getRootCause()
    {
        return StringUtils.defaultString(rootCause);
    }
}
