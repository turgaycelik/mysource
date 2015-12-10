package com.atlassian.jira.webtests.ztests.issue.security.xss;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * Test case for XSS exploits in user links rendered as part of comments using the Wiki Renderer.
 *
 * @since 5.2
 */
@WebTest({ Category.FUNC_TEST, Category.ISSUES, Category.SECURITY })
public class TestXssInCommentUserLink extends FuncTestCase
{

    private static final String XSS = "<script>alert('XSS');</script>";

    // the rel attribute of user link should contain an escaped XSS attack
    private static final String XSS_IN_REL = "rel=\"" + XSS + "\"";
    private static final String XSS_ESCAPED_IN_REL = "rel=\"&lt;script&gt;alert(&#39;XSS&#39;);&lt;/script&gt;\"";

    public void testXssInUserLinks()
    {
        backdoor.restoreBlankInstance();
        backdoor.fieldConfiguration().setFieldRenderer(DEFAULT_FIELD_CONFIGURATION, FIELD_COMMENT, WIKI_STYLE_RENDERER);
        backdoor.usersAndGroups().addUser(XSS);
        final String issueKey = backdoor.issues().createIssue(PROJECT_HOMOSAP_KEY, "Testing XSS").key();
        backdoor.issues().commentIssue(issueKey, "[~" + XSS + "]");
        navigation.issue().gotoIssue(issueKey);
        assertions.getTextAssertions().assertTextNotPresent(XSS_IN_REL);
        assertions.getTextAssertions().assertTextPresent(XSS_ESCAPED_IN_REL);

    }


}
