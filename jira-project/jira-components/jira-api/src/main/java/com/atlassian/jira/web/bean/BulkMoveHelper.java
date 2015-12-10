package com.atlassian.jira.web.bean;

import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.util.Function;

import java.util.Map;
import java.util.Collection;

/**
 * Interface for methods to help out with some calculations for Bulk Move operations.
 *
 * @since v4.1
 */
public interface BulkMoveHelper
{
    /**
     * For the given field, calculates all the distinct values present in the currently selected issues of the BulkEditBean.
     * These values are represented by a mapping from key (Long representing the id) to {@link DistinctValueResult} which provides
     * the name of the value as well as the name of the project the value belongs to. This information is used in the rendering
     * of the mapping controls to the user.
     *
     * @param bulkEditBean the bulk edit bean
     * @param orderableField the field to retrieve distinct values from
     * @param issueValueResolution must return a non-null Collection of values based on a given issue. It is implied that the values are for the field specified by orderableField.
     * @param nameResolution a function to resolve values to names to be displayed. May return null to indicate that it should not be displayed.
     * @return a mapping from values (ids in the form of Longs) to [name, project name] pairs.
     */
    Map<Long, DistinctValueResult> getDistinctValuesForMove(BulkEditBean bulkEditBean, final OrderableField orderableField, final Function<Issue, Collection<Object>> issueValueResolution, Function<Object, String> nameResolution);

    /**
     * Method called by Velocity templates to check if the current DistinctValueResult matches the value specified and thus should be selected.
     *
     * @param distinctValue the distinct value
     * @param id Option Id
     * @param valueName Option Name
     * @return true if the specified option should be selected
     */
    public boolean needsSelection(DistinctValueResult distinctValue, Long id, String valueName);

    /**
     * Represents a distinct project attribute value (i.e. Component or Version).
     */
    public static class DistinctValueResult
    {
        private final String valueName;
        private final String projectName;
        private final Long previouslySelectedValue;
        private final boolean isPreviousValueSpecified;

        public DistinctValueResult()
        {
            this("", "", null, false);
        }

        public DistinctValueResult(final String valueName, final String projectName, final Long previouslySelectedValue, final boolean isPreviousValueSpecified)
        {
            this.valueName = valueName;
            this.projectName = projectName;
            this.previouslySelectedValue = previouslySelectedValue;
            this.isPreviousValueSpecified = isPreviousValueSpecified;
        }

        public String getValueName()
        {
            return valueName;
        }

        public String getProjectName()
        {
            return projectName;
        }

        /**
         * @return the previously selected value; may return null if the previous value selected was "Unknown".
         */
        public Long getPreviouslySelectedValue()
        {
            return previouslySelectedValue;
        }

        /**
         * @return true if a value was previously specified (null meaning "Unknown"); false otherwise.
         */
        public boolean isPreviousValueSpecified()
        {
            return isPreviousValueSpecified;
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

            final DistinctValueResult that = (DistinctValueResult) o;

            if (isPreviousValueSpecified != that.isPreviousValueSpecified)
            {
                return false;
            }
            if (previouslySelectedValue != null ? !previouslySelectedValue.equals(that.previouslySelectedValue) : that.previouslySelectedValue != null)
            {
                return false;
            }
            if (projectName != null ? !projectName.equals(that.projectName) : that.projectName != null)
            {
                return false;
            }
            if (valueName != null ? !valueName.equals(that.valueName) : that.valueName != null)
            {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = valueName != null ? valueName.hashCode() : 0;
            result = 31 * result + (projectName != null ? projectName.hashCode() : 0);
            result = 31 * result + (previouslySelectedValue != null ? previouslySelectedValue.hashCode() : 0);
            result = 31 * result + (isPreviousValueSpecified ? 1 : 0);
            return result;
        }
    }

}
