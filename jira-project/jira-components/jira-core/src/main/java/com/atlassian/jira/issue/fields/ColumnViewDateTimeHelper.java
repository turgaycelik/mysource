package com.atlassian.jira.issue.fields;

import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.datetime.DateTimeStyle;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.collect.CompositeMap;
import org.apache.commons.collections.map.Flat3Map;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

/**
 * Helper class for date-related system fields.
 *
 * @since 4.4
 */
public class ColumnViewDateTimeHelper
{
    /**
     * The display parameter that indicates whether we're displaying the Excel view.
     */
    private static final String EXCEL_VIEW = "excel_view";

    /**
     * The name of the template used to render the field in Excel view.
     */
    private static final String EXCEL_VIEW_TEMPLATE = "date-excelview.vm";

    /**
     * The name of the template used to render the field in the Issue Navigator.
     */
    private static final String ISSUE_NAV_TEMPLATE = "date-columnview.vm";

    private final DateTimeFormatter dateTimeFormatter;
    private final JiraAuthenticationContext authenticationContext;

    public ColumnViewDateTimeHelper(DateTimeFormatterFactory dateTimeFormatterFactory, JiraAuthenticationContext authenticationContext)
    {
        this.authenticationContext = authenticationContext;
        this.dateTimeFormatter = dateTimeFormatterFactory.formatter().forLoggedInUser();
    }

    /**
     * Renders a given date/time in column view. Depending on the displayParams, this can either the Excel view or the
     * regular issue navigator view.
     *
     * @return a String containing rendered date/time
     */
    String render(NavigableFieldImpl field, FieldLayoutItem fieldLayoutItem, Map displayParams, Issue issue, @Nullable Date date)
    {
        // these aren't really used by our templates, but i'm leaving them here in case people have overriden the
        // templates and are relying on these params.
        Map<String, Object> legacyVelocityParams = field.getVelocityParams(fieldLayoutItem, authenticationContext.getI18nHelper(), displayParams, issue);

        // the new templates only use these 3 context values
        Map<String, Object> dateVelocityParams = Collections.emptyMap();
        if (date != null)
        {
            DateTimeStyle displayStyle = displayParams.containsKey(EXCEL_VIEW) ? DateTimeStyle.COMPLETE : DateTimeStyle.DATE;
            dateVelocityParams = new Flat3Map();
            dateVelocityParams.put("title", dateTimeFormatter.withStyle(DateTimeStyle.COMPLETE).format(date));
            dateVelocityParams.put("iso8601", dateTimeFormatter.withStyle(DateTimeStyle.ISO_8601_DATE_TIME).format(date));
            dateVelocityParams.put("value", dateTimeFormatter.withStyle(displayStyle).format(date));
        }

        return field.renderTemplate(displayParams.containsKey(EXCEL_VIEW) ? EXCEL_VIEW_TEMPLATE : ISSUE_NAV_TEMPLATE, CompositeMap.of(legacyVelocityParams, dateVelocityParams));
    }
}
