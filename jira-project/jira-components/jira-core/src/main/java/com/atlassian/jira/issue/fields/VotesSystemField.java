package com.atlassian.jira.issue.fields;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
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
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.issue.fields.rest.json.beans.VotesJsonBean;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.issue.search.parameters.lucene.sort.StringSortComparator;
import com.atlassian.jira.issue.statistics.VotesStatisticsMapper;
import com.atlassian.jira.issue.vote.VoteManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import org.apache.lucene.search.SortField;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: owenfellows
 * Date: 05-Aug-2004
 * Time: 09:24:27
 * To change this template use File | Settings | File Templates.
 */
public class VotesSystemField extends NavigableFieldImpl implements RestAwareField
{
    private final JiraBaseUrls jiraBaseUrls;

    public VotesSystemField(VelocityTemplatingEngine templatingEngine, ApplicationProperties applicationProperties, JiraAuthenticationContext authenticationContext, JiraBaseUrls jiraBaseUrls)
    {
        super(IssueFieldConstants.VOTES, "issue.field.vote", "issue.column.heading.vote", NavigableField.ORDER_DESCENDING, templatingEngine, applicationProperties, authenticationContext);
        this.jiraBaseUrls = jiraBaseUrls;
    }

    public LuceneFieldSorter getSorter()
    {
        return VotesStatisticsMapper.MAPPER;
    }

    @Override
    public List<SortField> getSortFields(final boolean sortOrder)
    {
        return Collections.singletonList(new SortField(DocumentConstants.ISSUE_VOTES, new StringSortComparator(), sortOrder));
    }

    public String getColumnViewHtml(FieldLayoutItem fieldLayoutItem, Map displayParams, Issue issue)
    {
        Map velocityParams = getVelocityParams(fieldLayoutItem, getAuthenticationContext().getI18nHelper(), displayParams, issue);
        velocityParams.put(getId(), issue.getVotes());
        return renderTemplate("votes-columnview.vm", velocityParams);
    }

    @Override
    public FieldTypeInfo getFieldTypeInfo(FieldTypeInfoContext fieldTypeInfoContext)
    {
        return new FieldTypeInfo(null, null);
    }

    @Override
    public JsonType getJsonSchema()
    {
        return JsonTypeBuilder.systemArray(JsonType.VOTES_TYPE, IssueFieldConstants.VOTES);
    }

    @Override
    public FieldJsonRepresentation getJsonFromIssue(Issue issue, boolean renderedVersionRequired, FieldLayoutItem fieldLayoutItem)
    {
        VoteManager voteManager = ComponentAccessor.getVoteManager();
        if (voteManager.isVotingEnabled())
        {
            return new FieldJsonRepresentation(new JsonData(VotesJsonBean.shortBean(issue.getKey(), issue.getVotes(), voteManager.hasVoted(authenticationContext.getLoggedInUser(), issue), jiraBaseUrls)));
        }
        return null;
    }
}
