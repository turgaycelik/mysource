package com.atlassian.jira.issue.fields;

import com.atlassian.jira.admin.PropertyDescriptions;
import com.atlassian.jira.admin.PropertyPersister;
import com.atlassian.jira.admin.RenderableProperty;
import com.atlassian.jira.admin.RenderablePropertyFactory;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Helper class for rendering the custom field description field.
 *
 * @since v5.0.7
 */
public class CustomFieldDescription
{
    private final RenderablePropertyFactory renderablePropertyFactory;
    private final I18nHelper.BeanFactory i18nFactory;
    private final JiraAuthenticationContext authenticationContext;

    public CustomFieldDescription(RenderablePropertyFactory renderablePropertyFactory, I18nHelper.BeanFactory i18nFactory, JiraAuthenticationContext authenticationContext)
    {
        this.renderablePropertyFactory = renderablePropertyFactory;
        this.i18nFactory = i18nFactory;
        this.authenticationContext = authenticationContext;
    }

    /**
     * Creates a RenderableProperty used for rendering view/edit controls for a given CustomField. If {@code
     * customField} is null, then the RenderableProperty will not have any associated storage (but can be used to render
     * the description field in the "create custom field" page).
     *
     * @param customField a CustomField (may be null)
     * @return a RenderableProperty
     */
    @Nonnull
    public RenderableProperty createRenderablePropertyFor(@Nullable CustomField customField)
    {
        if (customField == null)
        {
            return renderablePropertyFactory.create(new EmptyPropertyPersister(), new CustomFieldDescriptions());
        }

        return renderablePropertyFactory.create(new ReadOnlyPersister(customField), new CustomFieldDescriptions());
    }
    /**
     * Creates a RenderableProperty used for translating a given CustomField. It just uses the passed in text.
     *
     * @param description
     * @return a RenderableProperty
     */
    @Nonnull
    public RenderableProperty createRenderablePropertyFor(String description)
    {
        return renderablePropertyFactory.create(new ReadOnlyTextPersister(description), new CustomFieldDescriptions());
    }

    final String getText(String key, String... params)
    {
        return i18nFactory.getInstance(authenticationContext.getLoggedInUser()).getText(key, params);
    }

    private static class EmptyPropertyPersister implements PropertyPersister
    {
        @Override
        public String load()
        {
            return "";
        }

        @Override
        public void save(String value)
        {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Read-only persister for a custom field's description.
     */
    private static class ReadOnlyPersister implements PropertyPersister
    {
        private final CustomField customField;

        public ReadOnlyPersister(CustomField customField)
        {
            this.customField = customField;
        }

        @Override
        public String load()
        {
            return customField.getDescription();
        }

        @Override
        public void save(String value)
        {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Read-only persister for a custom field's translated text descriptions
     */
    private static class ReadOnlyTextPersister implements PropertyPersister
    {
        private final String desc;

        public ReadOnlyTextPersister(final String desc)
        {
            this.desc = desc;
        }

        @Override
        public String load()
        {
            return desc;
        }

        @Override
        public void save(String value)
        {
            throw new UnsupportedOperationException();
        }
    }

    private class CustomFieldDescriptions implements PropertyDescriptions
    {
        @Override
        public String getBtfDescriptionHtml()
        {
            return getText("admin.issuefields.customfields.edit.description.detail", "<br/>");
        }

        @Override
        public String getOnDemandDescriptionHtml()
        {
            return getText("admin.issuefields.customfields.edit.description.wiki.detail", "<br/>");
        }
    }
}
