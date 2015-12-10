package com.atlassian.jira.issue.customfields.searchers.transformer;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.customfields.converters.UserConverter;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.customfields.view.CustomFieldParamsImpl;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer;
import com.atlassian.jira.issue.search.searchers.util.UserFitsNavigatorHelper;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.query.Query;
import com.atlassian.query.operand.SingleValueOperand;

import java.util.Collections;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * The {@link com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer} for user custom fields.
 *
 * @since v4.0
 */
public class UserPickerCustomFieldSearchInputTransformer extends AbstractSingleValueCustomFieldSearchInputTransformer implements SearchInputTransformer
{
    private final UserConverter userConverter;
    private final UserFitsNavigatorHelper userFitsNavigatorHelper;

    public UserPickerCustomFieldSearchInputTransformer(String urlParameterName, final ClauseNames clauseNames, CustomField field,
            UserConverter userConverter,
            final UserFitsNavigatorHelper userFitsNavigatorHelper, final CustomFieldInputHelper customFieldInputHelper)
    {
        super(field, clauseNames, urlParameterName, customFieldInputHelper);
        this.userFitsNavigatorHelper = notNull("userFitsNavigatorHelper", userFitsNavigatorHelper);
        this.userConverter = notNull("userConverter", userConverter);
    }

    @Override
    public void validateParams(final User user, final SearchContext searchContext, final FieldValuesHolder fieldValuesHolder, final I18nHelper i18nHelper, final ErrorCollection errors)
    {
        final CustomFieldParams custParams = (CustomFieldParams) fieldValuesHolder.get(getCustomField().getId());
        if (custParams == null)
        {
            return;
        }
        final String username = (String) custParams.getFirstValueForNullKey();
        try
        {
            userConverter.getUser(username);
        }
        catch (final FieldValidationException e)
        {
            errors.addError(getCustomField().getId(), i18nHelper.getText("admin.errors.could.not.find.username", username));
        }
    }

    public boolean doRelevantClausesFitFilterForm(final User user, final Query query, final SearchContext searchContext)
    {
        final NavigatorConversionResult result = convertForNavigator(query);
        if (!result.fitsNavigator())
        {
            return false;
        }
        else if (result.getValue() == null)
        {
            return true;
        }
        else
        {
            final SingleValueOperand value = result.getValue();
            String username = value.getStringValue() == null ? value.getLongValue().toString() : value.getStringValue();
            return userFitsNavigatorHelper.checkUser(username) != null;
        }
    }

    protected CustomFieldParams getParamsFromSearchRequest(final User user, Query query, final SearchContext searchContext)
    {
        final NavigatorConversionResult result = convertForNavigator(query);
        if (result.fitsNavigator() && result.getValue() != null)
        {
            final SingleValueOperand value = result.getValue();
            final String userValue = value.getStringValue() == null ? value.getLongValue().toString() : value.getStringValue();
            final String userName = userFitsNavigatorHelper.checkUser(userValue);
            if (userName != null)
            {
                return new CustomFieldParamsImpl(getCustomField(), Collections.singleton(userName));
            }
        }
        return null;
    }
}
