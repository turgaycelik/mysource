/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Jul 28, 2004
 * Time: 11:01:00 AM
 */
package com.atlassian.jira.plugin.issuetabpanel;

import com.atlassian.jira.plugin.AbstractJiraModuleDescriptor;
import com.atlassian.jira.plugin.util.ModuleDescriptorXMLUtils;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.module.ModuleFactory;
import org.apache.log4j.Logger;
import org.dom4j.Element;

/**
 * An issue tab panel plugin adds extra panel tabs to JIRA's View Issue page.
 */
public class IssueTabPanelModuleDescriptorImpl extends AbstractJiraModuleDescriptor<IssueTabPanel3> implements IssueTabPanelModuleDescriptor
{
    private static final Logger log = Logger.getLogger(IssueTabPanelModuleDescriptorImpl.class);

    String label;
    private String labelKey;
    boolean isDefault = false;
    private int order;
    private boolean isSortable = false;
    private boolean supportsAjaxLoad;

    public IssueTabPanelModuleDescriptorImpl(JiraAuthenticationContext authenticationContext, final ModuleFactory moduleFactory)
    {
        super(authenticationContext, moduleFactory);
    }

    @Override
    public void init(Plugin plugin, Element element) throws PluginParseException
    {
        super.init(plugin, element);
        label = element.element("label").getTextTrim();
        if (element.element("label").attribute("key") != null)
        {
            labelKey = element.element("label").attribute("key").getText();
        }
        isDefault = getBooleanElement(element, "default");
        order = ModuleDescriptorXMLUtils.getOrder(element);
        isSortable = getBooleanElement(element, "sortable");
        supportsAjaxLoad = getBooleanElement(element, "supports-ajax-load");
    }

    @Override
    public void enabled()
    {
        super.enabled();
    }

    private static boolean getBooleanElement(Element parentElement, String elementName)
    {
        Element element = parentElement.element(elementName);
        if (element == null)
        {
            return false;
        }

        String isDefaultText = element.getTextTrim();
        return isDefaultText != null && Boolean.valueOf(isDefaultText);
    }

    private IssueTabPanel3 getTabPanel()
    {
        return getModule();
    }

    @Override
    public String getLabel()
    {
        if (labelKey != null)
        {
            return getI18nBean().getText(labelKey);
        }
        return label;
    }

    protected IssueTabPanel3 createModule()
    {
        // Don't make any claims on the type of this module yet - it could be whatever.
        final Object module = moduleFactory.createModule(moduleClassName, this);
        final IssueTabPanel3 issueTabPanel;
        if (module instanceof IssueTabPanel)
            issueTabPanel = IssueTabPanel3Adaptor.createFrom((IssueTabPanel) module);
        else if (module instanceof IssueTabPanel3)
            issueTabPanel = (IssueTabPanel3) module;
        else
            throw new ClassCastException("Class " + module.getClass().getCanonicalName() + " is not a legal class for issue tab panel plugin point.");
        issueTabPanel.init(this);
        return issueTabPanel;
    }

    @Override
    public int getOrder()
    {
        return order;
    }

    @Override
    public boolean isDefault()
    {
        return isDefault;
    }

    @Override
    public boolean isSortable()
    {
        return isSortable;
    }

    @Override
    public boolean isSupportsAjaxLoad()
    {
        return supportsAjaxLoad;
    }
}
