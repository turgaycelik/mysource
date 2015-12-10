
package com.atlassian.jira.webtests.ztests.user.rename;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.dom4j.DocumentHelper.createXPath;

/**
 * These tests are identical to those in {@link TestUserRenameOnIssuePrintableView},
 * but checking the XML view instead.
 *
 * @since v6.0
 */
@WebTest ({ Category.FUNC_TEST, Category.USERS_AND_GROUPS, Category.RENAME_USER, Category.ISSUES })
public class TestUserRenameOnIssueXmlView extends FuncTestCase
{
    private static final String CF_CC = "customfield_10200";
    private static final String CF_TESTER = "customfield_10300";

    private static final XPath XPATH_ISSUES = createXPath("//item");
    private static final XPath XPATH_CC = createXPath("customfields/customfield[@id='" + CF_CC + "']");
    private static final XPath XPATH_TESTER = createXPath("customfields/customfield[@id='" + CF_TESTER + "']");
    private static final XPath XPATH_CF_VALUES = createXPath("customfieldvalues/customfieldvalue");

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("user_rename.xml");
    }

    //    KEY       USERNAME    NAME
    //    bb	    betty	    Betty Boop
    //    ID10001	bb	        Bob Belcher
    //    cc	    cat	        Crazy Cat
    //    ID10101	cc	        Candy Chaos


    public void testXmlViewWithRenamedUsers() throws IOException, DocumentException
    {
        // COW-1
        Element issue = getXmlIssue("COW-1");
        assertXmlViewUserWithFullName(issue, "assignee", "betty", "Betty Boop");
        assertXmlViewUserWithFullName(issue, "reporter", "cat", "Crazy Cat");
        assertXmlViewUsernames(issue, XPATH_CC, "admin", "bb");
        assertXmlViewUsernames(issue, XPATH_TESTER, "cc");

        // COW-3
        issue = getXmlIssue("COW-3");
        assertXmlViewUserWithFullName(issue, "assignee", "bb", "Bob Belcher");
        assertXmlViewUserWithFullName(issue, "reporter", "cc", "Candy Chaos");
        assertXmlViewUsernames(issue, XPATH_CC, "admin", "betty");
        assertXmlViewUsernames(issue, XPATH_TESTER, "cat");

        // Now test Search
        List<Element> issues = runXmlSearch();

        // COW-3
        issue = issues.get(1);
        assertXmlViewUserWithFullName(issue, "assignee", "bb", "Bob Belcher");
        assertXmlViewUserWithFullName(issue, "reporter", "cc", "Candy Chaos");
        assertXmlViewUsernames(issue, XPATH_CC, "admin", "betty");
        assertXmlViewUsernames(issue, XPATH_TESTER, "cat");

        // COW-1
        issue = issues.get(3);
        assertXmlViewUserWithFullName(issue, "assignee", "betty", "Betty Boop");
        assertXmlViewUserWithFullName(issue, "reporter", "cat", "Crazy Cat");
        assertXmlViewUsernames(issue, XPATH_CC, "admin", "bb");
        assertXmlViewUsernames(issue, XPATH_TESTER, "cc");
    }

    public void testXmlViewBeforeAndAfterAnotherRename() throws IOException, DocumentException
    {
        // COW-1
        assertXmlViewUsernames(getXmlIssue("COW-1"), XPATH_TESTER, "cc");

        // COW-3
        navigation.issue().viewXml("COW-3");
        assertXmlViewUserWithFullName(getXmlIssue("COW-3"), "reporter", "cc", "Candy Chaos");

        // Rename cc to candy
        navigation.gotoPage("secure/admin/user/EditUser!default.jspa?editName=cc");
        tester.setFormElement("username", "candy");
        tester.submit("Update");
        // Now we are on View User
        assertEquals("/secure/admin/user/ViewUser.jspa?name=candy", navigation.getCurrentPage());
        assertEquals("candy", locator.id("username").getText());

        // COW-1 - Issue should still have Candy in the CF, but with the new username
        assertXmlViewUsernames(getXmlIssue("COW-1"), XPATH_TESTER, "candy");

        // COW-3
        assertXmlViewUserWithFullName(getXmlIssue("COW-3"), "reporter", "candy", "Candy Chaos");
    }



    private Element getXmlIssue(String issueKey) throws IOException, DocumentException
    {
        navigation.issue().viewXml(issueKey);
        return issue(document());
    }

    private List<Element> runXmlSearch() throws IOException, DocumentException
    {
        navigation.issueNavigator().runXmlSearch("", "assignee", "reporter", CF_CC, CF_TESTER);
        return issues(document());
    }

    private Document document() throws IOException, DocumentException
    {
        return new SAXReader().read(tester.getDialog().getResponse().getInputStream());
    }

    @SuppressWarnings("unchecked")
    private List<Element> issues(Document doc)
    {
        return (List<Element>)XPATH_ISSUES.selectNodes(doc);
    }

    private Element issue(Document doc)
    {
        return (Element)XPATH_ISSUES.selectSingleNode(doc);
    }


    private List<Element> elementsByName(Element parent, String name)
    {
        final List<Element> kids = new ArrayList<Element>();
        final List<?> nodes = parent.content();
        for (Object obj : nodes)
        {
            if (obj instanceof Element)
            {
                final Element child = (Element)obj;
                if (name.equals( child.getName() ))
                {
                    kids.add(child);
                }
            }
        }
        return kids;
    }

    private Element elementByName(Element parent, String name)
    {
        final List<Element> list = elementsByName(parent, name);
        return list.isEmpty() ? null : list.get(0);
    }



    private List<String> customFieldValues(Element customField)
    {
        final List<?> nodes = XPATH_CF_VALUES.selectNodes(customField);
        final List<String> values = new ArrayList<String>(nodes.size());
        for (Object obj : nodes)
        {
            final Element valueElement = (Element)obj;
            values.add(valueElement.getText());
        }
        return values;
    }



    private void assertXmlViewUserWithFullName(Element issue, String fieldName, String username, String fullName)
    {
        final Element userField = elementByName(issue, fieldName);
        assertEquals(username, userField.attribute("username").getValue());
        assertEquals(fullName, userField.getTextTrim());
    }

    private void assertXmlViewUsernames(Object context, XPath xpath, String... usernames)
    {
        final Element customField = (Element)xpath.selectSingleNode(context);
        assertEquals(asList(usernames), customFieldValues(customField));
    }
}

