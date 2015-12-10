package com.atlassian.jira.jql.context;

import com.atlassian.annotations.PublicApi;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Represents a context of projects and issuetypes generated from a search query.
 *
 * @since v4.0
 */
@PublicApi
public interface QueryContext
{
    Collection<ProjectIssueTypeContexts> getProjectIssueTypeContexts();

    class ProjectIssueTypeContexts
    {
        private final ProjectContext project;

        private final Collection<IssueTypeContext> issueTypes;

        public ProjectIssueTypeContexts(ProjectContext  project, Collection<IssueTypeContext> issueTypes)
        {
            this.project = project;
            this.issueTypes = issueTypes;
        }

        public ProjectContext getProjectContext()
        {
            return project;
        }

        public List<Long> getProjectIdInList()
        {
            if (project.isAll())
            {
                return Collections.emptyList();
            }
            return Collections.singletonList(project.getProjectId());
        }

        public List<String> getIssueTypeIds()
        {
            List<String> issueTypeIds = new ArrayList<String>();
            for (IssueTypeContext issueType : issueTypes)
            {
                if (!issueType.isAll())
                {
                    issueTypeIds.add(issueType.getIssueTypeId());
                }
            }
            return issueTypeIds;
        }

        public Collection<IssueTypeContext> getIssueTypeContexts()
        {
            return issueTypes;
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            final ProjectIssueTypeContexts contexts = (ProjectIssueTypeContexts) o;

            if (issueTypes != null ? !issueTypes.equals(contexts.issueTypes) : contexts.issueTypes != null)
            {
                return false;
            }
            if (project != null ? !project.equals(contexts.project) : contexts.project != null)
            {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = project != null ? project.hashCode() : 0;
            result = 31 * result + (issueTypes != null ? issueTypes.hashCode() : 0);
            return result;
        }

        @Override
        public String toString()
        {
            return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).
                    append("project", project).
                    append("issueTypes", issueTypes).
                    toString();
        }
    }
}
