package com.atlassian.jira.webtests.ztests.user;

import com.atlassian.jira.functest.framework.locator.CssLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;
import org.apache.commons.lang.StringUtils;

@WebTest ({ Category.FUNC_TEST, Category.USERS_AND_GROUPS })
public class TestUserProperties extends JIRAWebTest
{

    protected static final String USER_BOB_BROWSER = "/secure/admin/user/EditUserProperties.jspa?name=bob";
    protected static final String DELETE_PROP = "delete_testprop";

    public TestUserProperties(String name)
    {
        super(name);
    }

    @Override
    public void setUp()
    {
        super.setUp();
        restoreBlankInstance();
        addUser(BOB_USERNAME, BOB_PASSWORD, BOB_FULLNAME, BOB_EMAIL);
        goToBobPropertiesPage();
    }

    public void testAddUserProperty()
    {
        addBobProperty("testprop", "testvalue");
        // Test that the property has been added
        goToBobPropertiesPage();
        assertTextPresentBeforeText("testprop", "testvalue");
    }

    public void testDuplicateProperty()
    {
        addBobProperty("testprop", "testvalue1");
        addBobProperty("testprop", "testvalue2");
        assertions.assertNodeHasText(new CssLocator(tester, ".error[data-field=key]"), "This 'key' already exists for the user.");
    }

    public void testDeleteUserProperty()
    {
        addBobProperty("testprop", "testvalue");
        gotoPage(USER_BOB_BROWSER);
        clickLink(DELETE_PROP);
        assertTextPresent("Delete Property: testprop");
        submit("Delete");
        tester.assertTextPresent("currently has no properties");

    }

    public void testInvalidCharactersForProperty()
    {
        addBobProperty("testprop!", "testvalue");
        assertions.assertNodeHasText(new CssLocator(tester, ".error[data-field=key]"), "The 'key' can only contain alphanumeric characters");
    }

    public void testKeyExceedsCharacterLengthCheck()
    {
        addBobProperty(StringUtils.repeat("x", 251), "testvalue");
        assertions.assertNodeHasText(new CssLocator(tester, ".error[data-field=key]"), "The 'key' length must be less than 200 characters");

    }

    public void testValueExceedCharacterLengthCheck()
    {
        addBobProperty("testproperty", StringUtils.repeat("x", 251));
        assertions.assertNodeHasText(new CssLocator(tester, ".error[data-field=value]"), "The 'value' length must be less than 250 characters");
    }

    public void testKeyIsEmptyCheck()
    {
        addBobProperty("", "testvalue");
        assertions.assertNodeHasText(new CssLocator(tester, ".error[data-field=key]"), "The 'key' cannot be empty");
    }

    public void testValueIsEmptyCheck()
    {
        addBobProperty("testproperty", "");
        assertions.assertNodeHasText(new CssLocator(tester, ".error[data-field=value]"), "The 'value' cannot be empty");
    }

    public void testValueIsHtmlEncoded()
    {
        addBobProperty("testproperty", "<blink>Annoying</blink>");
        assertTextPresent("&lt;blink&gt;Annoying&lt;/blink&gt;");
        assertTextNotPresent("<blink>Annoying</blink>");
    }

    private void addBobProperty(String key, String value)
    {
        goToBobPropertiesPage();
        setFormElement("key", key);
        setFormElement("value", value);
        submit();
    }

    private void goToBobPropertiesPage()
    {
        gotoPage(USER_BOB_BROWSER);
        assertTextPresent(BOB_FULLNAME);
    }
}
