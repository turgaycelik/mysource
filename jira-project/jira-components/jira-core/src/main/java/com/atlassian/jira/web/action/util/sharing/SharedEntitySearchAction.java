package com.atlassian.jira.web.action.util.sharing;

import com.atlassian.jira.sharing.SharedEntityColumn;
import com.atlassian.jira.sharing.index.QueryBuilder;
import com.atlassian.jira.sharing.search.SearchParseException;
import com.atlassian.jira.sharing.search.SharedEntitySearchParameters;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.dbc.Assertions;

/**
 * The parameter methods that should be implemented by a Actions that will search for SharedEntity objects.
 *
 * @since v3.13
 */
public interface SharedEntitySearchAction
{
    String getSearchName();

    void setSearchName(String searchName);

    String getSearchOwnerUserName();

    void setSearchOwnerUserName(String searchOwnerUserName);

    String getSearchShareType();

    void setSearchShareType(String searchShareType);

    void setGroupShare(String groupShare);

    String getGroupShare();

    Long getPagingOffset();

    void setProjectShare(String projectShare);

    String getProjectShare();

    void setRoleShare(String roleShare);

    String getRoleShare();

    void setPagingOffset(Long pagingOffset);

    String getSortColumn();

    void setSortColumn(String sortColumn);

    boolean isSortAscending();

    void setSortAscending(boolean sortAscending);

    /**
     * Responsible for validating that a Query is parseable.
     */
    public static class QueryValidator
    {
        public static void validate(SharedEntitySearchParameters searchParameters, ErrorCollection errorCollection, I18nHelper i18nHelper)
        {
            try
            {
                QueryBuilder.validate(searchParameters);
            }
            catch ( SearchParseException e)
            {
                errorCollection.addError(FieldNameMapper.getFieldName(e.getColumn()), i18nHelper.getText("common.sharing.exception.search.parse"));
            }
        }
    }

    static class FieldNameMapper
    {
        static String getFieldName(final SharedEntityColumn column)
        {
            Assertions.notNull("column", column);
            if (SharedEntityColumn.NAME.equals(column))
            {
                return "searchName";
            }
            if (SharedEntityColumn.DESCRIPTION.equals(column))
            {
                return "searchName";
            }
            if (SharedEntityColumn.OWNER.equals(column))
            {
                return "searchOwnerUserName";
            }
            throw new IllegalArgumentException("Unmapped column: " + column);
        }
    }
}
