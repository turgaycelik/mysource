package com.atlassian.jira.plugin.viewissue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.datetime.DateTimeStyle;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.impl.AbstractCustomFieldType;
import com.atlassian.jira.issue.customfields.impl.DateCFType;
import com.atlassian.jira.issue.customfields.impl.DateTimeCFType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.FieldRenderingContext;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderer;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.fields.util.FieldPredicates;
import com.atlassian.jira.issue.operation.IssueOperations;
import com.atlassian.jira.plugin.webfragment.CacheableContextProvider;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.DateFieldFormat;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.plugin.PluginParseException;
import com.google.common.collect.Lists;
import webwork.action.Action;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

/**
 * Context Provider for Date Block on View Issue page.
 *
 * @since v4.4
 */
public class DateBlockContextProvider implements CacheableContextProvider
{
    private final FieldVisibilityManager fieldVisibilityManager;
    private final JiraAuthenticationContext authenticationContext;
    private final FieldScreenRendererFactory fieldScreenRendererFactory;

    /**
     * The formatter for date/time values, in the user's time zone with the user's locale.
     */
    private final DateTimeFormatter dateTimeFormater;

    /**
     * The formatter for dates, in the default JIRA time zone with the user's locale.
     */
    private final DateFieldFormat dateFieldFormat;

    public DateBlockContextProvider(FieldVisibilityManager fieldVisibilityManager, JiraAuthenticationContext authenticationContext,
            DateTimeFormatterFactory dateTimeFormatterFactory, FieldScreenRendererFactory fieldScreenRendererFactory,
            DateFieldFormat dateFieldFormat)
    {
        this.fieldVisibilityManager = fieldVisibilityManager;
        this.authenticationContext = authenticationContext;
        this.fieldScreenRendererFactory = fieldScreenRendererFactory;
        this.dateFieldFormat = dateFieldFormat;

        // prepare the needed formatter
        dateTimeFormater = dateTimeFormatterFactory.formatter().forLoggedInUser();
    }

    @Override
    public void init(Map<String, String> params) throws PluginParseException
    {
    }

    @Override
    public Map<String, Object> getContextMap(Map<String, Object> context)
    {
        final Issue issue = (Issue) context.get("issue");
        final Action action = (Action) context.get("action");

        final MapBuilder<String, Object> paramsBuilder = MapBuilder.newBuilder(context);

        final List<DateBlockField> dates = Lists.newArrayList();

        final Timestamp dueDate = issue.getDueDate();
        if (dueDate != null && !fieldVisibilityManager.isFieldHidden(IssueFieldConstants.DUE_DATE, issue))
        {
            dates.add(new DateField("due-date", "issue.field.due", dueDate));
        }

        dates.add(new DateTimeField("create-date", "issue.field.created", issue.getCreated()));
        dates.add(new DateTimeField("updated-date", "issue.field.updated", issue.getUpdated()));
        final Timestamp resolutionDate = issue.getResolutionDate();
        if (resolutionDate != null)
        {
            dates.add(new DateTimeField("resolved-date", "issue.field.resolution.date", resolutionDate));
        }

        dates.addAll(getDateCustomFields(issue, action));

        paramsBuilder.add("dates", dates);

        return paramsBuilder.toMap();
    }

    @Override
    public String getUniqueContextKey(Map<String, Object> context)
    {
        final Issue issue = (Issue) context.get("issue");
        final User user = authenticationContext.getLoggedInUser();

        return issue.getId() + "/" + (user == null ? "" : user.getName());
    }

