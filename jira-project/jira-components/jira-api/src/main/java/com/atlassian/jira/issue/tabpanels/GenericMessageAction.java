package com.atlassian.jira.issue.tabpanels;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.plugin.issuetabpanel.IssueAction;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.dbc.Assertions;

import com.google.common.collect.ImmutableMap;
import org.apache.log4j.Logger;
import org.apache.velocity.exception.VelocityException;

import javax.annotation.Nonnull;
import java.util.Date;
import java.util.Map;

import static com.atlassian.jira.template.TemplateSources.file;

/**
 * A simple action that can be used to display generic messages.
 */
@PublicApi
public class GenericMessageAction implements IssueAction
{
    private static final Logger log = Logger.getLogger(GenericMessageAction.class);
    private static final String PLUGIN_TEMPLATES = "templates/plugins/";

    private final String message;

    public GenericMessageAction(@Nonnull final String message)
    {
        this.message = Assertions.notNull(message);
    }

    public Date getTimePerformed()
    {
        throw new UnsupportedOperationException();
    }

    public String getHtml()
    {
        final String templateName = "jira/issuetabpanels/generic-message.vm";
        final VelocityTemplatingEngine templatingEngine = ComponentAccessor.getComponent(VelocityTemplatingEngine.class);
        try
        {
            final Map<String, Object> params = ImmutableMap.<String, Object>of("message", message);
            return templatingEngine.render(file(PLUGIN_TEMPLATES + templateName)).applying(params).asHtml();
        }
        catch (VelocityException e)
        {
            log.error("Error while rendering velocity template for '" + templateName + "'.", e);
            return "Velocity template generation failed.";
        }
    }

    public boolean isDisplayActionAllTab()
    {
        return false;
    }
}
