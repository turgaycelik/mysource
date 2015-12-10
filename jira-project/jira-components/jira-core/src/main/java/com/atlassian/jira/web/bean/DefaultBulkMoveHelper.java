package com.atlassian.jira.web.bean;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.util.Function;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Default implementation of the {@link BulkMoveHelper} interface.
 *
 * @since v4.1
 */
public class DefaultBulkMoveHelper implements BulkMoveHelper
{
    private final BulkEditBeanSessionHelper bulkEditBeanSessionHelper = new BulkEditBeanSessionHelper();

    protected BulkEditBean getBulkEditBeanFromSession()
    {
        return bulkEditBeanSessionHelper.getFromSession();
    }

    public Map<Long, DistinctValueResult> getDistinctValuesForMove(final BulkEditBean bulkEditBean, final OrderableField orderableField, final Function<Issue, Collection<Object>> issueValueResolution, final Function<Object, String> nameResolution)
    {
        final FieldLayoutItem targetFieldLayoutItem = bulkEditBean.getTargetFieldLayout().getFieldLayoutItem(orderableField);
        final List<Issue> issues = bulkEditBean.getSelectedIssues();
        final Map<Long, DistinctValueResult> distinctValues = new TreeMap<Long, DistinctValueResult>();

        // get mappings used from the session to provide defaults when names cannot be matched
        final Map<Long, Long> fieldSubtitutionMap = getBulkEditBeanFromSession().getFieldSubstitutionMap().get(orderableField.getId());

        for (Issue issue : issues)
        {
            final Issue targetIssue = bulkEditBean.getTargetIssueObjects().get(issue);
            if (!targetIssue.getProjectObject().getId().equals(issue.getProjectObject().getId()))
            {
                final Collection<Object> valuesForIssue = issueValueResolution.get(issue);
                if (valuesForIssue.isEmpty())
                {
                    // only want to allow the user to choose a mapping for "No Value" if the target field layout
                    // requires us to set a value for this field
                    if (targetFieldLayoutItem.isRequired())
                    {
                        distinctValues.put(-1L, new DistinctValueResult());
                    }
                }
                else
                {
                    for (Object value : valuesForIssue)
                    {
                        Long longValue = Long.parseLong(value.toString());
                        if (!distinctValues.containsKey(longValue))
                        {
                            final String valueName = nameResolution.get(value);
                            if (valueName != null)
                            {
                                final Long previouslySelectedValue;
                                final boolean isPreviousValueSpecified;

                                if (fieldSubtitutionMap != null)
                                {
                                    previouslySelectedValue = fieldSubtitutionMap.get(longValue);
                                    isPreviousValueSpecified = fieldSubtitutionMap.containsKey(longValue);
                                }
                                else
                                {
                                    previouslySelectedValue = null;
                                    isPreviousValueSpecified = false;
                                }

                                distinctValues.put(longValue,
                                        new DistinctValueResult(
                                                valueName.trim(),
                                                issue.getProjectObject().getName(),
                                                previouslySelectedValue,
                                                isPreviousValueSpecified));
                            }
                        }
                    }
                }
            }
        }

        return distinctValues;
    }

    public boolean needsSelection(DistinctValueResult distinctValue, Long id, String valueName)
    {
        if (distinctValue.isPreviousValueSpecified())
        {
            final Long previouslySelectedValue = distinctValue.getPreviouslySelectedValue();
            return previouslySelectedValue != null && previouslySelectedValue.equals(id);
        }
        else
        {
            return valueName.trim().equals(distinctValue.getValueName().trim());
        }
    }
}
