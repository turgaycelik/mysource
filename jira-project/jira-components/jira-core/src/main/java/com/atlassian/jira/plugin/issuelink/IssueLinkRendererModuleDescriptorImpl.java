package com.atlassian.jira.plugin.issuelink;

import com.atlassian.jira.issue.link.RemoteIssueLink;
import com.atlassian.jira.plugin.AbstractJiraModuleDescriptor;
import com.atlassian.jira.plugin.util.ModuleDescriptors;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.util.validation.ValidationPattern;
import org.dom4j.Element;

import java.util.Collections;

import static com.atlassian.plugin.util.validation.ValidationPattern.test;

/**
 * Default implementation of an {@link IssueLinkRendererModuleDescriptor}.
 *
 * @since v5.0
 */
public class IssueLinkRendererModuleDescriptorImpl extends AbstractJiraModuleDescriptor<IssueLinkRenderer> implements IssueLinkRendererModuleDescriptor
{
    @SuppressWarnings("unused")
    public static final String XML_ELEMENT_NAME = "issue-link-renderer";
    public static final String INITIAL_VIEW_RESOURCE_NAME = "initial-view";
    public static final String FINAL_VIEW_RESOURCE_NAME = "final-view";

    private String applicationType;
    private boolean defaultHandler;

    public IssueLinkRendererModuleDescriptorImpl(final JiraAuthenticationContext authenticationContext, final ModuleFactory moduleFactory)
    {
        super(authenticationContext, moduleFactory);
    }

    @Override
    public void init(final Plugin plugin, final Element element) throws PluginParseException
    {
        super.init(plugin, element);

        applicationType = element.attributeValue("application-type");
        defaultHandler = Boolean.parseBoolean(element.attributeValue("default-handler"));
    }

    @Override
    public void enabled()
    {
        super.enabled();
        assertModuleClassImplements(IssueLinkRenderer.class);
    }

    @Override
    protected void provideValidationRules(ValidationPattern pattern)
    {
        super.provideValidationRules(pattern);
        pattern.rule(test("@application-type").withError("The application type is required"));
    }

    @Override
    public boolean isDefaultHandler()
    {
        return defaultHandler;
    }

    @Override
    public boolean handlesApplicationType(String applicationType)
    {
        return this.applicationType != null && this.applicationType.equals(applicationType);
    }

    @Override
    public String getInitialHtml(RemoteIssueLink remoteIssueLink)
    {
        return getHtml(INITIAL_VIEW_RESOURCE_NAME, getModule().getInitialContext(remoteIssueLink, createVelocityParams(Collections.<String, Object>emptyMap())));
    }

    @Override
    public String getFinalHtml(RemoteIssueLink remoteIssueLink)
    {
        return getHtml(FINAL_VIEW_RESOURCE_NAME, getModule().getFinalContext(remoteIssueLink, createVelocityParams(Collections.<String, Object>emptyMap())));
    }

    @Override
    public boolean equals(Object obj)
    {
        return new ModuleDescriptors.EqualsBuilder(this).isEqualsTo(obj);
    }

    @Override
    public int hashCode()
    {
        return new ModuleDescriptors.HashCodeBuilder(this).toHashCode();
    }
}
