package com.atlassian.jira.issue.fields;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.datetime.DateTimeStyle;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.rest.FieldJsonRepresentation;
import com.atlassian.jira.issue.fields.rest.FieldTypeInfo;
import com.atlassian.jira.issue.fields.rest.FieldTypeInfoContext;
import com.atlassian.jira.issue.fields.rest.RestAwareField;
import com.atlassian.jira.issue.fields.rest.json.JsonData;
import com.atlassian.jira.issue.fields.rest.json.JsonType;
import com.atlassian.jira.issue.fields.rest.json.JsonTypeBuilder;
import com.atlassian.jira.issue.search.IssueComparator;
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.issue.search.parameters.lucene.sort.DefaultIssueSortComparator;
import com.atlassian.jira.issue.search.parameters.lucene.sort.DocumentSortComparatorSource;
import com.atlassian.jira.issue.search.parameters.lucene.sort.UserHistoryFieldComparatorSource;
import com.atlassian.jira.issue.statistics.DateFieldSorter;
import com.atlassian.jira.issue.statistics.util.LongComparator;
import com.atlassian.jira.rest.Dates;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.user.UserHistoryItem;
import com.atlassian.jira.user.UserIssueHistoryManager;
import com.atlassian.jira.util.collect.CompositeMap;
import com.atlassian.jira.util.collect.MapBuilder;
import org.apache.commons.collections.map.Flat3Map;
import org.apache.lucene.search.FieldComparatorSource;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Map;


public class LastViewedSystemField extends NavigableFieldImpl implements DateField, RestAwareField
{
    private final ColumnViewDateTimeHelper columnViewDateTimeHelper;
    private final DateTimeFormatterFactory dateTimeFormatterFactory;
    private final UserIssueHistoryManager historyManager;

    public LastViewedSystemField(VelocityTemplatingEngine templatingEngine, ApplicationProperties applicationProperties,
            JiraAuthenticationContext authenticationContext, ColumnViewDateTimeHelper columnViewDateTimeHelper,
            DateTimeFormatterFactory dateTimeFormatterFactory, UserIssueHistoryManager historyManager)
    {
        super(IssueFieldConstants.LAST_VIEWED, "issue.field.lastviewed", "issue.column.heading.lastviewed", ORDER_DESCENDING, templatingEngine, applicationProperties, authenticationContext);
        this.columnViewDateTimeHelper = columnViewDateTimeHelper;
        this.dateTimeFormatterFactory = dateTimeFormatterFactory;
        this.historyManager = historyManager;
    }

    @Override
    public FieldComparatorSource getSortComparatorSource()
    {
        final List<UserHistoryItem> fullHistory = historyManager.getFullIssueHistoryWithoutPermissionChecks(authenticationContext.getLoggedInUser());

        return new UserHistoryFieldComparatorSource(fullHistory);
    }

    @Override
    public LuceneFieldSorter getSorter()
    {
        return DateFieldSorter.ISSUE_LAST_VIEWED_DATE_STATSMAPPER;
    }

    public String getColumnViewHtml(FieldLayoutItem fieldLayoutItem, Map displayParams, Issue issue)
    {
        // we want to show relative dates so we can't just piggy back on the columnViewhelper
        final Timestamp date = getTimestampForIssue(issue.getId());

        if (displayParams.containsKey("excel_view"))
        {
            return columnViewDateTimeHelper.render(this, fieldLayoutItem, displayParams, issue, date);
        }
        else
        {
            final DateTimeFormatter dateTimeFormatter = dateTimeFormatterFactory.formatter().forLoggedInUser();
            final MapBuilder<String, Object> builder = MapBuilder.newBuilder();
            if (date != null)
            {
                dateTimeFormatterFactory.formatter().forLoggedInUser();
                builder.add("title", dateTimeFormatter.withStyle(DateTimeStyle.COMPLETE).format(date));
                builder.add("iso8601", dateTimeFormatter.withStyle(DateTimeStyle.ISO_8601_DATE_TIME).format(date));
                builder.add("value", dateTimeFormatter.withStyle(DateTimeStyle.RELATIVE_ALWAYS_WITH_TIME).format(date));
            }

            return renderTemplate("date-columnview.vm", builder.toMap());
        }

    }

    private Timestamp getTimestampForIssue(Long id)
    {
        final Long longTimestampForIssue = getLongTimestampForIssue(id);

        return longTimestampForIssue == null ? null : new Timestamp(longTimestampForIssue);

    }

    private Long getLongTimestampForIssue(Long id)
    {
        // This should be in memory all ready so shouldn't be expensive
        final List<UserHistoryItem> fullHistory = historyManager.getFullIssueHistoryWithoutPermissionChecks(authenticationContext.getLoggedInUser());
        for (UserHistoryItem historyItem : fullHistory)
        {
            if (id.toString().equals(historyItem.getEntityId()))
            {
                return historyItem.getLastViewed();
            }
        }
        return null;
    }

    @Override
    public FieldTypeInfo getFieldTypeInfo(FieldTypeInfoContext fieldTypeInfoContext)
    {
        return new FieldTypeInfo(null, null);
    }

    @Override
    public JsonType getJsonSchema()
    {
        return JsonTypeBuilder.system(JsonType.DATETIME_TYPE, getId());
    }

    @Override
    public FieldJsonRepresentation getJsonFromIssue(Issue issue, boolean renderedVersionRequired, FieldLayoutItem fieldLayoutItem)
    {
        final Timestamp timestamp = getTimestampForIssue(issue.getId());

        FieldJsonRepresentation fieldJsonRepresentation = new FieldJsonRepresentation(new JsonData(Dates.asTimeString(timestamp)));
        if (renderedVersionRequired && timestamp != null)
        {
            fieldJsonRepresentation.setRenderedData(new JsonData(dateTimeFormatterFactory.formatter().forLoggedInUser().format(timestamp)));
        }
        return fieldJsonRepresentation;
    }


}
