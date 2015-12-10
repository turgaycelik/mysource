package com.atlassian.jira.issue.statistics;

import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.issue.label.Label;
import com.atlassian.jira.issue.label.LabelComparator;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.SearchRequestAppender;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.util.SearchRequestAddendumBuilder;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.util.dbc.Assertions;

import org.apache.commons.lang.StringUtils;

import java.util.Comparator;

import static com.atlassian.jira.issue.search.util.SearchRequestAddendumBuilder.appendAndClause;
import static com.atlassian.jira.util.dbc.Assertions.notBlank;

/**
 * A stats mapper for Labels
 *
 * @since v4.2
 */
public class LabelsStatisticsMapper implements StatisticsMapper, SearchRequestAppender.Factory
{
    private final String clauseName;
    private final String indexedField;
    private final boolean includeEmpty;

    public LabelsStatisticsMapper(final boolean includeEmpty)
    {
        this(SystemSearchConstants.forLabels().getJqlClauseNames().getPrimaryName(),
                SystemSearchConstants.forLabels().getIndexField(), includeEmpty);
    }

    public LabelsStatisticsMapper(String clauseName, String indexedField, boolean includeEmpty)
    {
        this.clauseName = notBlank("clauseName", clauseName);
        this.indexedField = notBlank("indexedField", indexedField);
        this.includeEmpty = includeEmpty;
    }

    @Override
    public Comparator getComparator()
    {
        return LabelComparator.INSTANCE;
    }

    @Override
    public boolean isValidValue(Object value)
    {
        return value != null || includeEmpty;
    }

    @Override
    public Object getValueFromLuceneField(String documentValue)
    {
        if (StringUtils.isEmpty(documentValue) || FieldIndexer.LABELS_NO_VALUE_INDEX_VALUE.equals(documentValue))
        {
            return null;
        }
        else
        {
            return new Label(null, null, documentValue);
        }
    }

    @Override
    public boolean isFieldAlwaysPartOfAnIssue()
    {
        return true;
    }

    /**
     * @deprecated Use #getSearchRequestAppender().appendInclusiveSingleValueClause()
     */
    @Override
    @Deprecated
    public SearchRequest getSearchUrlSuffix(Object value, SearchRequest searchRequest)
    {
        return getSearchRequestAppender().appendInclusiveSingleValueClause(value, searchRequest);
    }

    /**
     * @since v6.0
     */
    @Override
    public SearchRequestAppender getSearchRequestAppender()
    {
        return new LabelsSearchRequestAppender(getClauseName());
    }

    protected String getClauseName()
    {
        return clauseName;
    }

    @Override
    public String getDocumentConstant()
    {
        return indexedField;
    }

    static class LabelsSearchRequestAppender
            implements SearchRequestAddendumBuilder.AddendumCallback<Label>, SearchRequestAppender<Label>
    {

        private final String clauseName;

        public LabelsSearchRequestAppender(String clauseName)
        {
            this.clauseName = Assertions.notNull(clauseName);
        }

        @Override
        public void appendNonNullItem(Label label, JqlClauseBuilder clauseBuilder)
        {
            clauseBuilder.labels(label.getLabel());
        }

        @Override
        public void appendNullItem(JqlClauseBuilder clauseBuilder)
        {
            clauseBuilder.addEmptyCondition(clauseName);
        }

        @Override
        public SearchRequest appendInclusiveSingleValueClause(Label value, SearchRequest searchRequest)
        {
            return appendAndClause(value, searchRequest, this);
        }

        @Override
        public SearchRequest appendExclusiveMultiValueClause(Iterable values, SearchRequest searchRequest)
        {
            // Because of the many-to-many relationship between issues and labels, the AND NOT(.. OR .. OR ..) approach
            // doesn't work, so we just return null here. See https://jira.atlassian.com/browse/JRA-24210
            return null;
        }
    }
}