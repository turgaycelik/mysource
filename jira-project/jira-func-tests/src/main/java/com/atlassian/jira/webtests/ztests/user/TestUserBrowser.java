package com.atlassian.jira.webtests.ztests.user;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @since v4.0
 */
@WebTest ({ Category.FUNC_TEST, Category.BROWSING })
public class TestUserBrowser extends FuncTestCase
{
    private static final List<String> EVERYONE = Arrays.asList(ADMIN_USERNAME, "anteater", "antoinette", "anton", "antone", "barney", "detkin");
    private static final String USER_NAME_FILTER = "userNameFilter";
    private static final String FULL_NAME_FILTER = "fullNameFilter";
    private static final String EMAIL_FILTER = "emailFilter";
    private static final String MAX = "max";

    @Override
    protected void setUpTest()
    {
    }

    public void testFindByFiltering()
    {
        administration.restoreData("TestUserBrowser.xml");

        gotoUserBrowser();
        resetAll();

        assertUserNameColumn(EVERYONE);

        filterBy(USER_NAME_FILTER, "ant");
        assertUserNameColumn(Arrays.asList("anteater", "antoinette", "anton", "antone"));

        filterBy(FULL_NAME_FILTER, "anto");
        assertUserNameColumn(Arrays.asList("antoinette", "anton", "antone"));

        filterBy(EMAIL_FILTER, "ant");
        assertUserNameColumn(Arrays.asList("antoinette", "anton", "antone"));

        resetFilterBy(FULL_NAME_FILTER);
        assertUserNameColumn(Arrays.asList("anteater", "antoinette", "anton", "antone"));

        resetFilterBy(EMAIL_FILTER);
        assertUserNameColumn(Arrays.asList("anteater", "antoinette", "anton", "antone"));

        resetFilterBy(USER_NAME_FILTER);
        assertUserNameColumn(EVERYONE);

        // now just by full name
        filterBy(FULL_NAME_FILTER, "a");
        assertUserNameColumn(EVERYONE);

        filterBy(FULL_NAME_FILTER, "an");    // dylan has 'an' is his full name but admin doesnt
        assertUserNameColumn(Arrays.asList("anteater", "antoinette", "anton", "antone", "detkin"));

        resetFilterBy(FULL_NAME_FILTER);
        filterBy(USER_NAME_FILTER, "d");
        assertUserNameColumn(Arrays.asList(ADMIN_USERNAME, "detkin"));

        filterBy(USER_NAME_FILTER, "d");
        filterBy(FULL_NAME_FILTER, "an");    // dylan has 'an' is his full name but admin doesnt
        filterBy(EMAIL_FILTER, "atlassian");
        assertUserNameColumn(Arrays.asList("detkin"));
    }

    public void testPaging()
    {
        administration.restoreData("TestUserBrowserManyUsers.xml");

        gotoUserBrowser();
        resetAll();

        filterBy(MAX,"10");
        assertUserNameColumn(asList(EVERYONE, userList(0,2)));

        filterBy(MAX,"20");
        assertUserNameColumn(asList(EVERYONE, userList(0,12)));

        filterBy(MAX,"50");
        assertUserNameColumn(asList(EVERYONE, userList(0,42)));

        filterBy(MAX,"100");
        assertUserNameColumn(asList(EVERYONE, userList(0,92)));

        filterBy(MAX,"All");
        assertUserNameColumn(asList(EVERYONE, userList(0,120)));


        resetAll();
        filterBy(USER_NAME_FILTER,"username");

        assertUserNameColumn(userList(0,19));
        gotoNext();
        assertUserNameColumn(userList(20,39));
        gotoPrev();
        assertUserNameColumn(userList(0,19));
        gotoNext();
        gotoNext();
        assertUserNameColumn(userList(40,59));
        gotoPrev();
        assertUserNameColumn(userList(20,39));

        resetAll();
        filterBy(USER_NAME_FILTER,"username");
        filterBy(EMAIL_FILTER,"odd");
        assertUserNameColumn(userListBy2(1,39));
        gotoNext();
        assertUserNameColumn(userListBy2(41,79));
        gotoNext();
        assertUserNameColumn(userListBy2(81,119));
        gotoPrev();
        assertUserNameColumn(userListBy2(41,79));
        gotoPrev();
        assertUserNameColumn(userListBy2(1,39));

    }

    private void gotoPrev()
    {
       tester.clickLinkWithText("<< Previous");
    }

    private void gotoNext() {
        tester.clickLinkWithText("Next >>");
    }

    private List<String> userList(final int startI, final int endI)
    {
        return userListBy(startI,endI,1);
    }

    private List<String> userListBy2(final int startI, final int endI)
    {
        return userListBy(startI,endI,2);
    }
    
    private List<String> userListBy(final int startI, final int endI, final int by)
    {
        List<String> users = new ArrayList<String>();
        for (int i = startI; i <= endI; i += by)
        {
            String padNum = StringUtils.leftPad(String.valueOf(i), 3, '0');
            users.add("username" + padNum);
        }
        return users;
    }

    private List<String> asList(final List<String>... lists)
    {
        final ArrayList<String> list = new ArrayList<String>();
        for (List<String> args : lists)
        {
            list.addAll(args);
        }
        return list;
    }


    private void assertUserNameColumn(final List<String> values)
    {
        assertColumnImpl(1,1,values);
    }

    private void assertColumnImpl(int col, final int startRow, final List<String> values)
    {
        int row = startRow;
        for (String value : values)
        {
            XPathLocator locator = new XPathLocator(tester, "//table[@id='user_browser_table']//tr[" + row + "]/td[" + col + "]");
            assertEquals("Asserting user browser row[" + row + "] col[" + col + "]", value, locator.getText());
            row++;
        }
    }

    private void filterBy(final String fieldName, final String value)
    {
        if (MAX.equals(fieldName))
        {
            tester.selectOption(fieldName, value);
        }
        else
        {
            tester.setFormElement(fieldName, value);
        }
        tester.submit("");

    }

    private void resetAll()
    {
        resetFilterBy(USER_NAME_FILTER);
        resetFilterBy(FULL_NAME_FILTER);
        resetFilterBy(EMAIL_FILTER);
        filterBy(MAX,"20");
    }
    private void resetFilterBy(final String fieldName)
    {
        tester.setFormElement(fieldName, "");
        tester.submit("");
    }

    private void gotoUserBrowser()
    {
        tester.gotoPage("secure/admin/user/UserBrowser.jspa");
    }
}
