package com.atlassian.jira.webtests.ztests.misc;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * This test case should be used to test for all sorts of XSS bugs.
 *
 */
@WebTest ({ Category.FUNC_TEST, Category.SECURITY })
public class TestXSS extends FuncTestCase
{

    private static final String XSS_ALERT_CONTENTS = "xss exploit";
    private static final String XSS_ALERT_RAW = "\"alert('" + XSS_ALERT_CONTENTS + "')";

    // the string will be have single & double quotes URL encoded and the rest of dangerous characters HTML escaped
    private static final String XSS_ALERT_ESCAPED = "&quot;alert(&#39;xss exploit&#39;)";


    private static final String XSS_RETURN_ATTACK_ENCODED =  "javascript%3Aalert(%22owned%22)%3B";
    private static final String XSS_RETURN_ATTACK_RAW =  "javascript:alert(\"owned\");";

    @Override
    protected void setUpTest()
    {
        administration.restoreBlankInstance();
    }

    //JRA-15707  \
    //JRADEV-3546 now return null if detect javascript

    public void testXSSReturnUrl()
    {
        final String issueKey = navigation.issue().createIssue("homosapien", null, "Test issue");
        navigation.issue().viewIssue(issueKey);
        tester.gotoPage("/secure/AddComment!default.jspa?id=10000&returnUrl="+ XSS_RETURN_ATTACK_ENCODED);
        final String responseHtml = tester.getDialog().getResponseText();
        assertTrue(responseHtml.indexOf(XSS_RETURN_ATTACK_ENCODED) == -1);
        assertTrue(responseHtml.indexOf(XSS_RETURN_ATTACK_RAW) == -1);
    }

    // JRA-21152
    public void testXSSReturnUrlInViewUserProfiles()
    {
        tester.gotoPage("/secure/admin/user/ViewUserProjectRoles!default.jspa?atl_token=AKJmt_DiLV&name=admin&returnUrl="+ XSS_RETURN_ATTACK_ENCODED);
        final String responseHtml = tester.getDialog().getResponseText();
        assertReturnUrlEscaped(responseHtml);
    }

    // JRA-21173
    public void testXSSFieldTypeInCreateCustomField()
    {
        navigation.gotoAdminSection("view_custom_fields");
        tester.clickLink("add_custom_fields");
        String atl_token = page.getXsrfToken();

        // make sure fieldType does not make it onto the page unescaped
        tester.gotoPage("http://localhost:8090/jira/secure/admin/CreateCustomField.jspa?currentStep=1&global=true&&issuetypes=-1&fieldType=<script>INJECT</script>&searcher=&description=&fieldName=&nextBtn=Next+%3e%3e&atl_token=" + atl_token);
        assertions.getTextAssertions().assertTextNotPresent("<script>INJECT</script>");
    }

    //JRA-20965
    public void testXSSPickers()
    {
        //colorpicker popup
        tester.gotoPage("/secure/popups/colorpicker.jsp?element=name;}catch%28e%29{}%0D%0A--%3E%3C/script%3E%3Cscript%3Ealert%28%27woot%27%29%3C/script%3E%3Cscript%3E%3C!--&defaultColor=%27;try{//");
        tester.assertTextPresent("&gt;&lt;/script&gt;&lt;script&gt;alert(&#39;woot&#39;)&lt;/script&gt;&lt;script&gt;&lt;!");
        tester.assertTextNotPresent("<script>alert('woot')</script>");
        tester.gotoPage("/secure/popups/colorpicker.jsp?defaultColor=<script>alert('boo');</script>");
        tester.assertTextPresent("&lt;script&gt;alert(&#39;boo&#39;);&lt;/script&gt;");
        tester.assertTextNotPresent("<script>alert('boo');</script>");

        //userpicker
        tester.gotoPage("/secure/popups/UserPickerBrowser.jspa?formName=startform&multiSelect=true&element=<script>alert('doo');</script>");
        tester.assertTextPresent("&lt;script&gt;alert(&#39;doo&#39;);&lt;/script&gt;");
        tester.assertTextNotPresent("<script>alert('doo');</script>");

        //grouppicker
        tester.gotoPage("/secure/popups/GroupPickerBrowser.jspa?formName=startform&multiSelect=true&element=<script>alert('foo');</script>");
        tester.assertTextPresent("&lt;script&gt;alert(&#39;foo&#39;);&lt;/script&gt;");
        tester.assertTextNotPresent("<script>alert('foo');</script>");

        //Banner preview
        tester.gotoPage("/secure/Dashboard.jspa?announcement_preview_banner_st=<script>alert('foobar');</script>");
        tester.assertTextNotPresent("<script>alert('foobar');</script>");
    }

    // JRA-21360
    public void testXSSIconPicker()
    {
        // fieldType
        tester.gotoPage("/secure/popups/IconPicker.jspa?fieldType=<script>alert('XSS');</script>&formName=jiraform");
        tester.assertTextPresent("&lt;script&gt;alert(&#39;XSS&#39;);&lt;/script&gt;");
        tester.assertTextNotPresent("<script>alert('XSS');</script>");
    }

    //JRA-24290
    public void testXssInHeaderUsername()
    {
        administration.usersAndGroups().addUser(XSS_ALERT_RAW, "password", XSS_ALERT_RAW, "xss@xss.com");
        navigation.login(XSS_ALERT_RAW, "password");
        assertions.getTextAssertions().assertTextPresent(XSS_ALERT_ESCAPED);
        assertions.getTextAssertions().assertTextNotPresent(XSS_ALERT_RAW);
    }



    private void assertReturnUrlEscaped(final String responseHtml)
    {
        assertFalse(responseHtml.contains(XSS_RETURN_ATTACK_ENCODED));
        assertFalse(responseHtml.contains(XSS_RETURN_ATTACK_RAW));
    }
}
