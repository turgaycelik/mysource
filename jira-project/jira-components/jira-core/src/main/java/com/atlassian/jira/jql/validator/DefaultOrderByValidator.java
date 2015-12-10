package com.atlassian.jira.jql.validator;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.entity.property.EntityPropertyType;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.NavigableField;
import com.atlassian.jira.issue.search.managers.SearchHandlerManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.InjectableComponent;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.query.order.OrderBy;
import com.atlassian.query.order.SearchSort;
import org.apache.lucene.search.FieldComparatorSource;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @since v4.0
 */
@InjectableComponent
public class DefaultOrderByValidator implements OrderByValidator
{
    private final SearchHandlerManager searchHandlerManager;
    private final FieldManager fieldManager;
    private final I18nHelper.BeanFactory iBeanFactory;

    public DefaultOrderByValidator(SearchHandlerManager searchHandlerManager, FieldManager fieldManager, I18nHelper.BeanFactory iBeanFactory)
    {
        this.searchHandlerManager = searchHandlerManager;
        this.fieldManager = fieldManager;
        this.iBeanFactory = iBeanFactory;
    }

    public MessageSet validate(final User searcher, final OrderBy orderBy)
    {
        final MessageSet messageSet = new MessageSetImpl();
        final List<SearchSort> searchSorts = orderBy.getSearchSorts();

        final Map<String, String> fieldToJqlNames = new HashMap<String, String>();

        for (SearchSort searchSort : searchSorts)
        {
            final String clauseName = searchSort.getField();
            if (EntityPropertyType.isJqlClause(clauseName)){
                continue;
            }
            final Collection<String> fieldIds = searchHandlerManager.getFieldIds(searcher, clauseName);

            if (fieldIds.isEmpty())
            {
                // There is no visible associated field and therefore no Sorter
                messageSet.addErrorMessage(getI18n(searcher).getText("jira.jql.order.by.field.is.not.orderable", clauseName));
            }
            else
            {
                // Check the field ids and make sure they resolve to a navigable field that has a non-null sorter
                if (!fieldsAreSortable(fieldIds))
                {
                    messageSet.addErrorMessage(getI18n(searcher).getText("jira.jql.order.by.field.not.supported", clauseName));
                }
                else
                {
                    // Now that we know the field is cool and has a sorter. Now validate that we can see the clause handler
                    // and that there are not more than one sort of the same kind in the sorts
                    validateSortUnique(searcher, messageSet, fieldToJqlNames, fieldIds, clauseName);
                }
            }
        }
        return messageSet;
    }

    private void validateSortUnique(final User searcher, final MessageSet messageSet, final Map<String, String> fieldIdToClauseName,
            final Collection<String> fieldIds, final String clause)
    {
        for (String fieldId : fieldIds)
        {
            final String origClauseName = fieldIdToClauseName.get(fieldId);
            if (origClauseName != null)
            {
                if (origClauseName.equals(clause))
                {
                    messageSet.addErrorMessage(getI18n(searcher).getText("jira.jql.order.by.field.is.duplicate", clause));
                }
                else
                {
                    messageSet.addErrorMessage(getI18n(searcher).getText("jira.jql.order.by.field.is.twice", clause, origClauseName));
                }
            }
            else
            {
                fieldIdToClauseName.put(fieldId, clause);
            }
        }
    }

    private boolean fieldsAreSortable(final Collection<String> fieldIds)
    {
        for (String fieldId : fieldIds)
        {
            if (fieldManager.isNavigableField(fieldId))
            {
                final NavigableField field = fieldManager.getNavigableField(fieldId);
                if (!field.getSortFields(false).isEmpty())
                {
                    return true;
                }
            }
        }
        return false;
    }

    private I18nHelper getI18n(User user)
    {
        return iBeanFactory.getInstance(user);
    }
}
