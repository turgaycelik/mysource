package com.atlassian.jira.webtests.ztests.comment;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

@WebTest ({ Category.FUNC_TEST, Category.COMMENTS })
public class TestAddCommentFooterLink extends TestAddComment
{
    public TestAddCommentFooterLink()
    {
        super("footer-comment-button");
    }
}
