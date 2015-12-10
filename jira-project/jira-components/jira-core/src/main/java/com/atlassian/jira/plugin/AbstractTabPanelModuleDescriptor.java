package com.atlassian.jira.plugin;

import com.atlassian.jira.plugin.browsepanel.TabPanel;
import com.atlassian.jira.plugin.util.ModuleDescriptorXMLUtils;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.util.IssueTableBean;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.module.ModuleFactory;
import org.dom4j.Element;

import java.util.HashMap;
import java.util.Map;

/**
 * Copyright 2007 Atlassian Software.
 * All rights reserved.
 */
abstract public class AbstractTabPanelModuleDescriptor<T extends TabPanel>
        extends AbstractJiraModuleDescriptor<T>
        implements TabPanelModuleDescriptor<T>
{
    private String label = "Unknown";
    private String labelKey;
    protected int order;

    public AbstractTabPanelModuleDescriptor(JiraAuthenticationContext authenticationContext, final ModuleFactory moduleFactory)
    {
        super(authenticationContext, moduleFactory);
    }

    public void init(Plugin plugin, Element element) throws PluginParseException
    {
        super.init(plugin, element);

        final Element labelEl = element.element("label");
        if (labelEl != null)
        {
            if (labelEl.attribute("key") != null)
            {
                labelKey = labelEl.attributeValue("key");
            }
            else
            {
                label = labelEl.getTextTrim();
            }
        }

        order = ModuleDescriptorXMLUtils.getOrder(element);
    }

    public void enabled()
    {
        super.enabled();
        assertModuleClass();
    }

    abstract protected void assertModuleClass() throws PluginParseException;

    public int getOrder()
    {
        return order;
    }

    public String getLabel()
    {
        if (labelKey != null)
        {
            return getI18nBean().getText(labelKey);
        }
        return label;
    }

    public String getLabelKey()
    {
        return labelKey;
    }

    @SuppressWarnings ({ "unchecked" })
    protected Map createVelocityParams(Map startingParams)
    {
        // Add IssueTableBean to pass through to velocity
        // Used to display subtasks as in IssueNavigator
        IssueTableBean issueBean = new IssueTableBean();
        Map velocityParams = new HashMap();
        velocityParams.putAll(super.createVelocityParams(startingParams));
        velocityParams.put("issueBean", issueBean);

        return velocityParams;
    }

}