    private List<DateBlockField> getDateCustomFields(Issue issue, Action action)
    {
        MapBuilder<String, Object> paramBuilder = MapBuilder.newBuilder();
        paramBuilder.add(FieldRenderingContext.ISSUE_VIEW, true);
        Map<String, Object> params = paramBuilder.toMap();


        FieldScreenRenderer screenRenderer = fieldScreenRendererFactory.getFieldScreenRenderer(issue, IssueOperations.VIEW_ISSUE_OPERATION, FieldPredicates.isCustomDateField());

        final List<FieldScreenRenderLayoutItem> dateRenderItems = screenRenderer.getAllScreenRenderItems();

        List<DateBlockField> customfields = Lists.newArrayListWithExpectedSize(dateRenderItems.size());
        for (FieldScreenRenderLayoutItem renderItem : dateRenderItems)
        {
            CustomField customField = (CustomField) renderItem.getOrderableField();
            String displayHtml = customField.getViewHtml(renderItem.getFieldLayoutItem(), action, issue, params);
            String fieldType = customField.getCustomFieldType().getDescriptor().getKey();
            String fieldTypeCompleteKey = customField.getCustomFieldType().getDescriptor().getCompleteKey();

            customfields.add(new DateBlockField(customField.getId() + "-val", customField.getName(), null, displayHtml, null, null, fieldType, fieldTypeCompleteKey));
        }

        return customfields;
    }

    protected String getI18nText(String key)
    {
        return authenticationContext.getI18nHelper().getText(key);
    }

    protected String formatDate(Date date)
    {
        return escapeHtml(dateFieldFormat.format(date));
    }

    protected String formatDateIso8601(Date date)
    {
        return escapeHtml(dateTimeFormater.withSystemZone().withStyle(DateTimeStyle.ISO_8601_DATE).format(date));
    }

    private String formatDateTime(Date dateTime)
    {
        return escapeHtml(dateTimeFormater.format(dateTime));
    }

    protected String formatDateTimeIso8601(Date dateTime)
    {
        return escapeHtml(dateTimeFormater.withStyle(DateTimeStyle.ISO_8601_DATE_TIME).format(dateTime));
    }

    protected String formatDateTimeTitle(Date dateTime)
    {
        return escapeHtml(dateTimeFormater.withStyle(DateTimeStyle.COMPLETE).format(dateTime));
    }

    /**
     * A system date field that is backed by a Timestamp.
     */
    class DateField extends DateBlockField
    {
        DateField(String id, String key, Timestamp date)
        {
            super(id, getI18nText(key), null, formatDate(date), formatDateIso8601(date), formatDate(date), "datepicker", null);
        }
    }

    /**
     * A system date/time field that is backed by a Timestamp.
     */
    class DateTimeField extends DateBlockField
    {
        public DateTimeField(String id, String key, Timestamp dateTime)
        {
            super(id, getI18nText(key), "user-tz", formatDateTime(dateTime), formatDateTimeIso8601(dateTime), formatDateTimeTitle(dateTime), "datetime", null);
        }
    }

    abstract class CustomFieldVisitor implements AbstractCustomFieldType.Visitor<String>, DateCFType.Visitor<String>, DateTimeCFType.Visitor<String>
    {
        private final CustomField field;
        private final Issue issue;

        CustomFieldVisitor(CustomField field, Issue issue)
        {
            this.field = field;
            this.issue = issue;
        }

        @Override
        public String visit(AbstractCustomFieldType customFieldType)
        {
            return null;
        }

        Date getDate(CustomFieldType dateTimeCFType)
        {
            return (Date) dateTimeCFType.getValueFromIssue(field, issue);
        }
    }

    /**
     * Calculates the "title" value for date custom fields.
     */
    class TitleVisitor extends CustomFieldVisitor
    {
        TitleVisitor(CustomField field, Issue issue)
        {
            super(field, issue);
        }

        @Override
        public String visitDate(DateCFType dateCustomFieldType)
        {
            return formatDate(getDate(dateCustomFieldType));
        }

        @Override
        public String visitDateTime(DateTimeCFType dateTimeCFType)
        {
            return formatDateTimeTitle(getDate(dateTimeCFType));
        }
    }

    /**
     * Calculates the "iso8601" value for date custom fields.
     */
    class Iso8601Visitor extends CustomFieldVisitor
    {
        Iso8601Visitor(CustomField field, Issue issue)
        {
            super(field, issue);
        }

        @Override
        public String visitDate(DateCFType dateCustomFieldType)
        {
            return formatDateIso8601(getDate(dateCustomFieldType));
        }

        @Override
        public String visitDateTime(DateTimeCFType dateTimeCFType)
        {
            return formatDateTimeIso8601(getDate(dateTimeCFType));
        }
    }
}
