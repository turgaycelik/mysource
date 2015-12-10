package com.atlassian.jira.issue.customfields.statistics;

import com.atlassian.jira.issue.comparator.OfBizComparators;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.SearchRequestAppender;
import com.atlassian.jira.project.ProjectManager;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.GenericValue;

import java.util.Comparator;

/**
 * A statistics mapper for project select custom fields.
 */
public class ProjectSelectStatisticsMapper extends AbstractCustomFieldStatisticsMapper
        implements SearchRequestAppender.Factory
{
    private ProjectManager projectManager;

    public ProjectSelectStatisticsMapper(CustomField customField, ProjectManager projectManager)
    {
        super(customField);
        this.projectManager = projectManager;
    }

    protected String getSearchValue(Object value)
    {
        GenericValue project = (GenericValue) value;
        return project.getString("id");
    }

    public Object getValueFromLuceneField(String documentValue)
    {
        if (StringUtils.isBlank(documentValue))
        {
            return null;
        }
        else
        {
            return projectManager.getProject(new Long(documentValue));
        }
    }

    public Comparator getComparator()
    {
        return OfBizComparators.NAME_COMPARATOR;
    }

    /**
     * @since v6.0
     */
    @Override
    public SearchRequestAppender getSearchRequestAppender()
    {
        return new CustomFieldSearchRequestAppender(customField, this);
    }
}