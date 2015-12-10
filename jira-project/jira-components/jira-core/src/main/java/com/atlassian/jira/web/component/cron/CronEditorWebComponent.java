package com.atlassian.jira.web.component.cron;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.datetime.DateTimeStyle;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.JiraVelocityUtils;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.web.component.AbstractWebComponent;
import com.atlassian.jira.web.component.cron.generator.CronExpressionGenerator;
import com.atlassian.jira.web.util.HelpUtil;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.util.profiling.UtilTimerStack;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * Used to display and process a cron-style HTML editor. This generates the inputs that can be placed into a form. You
 * can get a handle on the form HTML by using the {@link #getHtml(CronEditorBean,String)} method.
 * <p/>
 * Once the form has been submitted you can attain a cron string by processing the input parameters with the {@link
 * #getCronExpressionFromInput(CronEditorBean)} method.
 */
public class CronEditorWebComponent extends AbstractWebComponent
{
    private final JiraAuthenticationContext authenticationContext = ComponentAccessor.getComponentOfType(JiraAuthenticationContext.class);
    private final CronExpressionGenerator cronExpressionGenerator;

    public CronEditorWebComponent()
    {
        this(ComponentAccessor.getComponent(VelocityTemplatingEngine.class), ComponentAccessor.getApplicationProperties());
    }

    public CronEditorWebComponent(final VelocityTemplatingEngine templatingEngine, final ApplicationProperties applicationProperties)
    {
        super(templatingEngine, applicationProperties);
        cronExpressionGenerator = new CronExpressionGenerator();
    }

    /**
     * Will produce HTML inputs for selecting a time period that will be represented by a cron string. The incoming
     * expression will populate the values of the form. This uses the default i18n bean to resolve i18n properties.
     *
     * @param cronEditorBean defines how the form inputs will be intialized.
     * @param paramPrefix prefix's the input names so that you can render multiple versions of this component on one
     * page.
     * @return HTML that represents the cron editor inputs.
     */
    public String getHtml(final CronEditorBean cronEditorBean, final String paramPrefix)
    {
        return getHtml(cronEditorBean, paramPrefix, null);

    }

    /**
     * Will produce HTML inputs for selecting a time period that will be represented by a cron string. The incoming
     * expression will populate the values of the form. This uses the default i18n bean to resolve i18n properties.
     *
     * @param cronEditorBean defines how the form inputs will be intialized.
     * @param paramPrefix prefix's the input names so that you can render multiple versions of this component on one
     * page.
     * @param  errorMessage the error message that was associated with the last cron expression. Can be null to indicate
     * that there was no error.
     * @return HTML that represents the cron editor inputs.
     */
    public String getHtml(final CronEditorBean cronEditorBean, final String paramPrefix, @Nullable String errorMessage)
    {
        final I18nHelper i18n = authenticationContext.getI18nHelper();
        return getHtml(cronEditorBean, i18n, paramPrefix, errorMessage);
    }

    /**
     * Will produce HTML inputs for selecting a time period that will be represented by a cron string. The incoming
     * expression will populate the values of the form.
     *
     * @param cronEditorBean defines how the form inputs will be intialized.
     * @param i18n used to resolve the i18n labeles.
     * @param paramPrefix prefixes the input names so that you can render multiple versions of this component on one
     * page.
     * @param  errorMessage the error message that was associated with the last cron expression. Can be null to indicate
     * that there was no error.
     * @return HTML that represents the cron editor inputs.
     */
    public String getHtml(final CronEditorBean cronEditorBean, final I18nHelper i18n, String paramPrefix,
            @Nullable String errorMessage)
    {
        try
        {
            UtilTimerStack.push("CronEditorHtml");

            // Make sure the paramPrefix ends with a DOT
            if ((paramPrefix != null) && !paramPrefix.endsWith(CronEditorBean.DOT))
            {
                paramPrefix += CronEditorBean.DOT;
            }

            // Include the js that we require
            final WebResourceManager webResourceManager = ComponentAccessor.getWebResourceManager();
            webResourceManager.requireResource("jira.webresources:croneditor");

            DateTimeFormatterFactory dateTimeFormatterFactory = ComponentAccessor.getComponentOfType(DateTimeFormatterFactory.class);
            final String currentTime = dateTimeFormatterFactory.formatter().withSystemZone().withStyle(DateTimeStyle.COMPLETE).format(new Date());
            final Map startingParams = EasyMap.build("cronEditorBean", cronEditorBean, "i18n", i18n, "paramPrefix", paramPrefix, "helpUtil",
                HelpUtil.getInstance(), "currentTime", currentTime, "timezone",
                new TimeZoneHelper().getDisplayName(authenticationContext.getLocale()),
                "errorMessage", errorMessage);
            final Map<String, Object> params = JiraVelocityUtils.getDefaultVelocityParams(startingParams, authenticationContext);
            return getHtml("templates/jira/cron/croneditor.vm", params);
        }
        finally
        {
            UtilTimerStack.pop("CronEditorHtml");
        }
    }

    static class TimeZoneHelper
    {
        private final TimeZone timeZone;

        TimeZoneHelper()
        {
            this(TimeZone.getDefault());
        }

        TimeZoneHelper(final TimeZone timeZone)
        {
            this.timeZone = timeZone;
        }

        String getDisplayName(final Locale locale)
        {
            return timeZone.getDisplayName(useDaylight(new Date()), TimeZone.LONG, locale);
        }

        boolean useDaylight(final Date date)
        {
            return timeZone.inDaylightTime(date);
        }

    }

    public ErrorCollection validateInput(final CronEditorBean cronEditorBean, final String fieldName)
    {
        // TODO: If this grows then it should probably move into a validator object
        final I18nHelper i18n = authenticationContext.getI18nHelper();
        final SimpleErrorCollection errors = new SimpleErrorCollection();
        // Ensure that the user has selected at least one day of week if in the day of week mode
        if (cronEditorBean.isDayPerWeekMode() && StringUtils.isBlank(cronEditorBean.getSpecifiedDaysPerWeek()))
        {
            errors.addError(fieldName, i18n.getText("cron.editor.error.daysOfWeek.must.have.selection"));
        }
        // Ensure that if the user has set an hour range that the from is less than the to time
        if (cronEditorBean.isRange() && !cronEditorBean.isRangeHoursValid())
        {
            errors.addError(fieldName, i18n.getText("cron.editor.error.from.hour.after.to.hour"));
        }
        return errors;
    }

    /**
     * This is a utility method that will process the parameters that the view put into the form and create a cron
     * string from the inputs. This cron string must be validated, there is no guarantee that this output is a valid
     * cron string.
     *
     * @param cronEditorBean holds the state of the submitted form
     * @return a cron string that represents the user inputs in cron format.
     */
    public String getCronExpressionFromInput(final CronEditorBean cronEditorBean)
    {
        return cronExpressionGenerator.getCronExpressionFromInput(cronEditorBean);
    }

}
