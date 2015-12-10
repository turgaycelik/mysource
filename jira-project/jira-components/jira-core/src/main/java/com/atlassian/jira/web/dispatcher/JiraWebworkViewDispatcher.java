package com.atlassian.jira.web.dispatcher;

import com.atlassian.fugue.Option;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.webwork.actions.ActionConfiguration;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.seraph.util.RedirectUtils;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webwork.action.Action;
import webwork.config.util.ActionInfo;
import webwork.dispatcher.ActionResult;
import webwork.util.ServletValueStack;

import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static java.lang.String.format;

/**
 * The logic for dispatching a particular view for an action is encapsulated in this class
 *
 * @since v6.0
 */
class JiraWebworkViewDispatcher
{

    private static final Logger log = LoggerFactory.getLogger(JiraWebworkViewDispatcher.class);
    public static final Supplier<String> ASSERTION_CANT_BE_NONE = new Supplier<String>()
    {
        @Override
        public String get()
        {
            return "This cannot not be Option.none() at this stage";
        }
    };
    private final JiraSoyViewDispatcher soyViewDispatcher = new JiraSoyViewDispatcher();
    private final JiraVelocityViewDispatcher velocityViewDispatcher = new JiraVelocityViewDispatcher();

    void dispatchView(final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse, final boolean doCleanup, final ActionResult ar, final String actionAlias)
            throws ServletException, IOException
    {
        if (permissionViolationCheckHandled(ar, httpServletRequest, httpServletResponse, doCleanup))
        {
            return;
        }

        String actionView = ar.getView().toString();
        String actionResult = ar.getResult();

        log.debug("Action Result {} --> View Name: {}", actionResult, actionView);

        Option<ActionInfo.ViewInfo> viewInfo = determineViewInfo(ar, actionAlias);

        try
        {
            if (isSoyView(viewInfo))
            {
                dispatchViaSoyView(httpServletResponse, ar, viewInfo);
            }
            else if (isVelocityView(viewInfo))
            {
                dispatchViaVelocityView(httpServletRequest, httpServletResponse, ar, viewInfo);
            }
            else
            {
                dispatchViaHttpView(httpServletRequest, httpServletResponse, ar, viewInfo);
            }

        }
        finally
        {
            cleanUpStack(httpServletRequest, doCleanup);

        }
    }

    private Option<ActionInfo.ViewInfo> determineViewInfo(final ActionResult ar, final String actionAlias)
    {
        Option<ActionInfo.ViewInfo> viewInfo = getViewInfo(actionAlias, ar.getResult());
        if (viewInfo.isEmpty() && Action.INPUT.equals(ar.getResult()))
        {
            //
            // Webwork ConfigurationViewMappping class has an annoying edge case where it equates Action.INPUT ==> Action.ERROR
            // when the Action.INPUT view is undefined.  So we need to do something similar so that we find the right ActionInfo for that case
            // otherwise we will will not resolve to the appropriate view
            //
            viewInfo = getViewInfo(actionAlias, Action.ERROR);
        }
        return viewInfo;
    }

    private void dispatchViaHttpView(final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse, ActionResult actionResult, final Option<ActionInfo.ViewInfo> viewInfo)
            throws ServletException, IOException
    {
        String view = actionResult.getView().toString();

        forwardToHttpView(httpServletRequest, httpServletResponse, view, getRequestDispatcher(httpServletRequest, view));
    }

    private void dispatchViaVelocityView(final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse, final ActionResult actionResult, final Option<ActionInfo.ViewInfo> viewInfoOption)
            throws ServletException, IOException
    {
        velocityViewDispatcher.dispatch(httpServletRequest, httpServletResponse, actionResult, viewInfoOption.get());
    }

    private void forwardToHttpView(final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse, final String view, final RequestDispatcher dispatcher)
            throws ServletException, IOException
    {
        // If we're included or if content has already been sent down the pipe, then include the view
        // Otherwise do forward
        // This allow the page to, for example, set content type
        if (!httpServletResponse.isCommitted() && httpServletRequest.getAttribute("javax.servlet.include.servlet_path") == null)
        {
            httpServletRequest.setAttribute("webwork.view_uri", view);
            httpServletRequest.setAttribute("webwork.request_uri", httpServletRequest.getRequestURI());

            dispatcher.forward(httpServletRequest, httpServletResponse);
        }
        else
        {
            dispatcher.include(httpServletRequest, httpServletResponse);
        }
    }

