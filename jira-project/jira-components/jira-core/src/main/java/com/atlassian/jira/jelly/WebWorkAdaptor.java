/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */


/**
 */
package com.atlassian.jira.jelly;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.exception.FailedAuthenticationException;
import com.atlassian.jira.action.ActionContextKit;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.webwork.JiraActionFactory;
import com.atlassian.jira.jelly.tag.JellyTagConstants;
import com.atlassian.jira.security.login.JiraSeraphAuthenticator;
import com.atlassian.jira.security.websudo.InternalWebSudoManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.seraph.auth.AuthenticatorException;
import com.atlassian.seraph.auth.DefaultAuthenticator;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import webwork.action.Action;
import webwork.action.ActionSupport;
import webwork.action.factory.ActionFactory;
import webwork.dispatcher.ActionResult;
import webwork.dispatcher.GenericDispatcher;
import webwork.util.ValueStack;

import java.beans.Introspector;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class WebWorkAdaptor
{
    private static final Logger log = Logger.getLogger(WebWorkAdaptor.class);
    private static final String STACK_HEAD = "webwork.valuestack.head";
    private static final boolean FAILURE = false;
    private static final boolean SUCCESS = true;
    private HashMap jellyHttpSession;
    private JellyHttpResponse jellyHttpResponse;
    private JellyHttpRequest jellyHttpRequest;
    private ActionResult actionResult;

    public WebWorkAdaptor()
    {
        jellyHttpSession = new HashMap();
        jellyHttpRequest = new JellyHttpRequest(jellyHttpSession);
        jellyHttpResponse = new JellyHttpResponse();

        // Clear caches
        // RO: If not, then it will contain garbage after a couple of redeployments
        Introspector.flushCaches();

        // Clear ValueStack method cache
        // RO: If not, then it will contain garbage after a couple of redeployments
        ValueStack.clearMethods();
    }

    /**
     * Takes a Jelly Tag, maps it to a Webwork Action and executes the Webwork Action.
     * @param tag The Jelly Tag
     * @param output The XMLOutput writer (the Jelly XMLOutput Stream - where errors and output goes)
     */
    public boolean mapJellyTagToAction(ActionTagSupport tag, XMLOutput output) throws JellyTagException
    {
        log.debug("WebWorkAdaptor.mapJellyTagToAction");
        InternalWebSudoManager internalWebSudoManager = ComponentAccessor.getComponent(InternalWebSudoManager.class);

        // Set up the dispatcher
        String actionName = tag.getActionName();

        // Create a new request.
        jellyHttpRequest.getParameterMap().putAll(tag.getProperties());

        internalWebSudoManager.startSession(jellyHttpRequest,jellyHttpResponse);

        try
        {
            setSecurityCredentials(tag, output);
        }
        catch (Exception e)
        {
            log.error(e, e);
            throw new JellyTagException(e);
        }

        GenericDispatcher gd = new GenericDispatcher(actionName, ActionFactoryInstance.get());
        gd.prepareContext();
        gd.prepareValueStack();
        ActionContextKit.setContext(jellyHttpRequest, jellyHttpResponse, actionName);

        try
        {
            gd.executeAction();
            setResult(gd.finish());
        }
        catch (Exception e)
        {
            return processWebworkException(actionName, e, output, tag);
        }
        finally
        {
            gd.finalizeContext();
        }

        return processResult(tag, output);
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="NP_NULL_ON_SOME_PATH", justification="TODO this needs to be fixed")
    protected boolean processResult(ActionTagSupport tag, XMLOutput output) throws JellyTagException
    {
        // Check if there was an exception
        if (actionResult != null && actionResult.getActionException() != null)
        {
            log.error("Could not execute action", actionResult.getActionException());
            actionResult.getActionException().getMessage();

            StringBuffer errorMsg = new StringBuffer("Could not execute action [" + tag.getActionName() + "]:");
            errorMsg.append(actionResult.getActionException().getMessage());
            errorMsg.append("\n");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            actionResult.getActionException().printStackTrace(ps);
            errorMsg.append(baos.toString());
            errorMsg.append("\n");
            try
            {
                writeErrorToXmlOutput(output, errorMsg, tag.getActionName(), tag);
            }
            catch (SAXException e1)
            {
                log.error(e1);
                throw new JellyTagException(e1);
            }
            return FAILURE;
        }

        String result = actionResult.getResult();

        Action action = actionResult.getFirstAction();

        // Action action = (Action) ServletValueStack.getStack(jellyHttpRequest).popValue();
        jellyHttpRequest.setAttribute(STACK_HEAD, action);

        boolean hasErrorMessages = false;
        boolean hasErrors = false;
        boolean hasSecurityErrors = false;
        try
        {
            // Check for any error messages and write them to the output.
            hasErrorMessages = ((ActionSupport) action).getHasErrorMessages();
            if (hasErrorMessages)
            {
                Collection errorMessages = ((ActionSupport) action).getErrorMessages();
                StringBuffer buff = new StringBuffer();
                buff.append("\nResult=").append(result).append("\n");
                for (final Object errorMessage : errorMessages)
                {
                    String errorMsg = (String) errorMessage;
                    buff.append(errorMsg).append("\n");
                }
                writeErrorToXmlOutput(output, buff, tag.getActionName(), tag);
            }

            hasErrors = ((ActionSupport) action).getHasErrors();
            if (hasErrors)
            {
                Map errors = ((ActionSupport) action).getErrors();
                StringBuffer buff = new StringBuffer();
                buff.append("\nResult=").append(result).append("\n");
                for (final Object o : errors.keySet())
                {
                    String fieldName = (String) o;
                    String errorMessage = (String) errors.get(fieldName);
                    buff.append("Error for field \"").append(fieldName).append("\"").append(" : ").append(errorMessage).append("\n");
                }
                writeErrorToXmlOutput(output, buff, tag.getActionName(), tag);
            }

            hasSecurityErrors = false;
            if (result.equalsIgnoreCase("securitybreach"))
            {
                hasSecurityErrors = true;

                StringBuffer buff = new StringBuffer("Security Breach");
                writeErrorToXmlOutput(output, buff, tag.getActionName(), tag);
            }
        }
        catch (SAXException e)
        {
            log.error(e, e);
            throw new JellyTagException(e);
        }

        if (hasErrorMessages || hasErrors || hasSecurityErrors)
        {
            return FAILURE;
        }
        return SUCCESS;
    }

    protected boolean processWebworkException(String actionName, Exception e, XMLOutput output, ActionTagSupport tag) throws JellyTagException
    {
        log.error("Could not execute action [" + actionName + "]:", e);

        StringBuffer errorMsg = new StringBuffer("Could not execute action [" + actionName + "]:");
        errorMsg.append(e.getMessage());
        errorMsg.append("\n");

        // put the stack in the error
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        e.printStackTrace(ps);
        errorMsg.append(baos.toString());
        errorMsg.append("\n");
        try
        {
            writeErrorToXmlOutput(output, errorMsg, actionName, tag);
        }
        catch (SAXException e1)
        {
            log.error(e1);
            throw new JellyTagException(e1);
        }
        return FAILURE;
    }

    private void setSecurityCredentials(ActionTagSupport tag, XMLOutput output) throws Exception
    {
        CrowdService crowdService = ComponentAccessor.getComponent(CrowdService.class);
        String userName = (String) tag.getContext().getVariable(JellyTagConstants.USERNAME);
        String password = (String) tag.getContext().getVariable(JellyTagConstants.PASSWORD);
        if (userName != null)
        {
            ApplicationUser user = ComponentAccessor.getUserManager().getUserByName(userName);
            if (user != null)
            {
                if (password != null)
                {
                    try
                    {
                        crowdService.authenticate(user.getUsername(), password);
                        getJellyHttpSession().put(DefaultAuthenticator.LOGGED_IN_KEY, user);
                        jellyHttpRequest.setUserPrincipal(user);
                    }
                    catch (FailedAuthenticationException e)
                    {
                        log.info("Cannot login user '" + userName + "' as the password was incorrect");
                    }
                }
                else
                {
                    getJellyHttpSession().put(DefaultAuthenticator.LOGGED_IN_KEY, user);
                    jellyHttpRequest.setUserPrincipal(user);
                }
            }
            else
            {
                StringBuffer errorMsg = new StringBuffer("Security Error : User \"" + userName + "\" does not exist");
                writeErrorToXmlOutput(output, errorMsg, tag.getActionName(), tag);
            }
        }
        else
        {
            throw new IllegalStateException("JellyWebwork Security Error : No security credentials found in tag : " + tag.getActionName());
        }
    }

    /**
     * Formats the error output as one element per error.
     * @param output The XML Output stream.
     * @param errorMessage The Stringbuffer containing the error string.
     * @param actionName the name of the action.
     * @param badTag the tag that failed.
     * @throws SAXException if there's a problem creating XML on output.
     */
    public static void writeErrorToXmlOutput(XMLOutput output, CharSequence errorMessage, String actionName, TagSupport badTag) throws SAXException
    {
        log.debug("WebWorkAdaptor.writeErrorToXmlOutput");

        final AttributesImpl tagAttributes = new AttributesImpl();
        tagAttributes.addAttribute("", "", "action", "string", actionName);
        output.startElement("Error", tagAttributes);

        output.startElement("TagContents", new AttributesImpl());
        output.write(badTag.toString());
        output.endElement("TagContents");

        output.startElement("ExecutedAs", new AttributesImpl());
        output.write("user=" + badTag.getContext().getVariable(JellyTagConstants.USERNAME));
        output.endElement("ExecutedAs");

        output.startElement("ErrorMessage", new AttributesImpl());
        output.write(errorMessage.toString());
        output.endElement("ErrorMessage");

        output.endElement("Error");
    }

    public boolean authenticateUser(ActionTagSupport tag, String username, String password, XMLOutput output) throws Exception
    {
        try
        {
            ActionContextKit.setContext(getJellyHttpRequest(), getJellyHttpResponse(), "Login");

            JiraSeraphAuthenticator authenticator = new JiraSeraphAuthenticator();
            return authenticator.login(getJellyHttpRequest(), getJellyHttpResponse(), username, password);
        }
        catch (AuthenticatorException e)
        {
            log.error(e.getMessage(), e);
            writeErrorToXmlOutput(output, new StringBuffer(e.getMessage()), tag.getActionName(), tag);
            return false;
        }
    }

    public HashMap getJellyHttpSession()
    {
        return jellyHttpSession;
    }

    public JellyHttpResponse getJellyHttpResponse()
    {
        return jellyHttpResponse;
    }

    public JellyHttpRequest getJellyHttpRequest()
    {
        return jellyHttpRequest;
    }

    public void setResult(ActionResult actionResult)
    {
        this.actionResult = actionResult;
    }

    public ActionResult getResult()
    {
        return actionResult;
    }

    /**
     * Holds a lazily initialised {@link webwork.action.factory.ActionFactory} instance.
     *
     * Laziness is needed because when this class is initialised the {@link webwork.action.factory.ActionFactory} is not ready to be
     * initialised yet :-(
     */
    private static class ActionFactoryInstance
    {
        private final static ActionFactory INSTANCE = new JiraActionFactory.NonWebActionFactory();

        private ActionFactoryInstance(){}

        public static ActionFactory get()
        {
            return INSTANCE;
        }
    }
}
