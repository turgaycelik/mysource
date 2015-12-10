package com.atlassian.jira.issue.fields.layout.field;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.issue.issuetype.IssueType;
import org.ofbiz.core.entity.GenericValue;

/**
 * Responsible for storing the mapping from an {@link IssueType} -> {@link FieldLayout}
 * for a particular {@link FieldLayoutScheme}.
 * <p/>
 * In the UI, the {@link FieldLayoutSchemeEntity}s are the entries in the Configure Field Configuration Scheme
 * screen.
 */
@PublicApi
public interface FieldLayoutSchemeEntity extends Comparable<FieldLayoutSchemeEntity>
{
    Long getId();

    String getIssueTypeId();

    GenericValue getIssueType();

    IssueType getIssueTypeObject();

    void setIssueTypeId(String issueTypeId);

    Long getFieldLayoutId();

    void setFieldLayoutId(Long fieldLayoutId);

    FieldLayoutScheme getFieldLayoutScheme();

    void setFieldLayoutScheme(FieldLayoutScheme fieldLayoutScheme);

    GenericValue getGenericValue();

    void setGenericValue(GenericValue genericValue);

    void store();

    void remove();
}
