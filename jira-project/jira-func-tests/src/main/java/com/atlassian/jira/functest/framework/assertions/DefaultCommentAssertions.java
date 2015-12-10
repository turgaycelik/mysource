package com.atlassian.jira.functest.framework.assertions;

import com.atlassian.jira.functest.framework.FunctTestConstants;
import com.atlassian.jira.functest.framework.LocatorFactory;
import com.atlassian.jira.functest.framework.Navigation;

/**
 * Holds assertion methods around an issue's comments.
 *
 * @since v4.2
 */
public class DefaultCommentAssertions implements CommentAssertions
{
    private Iterable<String> comments;

    private Navigation navigation;

    private TextAssertions text;

    private LocatorFactory locator;

    DefaultCommentAssertions(final Iterable<String> comments, final Navigation navigation, final TextAssertions text, final LocatorFactory locator)
    {
        this.comments = comments;
        this.navigation = navigation;
        this.text = text;
        this.locator = locator;
    }

    public void areVisibleTo(String userName, String issueKey)
    {
        navigation.logout();
        navigation.login(userName);
        navigation.issue().gotoIssue(issueKey);

        for (String comment : comments)
        {
            text.assertTextPresent(locator.xpath("//*[@id='issue_actions_container']//div[contains(concat(' ', normalize-space(@class), ' '), ' action-body ')]"), comment);
        }

        // restore the admin login just to be sure
        navigation.login(FunctTestConstants.ADMIN_USERNAME, FunctTestConstants.ADMIN_PASSWORD);
    }

    public void areNotVisibleTo(String userName, String issueKey)
    {
        navigation.logout();
        navigation.login(userName);
        navigation.issue().gotoIssue(issueKey);

        for (String comment : comments)
        {
            text.assertTextNotPresent(locator.xpath("//*[@id='issue_actions_container']//div[contains(concat(' ', normalize-space(@class), ' '), ' action-body ')]"), comment);
        }

        // restore the admin login just to be sure
        navigation.login(FunctTestConstants.ADMIN_USERNAME, FunctTestConstants.ADMIN_PASSWORD);
    }
}
