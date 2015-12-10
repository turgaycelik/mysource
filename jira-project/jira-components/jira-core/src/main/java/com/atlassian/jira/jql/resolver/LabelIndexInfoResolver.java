package com.atlassian.jira.jql.resolver;

import com.atlassian.jira.issue.fields.LabelsSystemField;
import com.atlassian.jira.issue.label.Label;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An index resolver for the Labels system field.
 *
 * @since v4.2
 */
public class LabelIndexInfoResolver implements IndexInfoResolver<Label>
{
    private final boolean lowerCasing;

    public LabelIndexInfoResolver(boolean isLowerCasing)
    {
        lowerCasing = isLowerCasing;
    }

    public List<String> getIndexedValues(final String rawValue)
    {
        final String[] labels = StringUtils.split(rawValue, LabelsSystemField.SEPARATOR_CHAR);
        final List<String> cleanLabels = new ArrayList<String>();
        if(labels != null)
        {
            for (String label : labels)
            {
                if(lowerCasing)
                {
                    cleanLabels.add(label.toLowerCase());
                }
                else
                {
                    cleanLabels.add(label);
                }
            }
            return cleanLabels;
        }
        return Collections.emptyList();
    }

    public List<String> getIndexedValues(final Long rawValue)
    {
        return Lists.newArrayList(rawValue.toString());
    }

    public String getIndexedValue(final Label label)
    {
        if(lowerCasing)
        {
            return label.getLabel().toLowerCase();
        }
        else
        {
            return label.getLabel();
        }
    }
}
