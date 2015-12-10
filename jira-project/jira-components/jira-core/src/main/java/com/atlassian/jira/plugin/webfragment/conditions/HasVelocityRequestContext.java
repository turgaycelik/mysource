package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.util.velocity.DefaultVelocityRequestContextFactory;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;

import java.util.Map;

/**
 * Checks if there is a {@link com.atlassian.jira.util.velocity.VelocityRequestContext#getRequestParameters()}.
 * This will ensure that the printable icon link will not be displayed if there is no request. 
 */
public class HasVelocityRequestContext implements Condition
{
    public void init(Map params) throws PluginParseException
    {
    }

    public boolean shouldDisplay(Map context)
    {
        VelocityRequestContextFactory velocityRequestContextFactory = new DefaultVelocityRequestContextFactory(ComponentAccessor.getApplicationProperties());
        VelocityRequestContext velocityRequestContext = velocityRequestContextFactory.getJiraVelocityRequestContext();
        return velocityRequestContext != null && velocityRequestContext.getRequestParameters() != null;
    }
}
