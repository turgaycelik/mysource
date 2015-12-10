package com.atlassian.jira.jelly.tag.admin;

import java.util.Collections;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.issue.security.IssueSecurityLevelImpl;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import com.atlassian.jira.jelly.AbstractJellyTestCase;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.scheme.SchemeEntity;

import org.easymock.MockControl;

/**
 * @since v4.0
 */
public class TestAddIssueSecurityLevel extends AbstractJellyTestCase
{
    private User user;
    private Group group;

    private MockControl mockIssueSecuritySchemeManagerControl;
    private MockControl mockIssueSecurityLevelManagerControl;

    protected void setUp() throws Exception
    {
        super.setUp();
        //Create user and place in the action context
        user = createMockUser("AddIssueSecurityLevel-in-user");
        group = createMockGroup("admin-group");
        addUserToGroup(user, group);

        Scheme scheme = new Scheme(new Long(12345), "IssueSecurityScheme", "blah", "", Collections.<SchemeEntity>emptyList());
        JiraTestUtil.loginUser(user);
        mockIssueSecuritySchemeManagerControl = MockControl.createStrictControl(IssueSecuritySchemeManager.class);
        final IssueSecuritySchemeManager mockIssueSecuritySchemeManager = (IssueSecuritySchemeManager) mockIssueSecuritySchemeManagerControl.getMock();
        mockIssueSecuritySchemeManager.getSchemeObject("DUDE");
        mockIssueSecuritySchemeManagerControl.setReturnValue(null);
        mockIssueSecuritySchemeManager.getSchemeObjects();
        mockIssueSecuritySchemeManagerControl.setReturnValue(Collections.EMPTY_LIST);
        mockIssueSecuritySchemeManager.createSchemeObject("DUDE", null);
        mockIssueSecuritySchemeManagerControl.setReturnValue(scheme);
        mockIssueSecuritySchemeManagerControl.replay();

        MockGenericValue mockIssueSecLevel = new MockGenericValue("IssueSecurityLevel", EasyMap.build("name", "seclevel", "id", new Long(54321)));
        mockIssueSecurityLevelManagerControl = MockControl.createStrictControl(IssueSecurityLevelManager.class);
        final IssueSecurityLevelManager mockIssueSecurityLevelManager = (IssueSecurityLevelManager) mockIssueSecurityLevelManagerControl.getMock();
        mockIssueSecurityLevelManager.createIssueSecurityLevel(12345, "seclevel", "Defines visibility");
        mockIssueSecurityLevelManagerControl.setReturnValue(new IssueSecurityLevelImpl(54321L, "seclevel", "Defines visibility", 12345L));
        mockIssueSecurityLevelManager.getSchemeIssueSecurityLevels(12345L);
        mockIssueSecurityLevelManagerControl.setReturnValue(EasyList.build(mockIssueSecLevel));
        mockIssueSecurityLevelManagerControl.replay();

        ManagerFactory.addService(IssueSecuritySchemeManager.class, mockIssueSecuritySchemeManager);
        ManagerFactory.addService(IssueSecurityLevelManager.class, mockIssueSecurityLevelManager);
    }

    protected void tearDown() throws Exception
    {
        mockIssueSecuritySchemeManagerControl.verify();
        mockIssueSecurityLevelManagerControl.verify();

        ManagerFactory.addService(IssueSecuritySchemeManager.class, null);
        ManagerFactory.addService(IssueSecurityLevelManager.class, null);
        CoreFactory.getGenericDelegator().removeByAnd("SchemeIssueSecurityLevels", Collections.EMPTY_MAP);
        super.tearDown();
    }

    public TestAddIssueSecurityLevel(String s)
    {
        super(s);
    }

    protected String getRelativePath()
    {
        return "tag" + FS + "admin" + FS;
    }

    public void testSecurityLevelAddedWithCorrectDescription() throws Exception
    {
        runScriptAndAssertTextResultEquals(null, "add-issue-security-level.test.description.add.jelly");

        // Assertion is the fact that mockIssueSecurityLevelManager.createIssueSecurityLevel(12345, "seclevel", "Defines visibility"); is called
    }
}
