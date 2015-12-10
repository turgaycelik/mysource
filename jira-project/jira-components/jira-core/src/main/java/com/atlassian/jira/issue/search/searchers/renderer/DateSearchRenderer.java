package com.atlassian.jira.issue.search.searchers.renderer;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.constants.SimpleFieldSearchConstants;
import com.atlassian.jira.issue.search.searchers.util.DateSearcherConfig;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.jira.web.action.util.CalendarLanguageUtil;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import static com.atlassian.jira.util.dbc.Assertions.notBlank;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Render for system field date base searchers in Jira.
 *
 * @since v4.0
 */
public final class DateSearchRenderer extends AbstractDateSearchRenderer implements SearchRenderer
{
    private final SimpleFieldSearchConstants constants;
    private final FieldVisibilityManager fieldVisibilityManager;

    public DateSearchRenderer(final SimpleFieldSearchConstants constants, final DateSearcherConfig config, final String nameKey,
            final VelocityRequestContextFactory velocityRequestContextFactory,
            final ApplicationProperties applicationProperties, final VelocityTemplatingEngine templatingEngine, final CalendarLanguageUtil calendarUtils,
            final FieldVisibilityManager fieldVisibilityManager)
    {
        super(constants, config, new SimpleTranslationsHelper(nameKey), velocityRequestContextFactory,
                applicationProperties, templatingEngine, nameKey, calendarUtils);
        this.constants = notNull("constants", constants);
        this.fieldVisibilityManager = notNull("fieldVisibilityManager", fieldVisibilityManager);
    }

    public boolean isShown(final User user, final SearchContext searchContext)
    {
        return !fieldVisibilityManager.isFieldHiddenInAllSchemes(constants.getFieldId(), searchContext, user);
    }

    /**
     * Returns the transations for the date field by appending properties to its name key.
     *
     * @since 4.0
     */
    private final static class SimpleTranslationsHelper implements TranslationsHelper
    {
        private final String beforeKey;
        private final String afterKey;
        private final String periodKey;
        private final String nameKey;

        public SimpleTranslationsHelper(final String nameKey)
        {
            this.nameKey = notBlank("nameKey", nameKey);

            this.beforeKey = nameKey + ".before";
            this.afterKey = nameKey + ".after";
            this.periodKey = nameKey + ".period";
        }

        public String getName(final I18nHelper helper)
        {
            notNull("helper", helper);
            return helper.getText(nameKey);
        }

        public String getBeforeLabel(final I18nHelper helper)
        {
            notNull("helper", helper);
            return helper.getText(beforeKey);
        }

        public String getAfterLabel(final I18nHelper helper)
        {
            notNull("helper", helper);
            return helper.getText(afterKey);
        }

        public String getPeriodLabel(final I18nHelper helper)
        {
            notNull("helper", helper);
            return helper.getText(periodKey);
        }

        public String getDescription(final I18nHelper helper)
        {
            notNull("helper", helper);
            return "";
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SIMPLE_STYLE);
        }
    }
}
