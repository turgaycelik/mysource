package com.atlassian.jira.plugin.issueview;

import com.atlassian.jira.plugin.AbstractJiraModuleDescriptor;
import com.atlassian.jira.plugin.util.ModuleDescriptorXMLUtils;
import com.atlassian.jira.plugin.webfragment.descriptors.ConditionDescriptorFactory;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.web.Condition;
import org.dom4j.Element;

/**
 * An issue view allows you to view an issue in different ways  (eg XML, Word, PDF)
 *
 * @see com.atlassian.jira.plugin.issueview.IssueView
 */
public class IssueViewModuleDescriptorImpl extends AbstractJiraModuleDescriptor<IssueView> implements IssueViewModuleDescriptor
{
    private String fileExtension;
    private String contentType;
    private final IssueViewURLHandler urlHandler;
    private final ConditionDescriptorFactory conditionDescriptorFactory;
    private int order;
    private Condition condition;
    private Element element;

    public IssueViewModuleDescriptorImpl(final JiraAuthenticationContext authenticationContext,
            final IssueViewURLHandler urlHandler, final ModuleFactory moduleFactory, final ConditionDescriptorFactory conditionDescriptorFactory)
    {
        super(authenticationContext, moduleFactory);
        this.urlHandler = urlHandler;
        this.conditionDescriptorFactory = conditionDescriptorFactory;
    }

    public void init(Plugin plugin, Element element) throws PluginParseException
    {
        super.init(plugin, element);
        fileExtension = element.attribute("fileExtension").getStringValue();
        contentType = element.attribute("contentType").getStringValue();
        order = ModuleDescriptorXMLUtils.getOrder(element);
        this.element = element;
    }

    public void enabled()
    {
        super.enabled();
        assertModuleClassImplements(IssueView.class);
        condition = conditionDescriptorFactory.retrieveCondition(plugin, element);
    }

    @Override
    public void disabled()
    {
        condition = null;
        super.disabled();
    }

    public IssueView getIssueView()
    {
        return getModule();
    }

    public String getFileExtension()
    {
        return fileExtension;
    }

    public String getContentType()
    {
        return contentType;
    }

    public String getURLWithoutContextPath(String issueKey)
    {
        return urlHandler.getURLWithoutContextPath(this, issueKey);
    }

    public int getOrder() {
        return order;
    }

    @Override
    public Condition getCondition()
    {
        return condition;
    }
}
