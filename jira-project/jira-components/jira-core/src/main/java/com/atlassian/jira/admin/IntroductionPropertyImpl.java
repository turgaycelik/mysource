package com.atlassian.jira.admin;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.I18nBean;

import static com.atlassian.jira.config.properties.APKeys.JIRA_INTRODUCTION;

public class IntroductionPropertyImpl implements IntroductionProperty
{
    private final RenderablePropertyImpl introductionProperty;
    private final I18nHelper.BeanFactory i18nFactory;
    private final JiraAuthenticationContext authenticationContext;

    public IntroductionPropertyImpl(ApplicationProperties applicationProperties, I18nBean.BeanFactory i18nFactory, JiraAuthenticationContext authenticationContext, RenderablePropertyFactory renderablePropertyFactory)
    {
        this.i18nFactory = i18nFactory;
        this.authenticationContext = authenticationContext;
        this.introductionProperty = renderablePropertyFactory.create(new ApplicationPropertiesPersister(applicationProperties, JIRA_INTRODUCTION), new IntroductionDescriptions());
    }

    @Override
    public String getValue()
    {
        return introductionProperty.getValue();
    }

    @Override
    public void setValue(String value)
    {
        introductionProperty.setValue(value);
    }

    @Override
    public String getEditHtml(String fieldName)
    {
        return introductionProperty.getEditHtml(fieldName);
    }

    @Override
    public String getViewHtml()
    {
        return introductionProperty.getViewHtml();
    }

    public String getDescriptionHtml()
    {
        return introductionProperty.getDescriptionHtml();
    }

    private class IntroductionDescriptions implements PropertyDescriptions
    {
        @Override
        public String getBtfDescriptionHtml()
        {
            return i18nFactory.getInstance(authenticationContext.getUser()).getText("admin.generalconfiguration.introduction.description", "<br/>");
        }

        @Override
        public String getOnDemandDescriptionHtml()
        {
            return i18nFactory.getInstance(authenticationContext.getUser()).getText("admin.generalconfiguration.introduction.wiki.description", "<br/>");
        }
    }
}
