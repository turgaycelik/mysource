package com.atlassian.jira.issue.customfields.statistics;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.comparator.UserCachingComparator;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.SearchRequestAppender;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.user.util.UserManager;
import org.apache.commons.lang.StringUtils;

import java.util.Comparator;
import java.util.Locale;

public class UserPickerStatisticsMapper extends AbstractCustomFieldStatisticsMapper
        implements SearchRequestAppender.Factory
{
    private final UserManager userManager;
    private final JiraAuthenticationContext authenticationContext;

    public UserPickerStatisticsMapper(CustomField customField, UserManager userManager, JiraAuthenticationContext authenticationContext)
    {
        super(customField);
        this.userManager = userManager;
        this.authenticationContext = authenticationContext;
    }

    public Object getValueFromLuceneField(String documentValue)
    {
        if (StringUtils.isBlank(documentValue))
        {
            return null;
        }
        final ApplicationUser user = userManager.getUserByKey(documentValue);
        return (user != null) ? user.getDirectoryUser() : null;
    }

    public Comparator getComparator()
    {
        return new UserCachingComparator(getLocale());
    }

    protected String getSearchValue(Object value)
    {
        return ApplicationUsers.getKeyFor((User)value);
    }

    Locale getLocale()
    {
        return authenticationContext.getLocale();
    }

    /**
     * @since v6.0
     */
    @Override
    public SearchRequestAppender getSearchRequestAppender()
    {
        return new CustomFieldSearchRequestAppender(customField, this);
    }
}