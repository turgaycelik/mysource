package com.atlassian.jira.issue.customfields.searchers.renderer;

import com.atlassian.annotations.Internal;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.customfields.CustomFieldUtils;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.constants.SimpleFieldSearchConstants;
import com.atlassian.jira.issue.search.searchers.renderer.AbstractDateSearchRenderer;
import com.atlassian.jira.issue.search.searchers.renderer.SearchRenderer;
import com.atlassian.jira.issue.search.searchers.util.DateSearcherConfig;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.jira.web.action.util.CalendarLanguageUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import webwork.action.Action;

import java.util.Map;

/**
 * @since v4.0
 */
@Internal
public class DateCustomFieldSearchRenderer extends AbstractDateSearchRenderer implements SearchRenderer
{
    private final boolean dateTimePicker;
    private final CustomField customField;
    private final FieldVisibilityManager fieldVisibilityManager;

    public DateCustomFieldSearchRenderer(boolean isDateTimePicker, CustomField customField, SimpleFieldSearchConstants constants, DateSearcherConfig config,
            VelocityRequestContextFactory velocityRequestContextFactory,
            ApplicationProperties applicationProperties, VelocityTemplatingEngine templatingEngine, CalendarLanguageUtil calendarUtils,
            final FieldVisibilityManager fieldVisibilityManager)
    {
        super(constants, config, new CustomFieldTranslationsHelper(customField), velocityRequestContextFactory, applicationProperties, templatingEngine, null, calendarUtils);
        dateTimePicker = isDateTimePicker;
        this.customField = customField;
        this.fieldVisibilityManager = fieldVisibilityManager;
    }

    @Override
    protected Map<String, Object> getVelocityParams(final User user, final SearchContext searchContext, final FieldLayoutItem fieldLayoutItem, final FieldValuesHolder fieldValuesHolder, final Map<?, ?> displayParameters, final Action action)
    {
        Map<String, Object> map = super.getVelocityParams(user, searchContext, fieldLayoutItem, fieldValuesHolder, displayParameters, action);
        if (dateTimePicker)
        {
            map.put("dateTimePicker", Boolean.TRUE);
        }
        return map;
    }

    public boolean isShown(final User user, final SearchContext searchContext)
    {
        return CustomFieldUtils.isShownAndVisible(customField, user, searchContext, fieldVisibilityManager);
    }

    /**
     * Returns the transations for the date field
     *
     * @since 4.0
     */
    private final static class CustomFieldTranslationsHelper implements TranslationsHelper
    {
        private final CustomField field;

        public CustomFieldTranslationsHelper(final CustomField field)
        {
            this.field = field;
        }

        public String getBeforeLabel(final I18nHelper helper)
        {
            return field.getName() + " (" + StringUtils.lowerCase(helper.getText("navigator.filter.cf.before")) +  ")";
        }

        public String getAfterLabel(final I18nHelper helper)
        {
            return field.getName() + " (" + StringUtils.lowerCase(helper.getText("navigator.filter.cf.after")) + ")";
        }

        public String getPeriodLabel(final I18nHelper helper)
        {
            return field.getName();
        }

        public String getDescription(final I18nHelper helper)
        {
            return (field != null) ? field.getDescriptionProperty().getViewHtml() : "";
        }
       
        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SIMPLE_STYLE);
        }
    }
}
