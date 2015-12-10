package com.atlassian.jira.web.component;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.opensymphony.util.TextUtils;
import org.apache.log4j.Logger;
import org.apache.velocity.exception.VelocityException;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import static com.atlassian.jira.template.TemplateSources.file;

/**
 * The superclass of all web components, which has some simple helper methods.
 */
public class AbstractWebComponent
{
    private static final Logger log = Logger.getLogger(AbstractWebComponent.class);

    protected final ApplicationProperties applicationProperties;
    private final VelocityTemplatingEngine templatingEngine;

    public AbstractWebComponent(VelocityTemplatingEngine templatingEngine, ApplicationProperties applicationProperties)
    {
        this.templatingEngine = templatingEngine;
        this.applicationProperties = applicationProperties;
    }

    protected String getHtml(String resourceName, Map<String, Object> startingParams)
    {
        if (TextUtils.stringSet(resourceName))
        {
            try
            {
                return templatingEngine.render(file(resourceName)).applying(startingParams).asHtml();
            }
            catch (VelocityException e)
            {
                log.error("Error while rendering velocity template for '" + resourceName + "'.", e);
            }
        }
        return "";
    }

    protected void asHtml(Writer writer, String resourceName, Map<String, Object> startingParams)
    {
        if (TextUtils.stringSet(resourceName))
        {
            try
            {
                templatingEngine.render(file(resourceName)).applying(startingParams).asHtml(writer);
            }
            catch (IOException e)
            {
                log.error("Error while rendering velocity template for '" + resourceName + "'.", e);
            }
        }
    }
}
