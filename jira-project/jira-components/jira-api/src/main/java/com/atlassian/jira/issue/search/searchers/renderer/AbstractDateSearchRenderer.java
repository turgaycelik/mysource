package com.atlassian.jira.issue.search.searchers.renderer;

import com.atlassian.annotations.Internal;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.constants.SimpleFieldSearchConstants;
import com.atlassian.jira.issue.search.searchers.util.DateSearcherConfig;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.action.util.CalendarLanguageUtil;
import com.atlassian.jira.web.action.util.CalendarResourceIncluder;
import com.atlassian.query.Query;
import webwork.action.Action;

import java.util.Map;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Render for date base searchers in Jira.
 *
 * @since v4.0
 */
@Internal
public abstract class AbstractDateSearchRenderer extends AbstractSearchRenderer implements SearchRenderer
{
    private final CalendarLanguageUtil calendarUtils;
    private final DateSearcherConfig config;
    private final TranslationsHelper translationHelper;
    private final SimpleFieldSearchConstants constants;
    private final String searchNameKey;

    public AbstractDateSearchRenderer(SimpleFieldSearchConstants constants, DateSearcherConfig config, TranslationsHelper translationHelper,
            VelocityRequestContextFactory velocityRequestContextFactory, ApplicationProperties applicationProperties,
            VelocityTemplatingEngine templatingEngine, String searcherNameKey, CalendarLanguageUtil calendarUtils
    )
    {
        super(velocityRequestContextFactory, applicationProperties, templatingEngine, constants, searcherNameKey);
        this.constants = notNull("constants", constants);
        this.config = notNull("config", config);
        this.calendarUtils = notNull("calendarUtils", calendarUtils);
        this.translationHelper = notNull("translationHelper", translationHelper);
        this.searchNameKey = searcherNameKey;
    }

    abstract public boolean isShown(final User user, final SearchContext searchContext);

    public String getEditHtml(final User user, final SearchContext searchContext, final FieldValuesHolder fieldValuesHolder, final Map<?, ?> displayParameters, final Action action)
    {
        final Map<String, Object> velocityParams = getVelocityParams(user, searchContext, null, fieldValuesHolder, displayParameters, action);
        return renderEditTemplate("date-searcher-edit.vm", addEditParameters(user, velocityParams));
    }

    private Map<String, Object> addEditParameters(final User searcher, final Map<String, Object> velocityParams)
    {
        final I18nHelper i18n = getI18n(searcher);
        final String language = i18n.getLocale().getLanguage();
        velocityParams.put("hasCalendarTranslation", calendarUtils.hasTranslationForLanguage(language));
        velocityParams.put("calendarIncluder", new CalendarResourceIncluder());
        velocityParams.put("fieldName", config.getFieldName());

        return addCommonParameters(searcher, velocityParams);
    }

    public String getViewHtml(final User user, final SearchContext searchContext, final FieldValuesHolder fieldValuesHolder, final Map<?, ?> displayParameters, final Action action)
    {
        final Map<String, Object> velocityParams = getVelocityParams(user, searchContext, null, fieldValuesHolder, displayParameters, action);
        return renderViewTemplate("date-searcher-view.vm", addViewParameters(user, velocityParams, fieldValuesHolder));
    }

    private Map<String, Object> addViewParameters(final User searcher, final Map<String, Object> velocityParams, final FieldValuesHolder fieldValuesHolder)
    {
        DateSearchRendererViewHtmlMessageProvider provider = new DateSearchRendererViewHtmlMessageProvider(getI18n(searcher), fieldValuesHolder, config, searchNameKey);
        DateSearchRendererViewHtmlMessageProvider.Result result = provider.getResult();

        if (result != null)
        {
            velocityParams.put("message", result.message);
            velocityParams.put("previous", result.previous);
            velocityParams.put("next", result.next);
        }

        return addCommonParameters(searcher, velocityParams);
    }

    private Map<String, Object> addCommonParameters(final User searcher, final Map<String, Object> velocityParams)
    {
        //Insert navigator form names.
        velocityParams.put("afterField", config.getAfterField());
        velocityParams.put("beforeField", config.getBeforeField());
        velocityParams.put("previousField", config.getPreviousField());
        velocityParams.put("nextField", config.getNextField());
        velocityParams.put("id", config.getId());

        final I18nHelper i18n = getI18n(searcher);

        //Insert the labels used to render the editor.
        velocityParams.put("afterFieldLabel", translationHelper.getAfterLabel(i18n));
        velocityParams.put("beforeFieldLabel", translationHelper.getBeforeLabel(i18n));
        velocityParams.put("periodLabel", translationHelper.getPeriodLabel(i18n));
        velocityParams.put("description", translationHelper.getDescription(i18n));

        return velocityParams;
    }

    public boolean isRelevantForQuery(final User user, final Query query)
    {
        return isRelevantForQuery(constants.getJqlClauseNames(), query);
    }

    /**
     * Interface used by the renderer to get the translations needed to render a date searcher.
     *
     * @since 4.0
     */
    public interface TranslationsHelper
    {
        /**
         * Get the label associated with the before field.
         *
         * @param helper the i18n helper that can return translations.
         * @return the label associated with the before field.
         */
        String getBeforeLabel(I18nHelper helper);

        /**
         * Get the label associated with the after field.
         *
         * @param helper the i18n helper that can return translations.
         * @return the label associated with the after field.
         */
        String getAfterLabel(I18nHelper helper);

        /**
         * Get the label associated with the period fields.
         *
         * @param helper the i18n helper that can return translations.
         * @return the label associated with the period fields.
         */
        String getPeriodLabel(I18nHelper helper);

        /**
         * Get the description associated with the period fields.
         *
         * @param helper the i18n helper that can return translations.
         * @return the description associated with the searcher.
         */
        String getDescription(I18nHelper helper);
    }
}