    private RequestDispatcher getRequestDispatcher(final HttpServletRequest httpServletRequest, final String view)
            throws ServletException
    {
        final RequestDispatcher dispatcher;
        dispatcher = httpServletRequest.getRequestDispatcher(view);

        if (dispatcher == null)
        {
            throw new ServletException(format("No presentation file with name: '%s' found!", view));
        }
        return dispatcher;
    }

    private void dispatchViaSoyView(final HttpServletResponse httpServletResponse, final ActionResult ar, Option<ActionInfo.ViewInfo> viewInfo)
            throws ServletException, IOException
    {
        soyViewDispatcher.dispatch(httpServletResponse, ar, viewInfo.getOrError(ASSERTION_CANT_BE_NONE));
    }

    private boolean isSoyView(Option<ActionInfo.ViewInfo> viewInfo)
    {
        return "soy".equals(getAttrValue(viewInfo, "type").getOrNull());
    }

    private boolean isVelocityView(final Option<ActionInfo.ViewInfo> viewInfo)
    {
        boolean isVelocity = false;
        if (viewInfo.isDefined())
        {
            isVelocity = "velocity".equals(getAttrValue(viewInfo, "type").getOrNull());
            isVelocity |= viewInfo.get().getViewValue().endsWith(".vm");
        }
        return isVelocity;
    }

    private void cleanUpStack(HttpServletRequest httpServletRequest, boolean doCleanup)
    {
        if (doCleanup)
        {
            // Get last action from stack and and store it in request attribute STACK_HEAD
            // It is then popped from the stack.
            httpServletRequest.setAttribute(JiraWebworkActionDispatcher.STACK_HEAD, ServletValueStack.getStack(httpServletRequest).popValue());
        }
    }

    private boolean permissionViolationCheckHandled(final ActionResult ar, final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse, final boolean doCleanup)
    {
        // special hard coded support for actions returning permission violation that require a login
        if (!isUserLoggedIn())
        {
            final String result = ar.getResult();
            if ("permissionviolation".equals(result) || "securitybreach".equals(result))
            {
                final String loginUrl = RedirectUtils.getLoginUrl(httpServletRequest);
                try
                {
                    httpServletResponse.sendRedirect(loginUrl);
                }
                catch (IOException e)
                {
                    log.error("Unable to redirect permission violation to " + loginUrl);
                }
                finally
                {
                    cleanUpStack(httpServletRequest, doCleanup);
                }
                return true;
            }
        }
        return false;
    }

    private Option<ActionInfo.ViewInfo> getViewInfo(final String actionAlias, final String actionResult)
    {
        ActionConfiguration.Entry actionConfiguration = getActionConfiguration(actionAlias);

        ActionInfo actionInfo = actionConfiguration.getActionInfo();
        for (ActionInfo.ViewInfo viewInfo : actionInfo.getViews())
        {
            if (viewInfo.getViewName().equals(actionResult))
            {
                return Option.some(viewInfo);
            }
        }
        return Option.none();
    }

    private Option<String> getAttrValue(Option<ActionInfo.ViewInfo> viewInfo, final String attrName)
    {
        return viewInfo.map(new Function<ActionInfo.ViewInfo, String>()
        {
            @Override
            public String apply(final ActionInfo.ViewInfo input)
            {
                return input.getAttributeValue(attrName);
            }
        });
    }

    /**
     * Returns true if the plugin source is from JIRA core
     *
     * @param pluginSource the source to check
     * @return true if the plugin source is from JIRA core
     */
    public static boolean isFromCore(final String pluginSource)
    {
        return pluginSource == null
                || pluginSource.endsWith("WEB-INF/classes/actions.xml") // standard run
                || pluginSource.endsWith("jira-core/target/classes/actions.xml"); // when using JRebel
    }


    @VisibleForTesting
    boolean isUserLoggedIn()
    {
        return ComponentAccessor.getComponent(JiraAuthenticationContext.class).getUser() != null;
    }

    @VisibleForTesting
    ActionConfiguration.Entry getActionConfiguration(String actionAlias)
    {
        return ComponentAccessor.getComponent(ActionConfiguration.class).getActionCommand(actionAlias);
    }

}
