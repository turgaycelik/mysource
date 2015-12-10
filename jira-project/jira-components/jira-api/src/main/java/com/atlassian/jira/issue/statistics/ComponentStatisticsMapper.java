package com.atlassian.jira.issue.statistics;

import com.atlassian.jira.bc.EntityNotFoundException;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.SearchRequestAppender;
import com.atlassian.jira.issue.search.constants.SimpleFieldSearchConstantsWithEmpty;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.util.SearchRequestAddendumBuilder;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.query.clause.Clause;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import java.util.Comparator;

import static com.atlassian.jira.issue.comparator.ComponentComparator.COMPARATOR;
import static com.atlassian.jira.issue.search.util.SearchRequestAddendumBuilder.appendAndClause;

public class ComponentStatisticsMapper implements StatisticsMapper, SearchRequestAppender.Factory
{
    private static final Logger log = Logger.getLogger(ComponentStatisticsMapper.class);
    private final SimpleFieldSearchConstantsWithEmpty searchConstants = SystemSearchConstants.forComponent();

    @Override
    public String getDocumentConstant()
    {
        return searchConstants.getIndexField();
    }

    @Override
    public Comparator getComparator()
    {
        return COMPARATOR;
    }

    @Override
    public boolean isValidValue(Object value)
    {
        return true;
    }

    @Override
    public boolean isFieldAlwaysPartOfAnIssue()
    {
        return true;
    }

    @Override
    public Object getValueFromLuceneField(String documentValue)
    {
        if (documentValue == null)
        {
            return null;
        }
        long componentId = Long.parseLong(documentValue);
        if (componentId > 0)
        {
            try
            {
                // Retrieve current version of ProjectComponentManager
                ProjectComponentManager projectComponentManager = getProjectComponentManager();
                return projectComponentManager.convertToGenericValue(projectComponentManager.find(componentId));
            }
            catch (EntityNotFoundException e)
            {
                log.error("Indexes may be corrupt - unable to retrieve component with id '" + componentId + "'.");
            }
        }
        return null;
    }

    protected ProjectComponentManager getProjectComponentManager()
    {
        return ComponentAccessor.getComponent(ProjectComponentManager.class);
    }

    protected ProjectManager getProjectManager()
    {
        return ComponentAccessor.getComponent(ProjectManager.class);
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
        return new ComponentSearchRequestAppender(getProjectComponentManager(), getProjectManager());
    }

    protected Clause getComponentClause(Long value)
    {
        return JqlQueryBuilder.newBuilder().where().component(value).buildClause();
    }

    protected Clause getProjectClause(Long value)
    {
        return JqlQueryBuilder.newBuilder().where().project(value).buildClause();
    }

    @Override
    public int hashCode()
    {
        return this.getDocumentConstant().hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }

        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }

        ComponentStatisticsMapper that = (ComponentStatisticsMapper) obj;

        return this.getDocumentConstant().equals(that.getDocumentConstant());
    }


    static class ComponentSearchRequestAppender
            implements SearchRequestAddendumBuilder.AddendumCallback<GenericValue>, SearchRequestAppender<GenericValue>
    {

        private final ProjectComponentManager projectComponentManager;
        private final ProjectManager projectManager;

        public ComponentSearchRequestAppender(ProjectComponentManager projectComponentManager, ProjectManager projectManager)
        {
            this.projectComponentManager = Assertions.notNull(projectComponentManager);
            this.projectManager = Assertions.notNull(projectManager);
        }

        @Override
        public void appendNonNullItem(GenericValue value, JqlClauseBuilder clauseBuilder)
        {
            final Long id = value.getLong("id");
            final Long projectId = value.getLong("project");

            try
            {
                final ProjectComponent projectComponent = projectComponentManager.find(id);
                final String componentName = projectComponent.getName();
                final Project project = projectManager.getProjectObj(projectId);

                clauseBuilder.sub().component(componentName).and().project(project.getKey()).endsub();
            }
            catch (EntityNotFoundException e)
            {
                log.error("Unable to retrieve component with id '" + id + "'.");
                clauseBuilder.component(id).project(projectId);
            }
        }

        @Override
        public void appendNullItem(JqlClauseBuilder clauseBuilder)
        {
            clauseBuilder.componentIsEmpty();
        }

        @Override
        public SearchRequest appendInclusiveSingleValueClause(GenericValue value, SearchRequest searchRequest)
        {
            return appendAndClause(value, searchRequest, this);
        }

        @Override
        public SearchRequest appendExclusiveMultiValueClause(Iterable values, SearchRequest searchRequest)
        {
            // Because of the many-to-many relationship between issues and components, the AND NOT(.. OR .. OR ..) approach
            // doesn't work, so we just return null here. See https://jira.atlassian.com/browse/JRA-24210
            return null;
        }
    }
}