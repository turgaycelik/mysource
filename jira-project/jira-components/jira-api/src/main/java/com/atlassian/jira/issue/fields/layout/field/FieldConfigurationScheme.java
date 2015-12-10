package com.atlassian.jira.issue.fields.layout.field;

import com.atlassian.annotations.PublicApi;

import java.util.Collection;
import java.util.Set;

/**
 * A Field Configuration Scheme maps each Issue Type to a "Field Configuration" ({@link FieldLayoutSchemeEntity}).
 *
 * <p> A Field Configuration defines for each field if it is required or not, whether it is visible or hidden, and what
 * "Screens" it will appear on. (The Screen defines the order the fields are shown in, and can define multiple tabs).
 */
@PublicApi
public interface FieldConfigurationScheme
{
    Long getId();

    String getName();

    String getDescription();

    /**
     * Returns the id of the field layout to use for this given issue type id. This will do all the necessary work to
     * lookup the default entry if no specific mapping for the given isuse type id exists. So after calling this method
     * simply use the returned field layout id.
     *
     * @param issueTypeId the Issue Type ID.
     * @return the id of the {@link FieldLayout} ("Field Configuration") to use for this given issue type id.
     */
    Long getFieldLayoutId(String issueTypeId);

    /**
     * Returns the id's of the field layout's represented by FieldConfigurationScheme (i.e. the layouts associated
     * with this project).
     *
     * @param allIssueTypeIds all the issue type id's that are in the system. This is used to determine if we need
     * to return the layout for the default, if we have all the issue types mapped in the system then we do not care
     * about the default since it is never used.
     * @return the id's of the {@link FieldLayout} ("Field Configuration") to use for the project this is associcated with.
     */
    Set<Long> getAllFieldLayoutIds(Collection<String> allIssueTypeIds);
}
