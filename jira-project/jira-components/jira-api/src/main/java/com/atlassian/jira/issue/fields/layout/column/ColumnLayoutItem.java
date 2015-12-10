package com.atlassian.jira.issue.fields.layout.column;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.NavigableField;
import com.atlassian.jira.user.ApplicationUser;
import com.google.common.base.Function;

import java.util.Map;

@PublicApi
public interface ColumnLayoutItem extends Comparable
{
    /**
     * Transform to its id.
     */
    public static final Function<ColumnLayoutItem, String> TO_ID = new Function<ColumnLayoutItem, String>()
    {
        @Override
        public String apply(ColumnLayoutItem item)
        {
            return item.getId();
        }
    };

    NavigableField getNavigableField();

    /**
     * Return the string form of the unique identifier for this column. When the column corresponds to a {@link
     * NavigableField}, the id of the column will be the same as the id of the field.
     *
     * @return the id;
     */
    public String getId();

    boolean isAliasForField(User user, String sortField);

    boolean isAliasForField(ApplicationUser user, String sortField);

    int getPosition();

    String getHtml(Map displayParams, Issue issue);

    /**
     * Return some text for the Column Header.  By default this calls {@link NavigableField#getColumnHeadingKey} but
     * implementations can override this to provide different column headings as appropriate
     *
     * @return A key, which can be run through {@link com.atlassian.jira.util.I18nHelper#getText(String)} to get a
     *         heading
     */
    String getColumnHeadingKey();

}
