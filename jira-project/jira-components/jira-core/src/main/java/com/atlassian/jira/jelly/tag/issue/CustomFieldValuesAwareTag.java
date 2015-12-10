package com.atlassian.jira.jelly.tag.issue;

import com.atlassian.jira.issue.fields.CustomField;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
public interface CustomFieldValuesAwareTag
{
    void addCustomFieldValue(CustomField customField, String customFieldValue, String key);
}
