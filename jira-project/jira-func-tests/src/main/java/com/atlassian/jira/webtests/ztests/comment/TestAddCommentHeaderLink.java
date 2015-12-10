package com.atlassian.jira.webtests.ztests.comment;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

@WebTest ({ Category.FUNC_TEST, Category.COMMENTS })
public class TestAddCommentHeaderLink extends TestAddComment
{
    public TestAddCommentHeaderLink()
    {
        super("comment-issue");
    }
}
