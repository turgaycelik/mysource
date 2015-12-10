package com.atlassian.jira.issue.statistics.util;

import com.atlassian.jira.issue.search.constants.SystemSearchConstants;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Has some utility methods that the hit collectors can use.
 *
 * @since v4.0
 */
public class HitCollectorUtil
{
    private static final Pattern CUSTOM_FIELD_REGEX = Pattern.compile("^customfield_\\d+", Pattern.CASE_INSENSITIVE);

    public HitCollectorUtil()
    {
    }

    /**
     * Translates between a known set of document id's to field id's that can be used to check visiblity.
     *
     * This method will turn any custom field id's such as customfield_10000_RAW into customfield_10000.
     *
     * @param documentConstant the value to transform into a field id
     * @return the field id for the passed document constant
     */
    public String getFieldId(final String documentConstant)
    {
        if (SystemSearchConstants.forComponent().getIndexField().equals(documentConstant))
        {
            return SystemSearchConstants.forComponent().getFieldId();
        }
        else if (SystemSearchConstants.forAssignee().getIndexField().equals(documentConstant))
        {
            return SystemSearchConstants.forAssignee().getFieldId();
        }
        else if (SystemSearchConstants.forIssueType().getIndexField().equals(documentConstant))
        {
            return SystemSearchConstants.forIssueType().getFieldId();
        }
        else if (SystemSearchConstants.forFixForVersion().getIndexField().equals(documentConstant))
        {
            return SystemSearchConstants.forFixForVersion().getFieldId();
        }
        else if (SystemSearchConstants.forPriority().getIndexField().equals(documentConstant))
        {
            return SystemSearchConstants.forPriority().getFieldId();
        }
        else if (SystemSearchConstants.forProject().getIndexField().equals(documentConstant))
        {
            return SystemSearchConstants.forProject().getFieldId();
        }
        else if (SystemSearchConstants.forAffectedVersion().getIndexField().equals(documentConstant))
        {
            return SystemSearchConstants.forAffectedVersion().getFieldId();
        }
        else if (SystemSearchConstants.forReporter().getIndexField().equals(documentConstant))
        {
            return SystemSearchConstants.forReporter().getFieldId();
        }
        else if (SystemSearchConstants.forResolution().getIndexField().equals(documentConstant))
        {
            return SystemSearchConstants.forResolution().getFieldId();
        }
        else if (SystemSearchConstants.forStatus().getIndexField().equals(documentConstant))
        {
            return SystemSearchConstants.forStatus().getFieldId();
        }

        // Must be a custom field, lets trim any crap off the back
        final Matcher matcher = CUSTOM_FIELD_REGEX.matcher(documentConstant);
        if (matcher.find())
        {
            return matcher.group();
        }
        else
        {
            return documentConstant;
        }
    }
}
