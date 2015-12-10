package com.atlassian.jira.jql.validator;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.util.MessageSet;
import javax.annotation.Nonnull;
import com.atlassian.query.order.OrderBy;

/**
 * Responsible for validating the {@link com.atlassian.jira.web.bean.StatisticAccessorBean.OrderBy} portion
 * of a {@link com.atlassian.query.Query}.
 *
 * This will check to see that all the {@link com.atlassian.query.order.SearchSort}s contain orderable
 * jql clause names and that there are no duplicates in the total clause.
 *
 * @since v4.0
 */
public interface OrderByValidator
{
    /**
     * Will add messages if there is a portion of the order by that contains a non-orderable jql clause name or
     * if there are any duplicates or if the user is trying to order by a field that they can't see.
     *
     * @param searcher the user performing the validation.
     * @param orderBy the OrderBy containing the SearchSorts.
     * @return a MessageSet that will contain i18n'ed messages if something does not pass validation and will be
     * empty if all is well, not null.
     */
    @Nonnull MessageSet validate(User searcher, OrderBy orderBy);
}
