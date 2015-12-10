package com.atlassian.jira.security.xsrf;

import java.lang.reflect.Method;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;

import com.atlassian.jira.util.ComponentLocator;
import com.atlassian.jira.util.dbc.Null;
import com.atlassian.jira.web.action.JiraWebActionSupport;

import org.apache.commons.lang.StringUtils;

import webwork.action.Action;
import webwork.action.ActionContext;
import webwork.config.Configuration;
import webwork.config.util.ActionInfo;

/**
 * This class will check that a web-request (either WebWork action or HttpServlet) has been invoked with the correct
 * XSRF token.
 *
 * @since v4.1
 */
public class DefaultXsrfInvocationChecker implements XsrfInvocationChecker
{

    private final ComponentLocator componentLocator;
    private static final String NO_CHECK = "no-check";

    public DefaultXsrfInvocationChecker(ComponentLocator componentLocator)
    {
        this.componentLocator = componentLocator;
    }

    /**
     * Checks that the action about to be executed has been invoked within the correct XSRF parameters. This method will
     * only perform the check if the current "command" is annotated with {@link RequiresXsrfCheck}.
     *
     * @param action the {@link webwork.action.ActionSupport} in play. Cannot be null.
     * @param parameters the parameters this has been called with. Cannot be null.
     * @return false if the action failed the XSRF check.
     */
    public XsrfCheckResult checkActionInvocation(final Action action, final Map<String, ?> parameters)
    {
        Null.not("action", action);
        Null.not("parameters", parameters);

        return checkInvocation(action, parameters, getActionHttpRequest());
    }

    /**
     * Checks that the web request contains the correct XSRF parameters.
     *
     * @param httpServletRequest the {@link javax.servlet.http.HttpServletRequest} in play. Can't be null.
     * @return false if the request failed the XSRF check.
     */
    public XsrfCheckResult checkWebRequestInvocation(HttpServletRequest httpServletRequest)
    {
        @SuppressWarnings ({ "unchecked" })
        Map<String, ?> parameters = httpServletRequest.getParameterMap();

        Null.not("httpServletRequest", httpServletRequest);
        Null.not("httpServletRequest.parameters", parameters);

        return checkInvocation(null, parameters, httpServletRequest);
    }

    /**
     * Checks that an invocation has been performed within the correct XSRF parameters. <br /> Takes into account
     * actions and generic web requests.
     *
     * @param action The "action" about to be executed. If not null it is the {@link webwork.action.ActionSupport} that
     * was invoked. Otherwise, it means that a request has been invoked.
     * @param parameters the parameters this has been called with.
     * @param httpRequest the {@link javax.servlet.http.HttpServletRequest} in play.
     * @return false if the request failed the XSRF check.
     */
    private XsrfCheckResult checkInvocation(final Action action, final Map<String, ?> parameters, final HttpServletRequest httpRequest)
    {
        if (needsXsrfCheck(action, httpRequest))
        {
            String token = getXsrfToken(parameters);

            final XsrfTokenGenerator tokenGenerator = getXsrfTokenGenerator();
            final boolean passed = tokenGenerator.validateToken(httpRequest, token);
            final boolean authed = tokenGenerator.generatedByAuthenticatedUser(token);
            return createResult(true, passed, authed);
        }
        return createResult(false, true, true);
    }

    private static XsrfCheckResult createResult(final boolean required, final boolean passed, final boolean authed)
    {
        return new XsrfCheckResult()
        {
            @Override
            public boolean isRequired()
            {
                return required;
            }

            @Override
            public boolean isValid()
            {
                return passed;
            }

            @Override
            public boolean isGeneratedForAuthenticatedUser()
            {
                return authed;
            }

            @Override
            public String toString()
            {
                return String.format("required=%b valid=%b authed=%b", required, passed, authed);
            }
        };
    }

    private boolean needsXsrfCheck(final Action action, final HttpServletRequest httpRequest)
    {
        if (requestHasOptOutHeader(httpRequest))
        {
            return false;
        }

        final XsrfDefaults defaults = getXsrfDefaults();
        if (!defaults.isXsrfProtectionEnabled())
        {
            return false;
        }

        // We don't care about checking the action for annotations.
        if (action == null)
        {
            return true;
        }
        else if (action instanceof JiraWebActionSupport)
        {
            return checkActionAnnotations((JiraWebActionSupport) action);
        }

        return false;
    }

    private boolean requestHasOptOutHeader(final HttpServletRequest httpRequest)
    {
        if (httpRequest != null)
        {
            String tokenValue = httpRequest.getHeader(X_ATLASSIAN_TOKEN);
            if (StringUtils.isNotBlank(tokenValue) && NO_CHECK.equals(tokenValue.trim().toLowerCase()))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * We work out what method we are going to end up invoking with this action invocation, and then check that method
     * for a specific {@link RequiresXsrfCheck} annotation.
     * <p/>
     * if it doesnt have it , then we return false otherwise we return true.
     *
     * @param action the action in play
     * @return true if the annotation is present
     */
    private boolean checkActionAnnotations(@Nonnull final JiraWebActionSupport action)
    {
        String command = action.getCommandName();
        if (StringUtils.isBlank(command))
        {
            command = "execute";
        }
        Method method = getMethod(action, command);
        if (method != null)
        {
            return method.isAnnotationPresent(RequiresXsrfCheck.class);
        }
        return false;
    }

    private Method getMethod(final JiraWebActionSupport action, final String command)
    {
        final String methodName = "do" + StringUtils.capitalize(command);
        return getMethod(action.getClass(), methodName);
    }

    /**
     * Finds the declared method on the provided class. If the method is not declared on the class, we search
     * recursively up the inheritance hierarchy, stopping at {@link JiraWebActionSupport}.
     *
     * @param clazz the class
     * @param methodName the method name to find
     * @return the method on the class; null if it could not be found.
     */
    private Method getMethod(final Class clazz, final String methodName)
    {
        try
        {
            return clazz.getDeclaredMethod(methodName);
        }
        catch (NoSuchMethodException e)
        {
            if (clazz.equals(JiraWebActionSupport.class))
            {
                return null;
            }
            else
            {
                return getMethod(clazz.getSuperclass(), methodName);
            }
        }
    }

    HttpServletRequest getActionHttpRequest()
    {
        return ActionContext.getRequest();
    }

    /**
     * Returns ActionInfo config objects for action aliases
     *
     * @param magicKey the action alias key
     *
     * @return {@link webwork.config.util.ActionInfo} for that action alias
     *
     * @throws IllegalAccessException if the key cant be found (stupid Webwork)
     */
    ActionInfo getActionInfo(final String magicKey) {
        return (ActionInfo) Configuration.get(magicKey);
    }

    private String getXsrfToken(final Map<String, ?> parameters)
    {
        Object mapObj = parameters.get(XsrfTokenGenerator.TOKEN_WEB_PARAMETER_KEY);
        if (mapObj instanceof String[])
        {
            String[] tokenArr = (String[]) mapObj;
            if (tokenArr.length > 0)
            {
                return tokenArr[0];
            }

        }
        return null;
    }

    private XsrfDefaults getXsrfDefaults()
    {
        return componentLocator.getComponentInstanceOfType(XsrfDefaults.class);
    }

    private XsrfTokenGenerator getXsrfTokenGenerator()
    {
        return componentLocator.getComponentInstanceOfType(XsrfTokenGenerator.class);
    }

}
