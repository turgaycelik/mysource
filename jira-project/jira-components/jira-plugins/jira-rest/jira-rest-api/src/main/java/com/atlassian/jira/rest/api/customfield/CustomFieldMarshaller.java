package com.atlassian.jira.rest.api.customfield;

import com.atlassian.jira.issue.fields.CustomField;

/**
 * Custom field implementations may override how their field value is marshalled by providing a CustomFieldMarshaller.
 *
 * @see com.atlassian.jira.issue.customfields.CustomFieldType
 * @since v4.2
 */
public interface CustomFieldMarshaller<T, U>
{
    /**
     * Returns a marshalled custom field instance. The returned instance will be marshalled by JAXB.
     *
     * @param customField the custom field to marshall
     * @param t the transport object
     * @return a marshalled custom field instance
     * @see com.atlassian.jira.issue.customfields.CustomFieldType#getValueFromIssue(com.atlassian.jira.issue.fields.CustomField,
     *      com.atlassian.jira.issue.Issue)
     */
    U marshall(CustomField customField, T t);
}
