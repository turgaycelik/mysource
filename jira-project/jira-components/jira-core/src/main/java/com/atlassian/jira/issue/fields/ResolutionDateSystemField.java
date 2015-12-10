package com.atlassian.jira.issue.fields;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
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
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.issue.search.SearchHandler;
import com.atlassian.jira.issue.search.handlers.ResolutionDateSearchHandlerFactory;
import com.atlassian.jira.issue.search.handlers.SearchHandlerFactory;
import com.atlassian.jira.issue.search.parameters.lucene.sort.StringSortComparator;
import com.atlassian.jira.issue.statistics.DateFieldSorter;
import com.atlassian.jira.rest.Dates;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import org.apache.lucene.search.SortField;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Stores the date an issue was resolved on.  If an issue is changed back into the unresolved state, this
 * field returns nothing again.
 *
 * @since v4.0
 */
public class ResolutionDateSystemField extends NavigableFieldImpl implements SearchableField, DateField, RestAwareField
{
    private final SearchHandlerFactory searchHandlerFactory;
    private final ColumnViewDateTimeHelper columnViewDateTimeHelper;
    private final DateTimeFormatterFactory dateTimeFormatterFactory;

    public ResolutionDateSystemField(VelocityTemplatingEngine templatingEngine, ApplicationProperties applicationProperties, JiraAuthenticationContext authenticationContext, ResolutionDateSearchHandlerFactory searchHandlerFactory, ColumnViewDateTimeHelper columnViewDateTimeHelper, DateTimeFormatterFactory dateTimeFormatterFactory)
    {
        super(IssueFieldConstants.RESOLUTION_DATE, "issue.field.resolution.date", "issue.column.heading.resolution.date", ORDER_DESCENDING, templatingEngine, applicationProperties, authenticationContext);
        this.searchHandlerFactory = searchHandlerFactory;
        this.columnViewDateTimeHelper = columnViewDateTimeHelper;
        this.dateTimeFormatterFactory = dateTimeFormatterFactory;
    }

    public LuceneFieldSorter getSorter()
    {
        return DateFieldSorter.ISSUE_RESOLUTION_DATE_STATSMAPPER;
    }

    @Override
    public List<SortField> getSortFields(final boolean sortOrder)
    {
        return Collections.singletonList(new SortField(DocumentConstants.ISSUE_SORT_RESOLUTION_DATE, new StringSortComparator(), sortOrder));
    }

    public String getColumnViewHtml(FieldLayoutItem fieldLayoutItem, Map displayParams, Issue issue)
    {
        return columnViewDateTimeHelper.render(this, fieldLayoutItem, displayParams, issue, issue.getResolutionDate());
    }

    public SearchHandler createAssociatedSearchHandler()
    {
        return searchHandlerFactory.createHandler(this);
    }

    @Override
    public FieldTypeInfo getFieldTypeInfo(FieldTypeInfoContext fieldTypeInfoContext)
    {
        return new FieldTypeInfo(null, null);
    }

    @Override
    public JsonType getJsonSchema()
    {
        return JsonTypeBuilder.system(JsonType.DATETIME_TYPE, IssueFieldConstants.RESOLUTION_DATE);
    }

    @Override
    public FieldJsonRepresentation getJsonFromIssue(Issue issue, boolean renderedVersionRequired, FieldLayoutItem fieldLayoutItem)
    {
        Timestamp resolutionDate = issue.getResolutionDate();
        FieldJsonRepresentation fieldJsonRepresentation = new FieldJsonRepresentation(new JsonData(Dates.asTimeString(resolutionDate)));
        if (renderedVersionRequired && resolutionDate != null)
        {
            fieldJsonRepresentation.setRenderedData(new JsonData(dateTimeFormatterFactory.formatter().forLoggedInUser().format(resolutionDate)));
        }
        return fieldJsonRepresentation;
    }
}