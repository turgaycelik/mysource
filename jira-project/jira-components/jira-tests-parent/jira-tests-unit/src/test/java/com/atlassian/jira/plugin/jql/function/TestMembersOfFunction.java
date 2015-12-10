package com.atlassian.jira.plugin.jql.function;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.issue.comparator.UserCachingComparator;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.jql.query.QueryCreationContextImpl;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.plugin.jql.operand.MockJqlFunctionModuleDescriptor;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockGroup;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;

import org.easymock.classextension.EasyMock;
import org.junit.Before;
import org.junit.Test;

import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestMembersOfFunction extends MockControllerTestCase
{
    private static final String FUNC_NAME = "funcName";
    private MembersOfFunction membersOfFunction;
    private TerminalClause terminalClause = null;
    private QueryCreationContext queryCreationContext;
    private UserUtil userUtil;
    private CrowdService crowdService;
    private Group groupForTest;
    private User userForTest;
    private User testUser;

    @Before
    public void setUp() throws Exception
    {

        testUser = new MockUser("theUser");
        groupForTest = new MockGroup("group1");
        userForTest = new MockUser("fred");

        userUtil = EasyMock.createMock(UserUtil.class);
        crowdService = EasyMock.createMock(CrowdService.class);

        membersOfFunction = new MembersOfFunction(userUtil, crowdService);
        membersOfFunction.init(MockJqlFunctionModuleDescriptor.create(FUNC_NAME, true));
        queryCreationContext = new QueryCreationContextImpl((ApplicationUser) null);
    }

    @Test
    public void testDataType() throws Exception
    {
        assertEquals(JiraDataTypes.USER, membersOfFunction.getDataType());
    }

    @Test
    public void testValidateArguments() throws Exception
    {
        final FunctionOperand functionOperand = new FunctionOperand(FUNC_NAME, Collections.<String>emptyList());
        final MessageSet messageSet = membersOfFunction.validate(null, functionOperand, terminalClause);
        assertTrue(messageSet.hasAnyErrors());
        assertEquals("Function 'funcName' expected '1' arguments but received '0'.", messageSet.getErrorMessages().iterator().next());
    }

    @Test
    public void testGroupDoesNotExist() throws Exception
    {
        final FunctionOperand functionOperand = new FunctionOperand(FUNC_NAME, Arrays.asList("myGroupWillNeverExistHaHaHa"));
        final MessageSet messageSet = membersOfFunction.validate(null, functionOperand, terminalClause);
        assertTrue(messageSet.hasAnyErrors());
        assertEquals("Function 'funcName' can not generate a list of usernames for group 'myGroupWillNeverExistHaHaHa'; the group does not exist.", messageSet.getErrorMessages().iterator().next());
    }

    @Test
    public void testValidationHappyPath() throws Exception
    {
        // One valid user name supplied
        EasyMock.expect(crowdService.getGroup("group")).andReturn(groupForTest);
        replay(crowdService);

        final FunctionOperand functionOperand = new FunctionOperand(FUNC_NAME, Arrays.asList("group"));
        final MessageSet messageSet = membersOfFunction.validate(testUser, functionOperand, terminalClause);
        assertFalse(messageSet.hasAnyMessages());
    }

    @Test
    public void testGetValuesBadArgument()
    {
        final FunctionOperand functionOperand = new FunctionOperand(FUNC_NAME);
        final List<QueryLiteral> stringList = membersOfFunction.getValues(null, functionOperand, terminalClause);
        assertTrue(stringList.isEmpty());
    }

    @Test
    public void testGetValuesNoSuchGroup() throws Exception
    {
        final FunctionOperand functionOperand = new FunctionOperand(FUNC_NAME, Arrays.asList("myGroupWillNeverExistHaHaHa"));
        final List<QueryLiteral> stringList = membersOfFunction.getValues(null, functionOperand, terminalClause);
        assertTrue(stringList.isEmpty());
    }

    @Test
    public void testGetValuesGroupDoesNotContainUser() throws Exception
    {
//        Group testGroup = createMockGroup("groupFunctionTestGroupDoesNotExist");
//        try
//        {
            final FunctionOperand functionOperand = new FunctionOperand(FUNC_NAME, Arrays.asList("groupFunctionTestGroupDoesNotExist"));
            final List<QueryLiteral> stringList = membersOfFunction.getValues(null, functionOperand, terminalClause);
            assertTrue(stringList.isEmpty());
//        }
//        finally
//        {
//            testGroup.remove();
//        }
    }

    @Test
    public void testGetValuesHappyPath() throws Exception
    {
        EasyMock.expect(crowdService.getGroup("group")).andReturn(groupForTest);

        List<Group> groups = Collections.singletonList(groupForTest);
        final SortedSet<User> setOfUsers = new TreeSet<User>(new UserCachingComparator());
        setOfUsers.add(userForTest);
        EasyMock.expect(userUtil.getAllUsersInGroups(groups)).andReturn(setOfUsers);
        replay(userUtil, crowdService);

        final FunctionOperand functionOperand = new FunctionOperand(FUNC_NAME, Arrays.asList("group"));
        final List<QueryLiteral> stringList = membersOfFunction.getValues(queryCreationContext, functionOperand, terminalClause);
        assertEquals(1, stringList.size());
        assertEquals("fred", stringList.get(0).getStringValue());
        assertEquals(functionOperand, stringList.get(0).getSourceOperand());
    }

    @Test
    public void testGetValuesMultipleGroups() throws Exception
    {
//        Group testGroup1 = createMockGroup("groupForTest");
//        Group testGroup2 = createMockGroup("GROUPforTEST");
//        User testUser1 = createMockUser("groupUser1");
//        User testUser2 = createMockUser("groupUser2");
//        testGroup1.addUser(testUser1);
//        testGroup1.addUser(testUser2);
//        testGroup2.addUser(testUser2);
//        try
//        {
        Group group2ForTest = new MockGroup("group2");
        User user2ForTest = new MockUser("mary");

        EasyMock.expect(crowdService.getGroup("group")).andReturn(groupForTest);
        EasyMock.expect(crowdService.getGroup("group2")).andReturn(group2ForTest);

        List<Group> groups = Collections.singletonList(groupForTest);
        SortedSet<User> setOfUsers = new TreeSet<User>(new UserCachingComparator());
        setOfUsers.add(userForTest);
        setOfUsers.add(user2ForTest);
        EasyMock.expect(userUtil.getAllUsersInGroups(groups)).andReturn(setOfUsers);

        groups = Collections.singletonList(group2ForTest);
        setOfUsers = new TreeSet<User>(new UserCachingComparator());
        setOfUsers.add(user2ForTest);
        EasyMock.expect(userUtil.getAllUsersInGroups(groups)).andReturn(setOfUsers);

        replay(userUtil, crowdService);

        final FunctionOperand functionOperand = new FunctionOperand(FUNC_NAME, Arrays.asList("group", "group2"));
        final List<QueryLiteral> stringList = membersOfFunction.getValues(queryCreationContext, functionOperand, terminalClause);
        assertEquals(2, stringList.size());
        assertTrue(stringList.contains(createLiteral("fred")));
        assertTrue(stringList.contains(createLiteral("mary")));
        assertEquals(functionOperand, stringList.get(0).getSourceOperand());
        assertEquals(functionOperand, stringList.get(1).getSourceOperand());
    }

    @Test
    public void testGetMinimumNumberOfExpectedArguments() throws Exception
    {
        assertEquals(1, membersOfFunction.getMinimumNumberOfExpectedArguments());
    }

}
