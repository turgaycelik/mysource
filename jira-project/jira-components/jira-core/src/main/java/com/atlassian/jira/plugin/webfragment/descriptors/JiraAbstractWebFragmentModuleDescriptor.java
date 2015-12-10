package com.atlassian.jira.plugin.webfragment.descriptors;

import com.atlassian.jira.plugin.webfragment.model.JiraWebLabel;
import com.atlassian.jira.plugin.webfragment.model.JiraWebParam;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.plugin.web.descriptors.AbstractWebFragmentModuleDescriptor;
import com.atlassian.plugin.web.descriptors.DefaultAbstractWebFragmentModuleDescriptor;
import com.atlassian.plugin.web.model.WebLabel;
import com.atlassian.plugin.web.model.WebParam;

public abstract class JiraAbstractWebFragmentModuleDescriptor extends DefaultAbstractWebFragmentModuleDescriptor<Void>
{
    protected final JiraAuthenticationContext authenticationContext;

    public JiraAbstractWebFragmentModuleDescriptor(final JiraAuthenticationContext authenticationContext, final AbstractWebFragmentModuleDescriptor<Void> abstractDescriptor)
    {
        super(abstractDescriptor);
        this.authenticationContext = authenticationContext;
    }

    @Override
    public String getDescription()
    {
        final String key = getDescriptionKey();
        if (key != null)
        {
            return getI18nBean().getText(key);
        }

        return super.getDescription();
    }

    @Override
    public String getName()
    {
        if (getI18nNameKey() != null)
        {
            return getI18nBean().getText(getI18nNameKey());
        }

        return super.getName();
    }

    // TODO (4.3) this should override #getWebLabel instead of providing alternative API
    public WebLabel getLabel()
    {
        final WebLabel label = getDecoratedDescriptor().getWebLabel();
        if (label != null)
        {
            return new JiraWebLabel(label, authenticationContext);
        }

        return null;
    }

    @Override
    public WebLabel getTooltip()
    {
        final WebLabel tooltip = getDecoratedDescriptor().getTooltip();
        if (tooltip != null)
        {
            return new JiraWebLabel(tooltip, authenticationContext);
        }

        return null;
    }

    @Override
    public WebParam getWebParams()
    {
        final WebParam webParams = getDecoratedDescriptor().getWebParams();
        if (webParams != null)
        {
            return new JiraWebParam(webParams);
        }

        return null;
    }

    // This may be used within velocity
    public I18nHelper getI18nBean()
    {
        return authenticationContext.getI18nHelper();
    }
}
