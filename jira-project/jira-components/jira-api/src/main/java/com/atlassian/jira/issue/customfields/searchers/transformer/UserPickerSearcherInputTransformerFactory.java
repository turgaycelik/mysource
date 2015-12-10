package com.atlassian.jira.issue.customfields.searchers.transformer;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.constants.UserFieldSearchConstants;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer;
import com.atlassian.jira.issue.search.searchers.util.UserFitsNavigatorHelper;

/**
 * @since v5.2
 *
 */
@Internal
public interface UserPickerSearcherInputTransformerFactory
{
    SearchInputTransformer create(UserFieldSearchConstants searchConstants, CustomField field,
            UserFitsNavigatorHelper userFitsNavigatorHelper);
}
