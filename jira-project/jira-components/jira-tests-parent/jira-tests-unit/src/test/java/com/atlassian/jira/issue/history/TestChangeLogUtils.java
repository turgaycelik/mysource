package com.atlassian.jira.issue.history;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.entity.model.MockModelEntity;
import org.ofbiz.core.entity.model.ModelEntity;

import com.atlassian.cache.memory.MemoryCacheManager;
import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.DefaultConstantsManager;
import com.atlassian.jira.config.MockIssueConstantFactory;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoContainer;
import com.atlassian.jira.mock.MockConstantsManager;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.mock.ofbiz.ModelReaderMock;
import com.atlassian.jira.mock.security.MockSimpleAuthenticationContext;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockCrowdService;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.util.MockUserManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.collect.MapBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

public class TestChangeLogUtils
{
    @Rule
    public MockitoContainer mockitoContainer = new MockitoContainer(this);

    @Mock
    @AvailableInContainer
    private SubTaskManager subTaskManager;

    @AvailableInContainer
    private final OfBizDelegator ofbiz = new MockOfBizDelegator();

    @AvailableInContainer
    private final CrowdService crowdService = new MockCrowdService();

    @AvailableInContainer
    private final UserManager userManager = new MockUserManager();

    User user;

    ConstantsManager constantsManager;

    @Before
    public void setUp() throws Exception
    {
        ((MockOfBizDelegator) ofbiz).setModelReader(ModelReaderMock.getMock(new ModelReaderMock.Delegate()
        {
            
            @Override
            public ModelEntity getModelEntity(final String entityName)
            {
                if ("Issue".equals(entityName)) {
                    return new MockModelEntity(entityName, Arrays.asList("status", "summary"));
                } else {
                    return null;
                }
            }
            
        }));
        user = createMockUser("bob", "Bob the Builder");
        constantsManager = new DefaultConstantsManager(new MockSimpleAuthenticationContext(user), ofbiz, new MockIssueConstantFactory(), new MemoryCacheManager());
        mockitoContainer.getMockWorker().addMock(ConstantsManager.class, constantsManager);
    }
    @Test
    public void testGenerateChangeItemSame() throws GenericEntityException
    {
        final GenericValue before = new MockGenericValue("Issue", MapBuilder.build("id", "5"));
        final GenericValue after = new MockGenericValue("Issue", MapBuilder.build("id", "5"));

        assertNull(ChangeLogUtils.generateChangeItem(before, after, "id"));
    }

    @Test
    public void testGenerateChangeItemNulls() throws GenericEntityException
    {
        final GenericValue before = new MockGenericValue("Issue", MapBuilder.build("id", null));
        final GenericValue after = new MockGenericValue("Issue", MapBuilder.build("id", null));

        assertNull(ChangeLogUtils.generateChangeItem(before, after, "id"));
    }

    @Test
    public void testGenerateChangeItemBeforeNull() throws GenericEntityException
    {
        final GenericValue before = new MockGenericValue("Issue", MapBuilder.build("id", null));
        final GenericValue after = new MockGenericValue("Issue", MapBuilder.build("id", "5"));

        final ChangeItemBean expected = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "id", null, null, null, "5");

        assertEquals(expected, ChangeLogUtils.generateChangeItem(before, after, "id"));
    }

    @Test
    public void testGenerateChangeItemAfterNull() throws GenericEntityException
    {
        final GenericValue before = new MockGenericValue("Issue", MapBuilder.build("id", "5"));
        final GenericValue after = new MockGenericValue("Issue", MapBuilder.build("id", null));

        final ChangeItemBean expected = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "id", null, "5", null, null);

        assertEquals(expected, ChangeLogUtils.generateChangeItem(before, after, "id"));
    }

    @Test
    public void testGenerateChangeItemDifferent() throws GenericEntityException
    {
        final GenericValue before = new MockGenericValue("Issue", MapBuilder.build("id", "5"));
        final GenericValue after = new MockGenericValue("Issue", MapBuilder.build("id", "7"));

        final ChangeItemBean expected = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "id", null, "5", null, "7");

        assertEquals(expected, ChangeLogUtils.generateChangeItem(before, after, "id"));
    }

    @Test
    public void testGenerateChangeItemType() throws GenericEntityException
    {
        final GenericValue before = UtilsForTests.getTestEntity("Issue", MapBuilder.build("type", "10"));
        final GenericValue after = UtilsForTests.getTestEntity("Issue", Maps.newHashMap());

        UtilsForTests.getTestEntity("IssueType", MapBuilder.build("id", "10", "name", "Foo"));
        ComponentAccessor.getConstantsManager().refreshIssueTypes();

        final ChangeItemBean expected = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "type", "10", "Foo", null, null);

        assertEquals(expected, ChangeLogUtils.generateChangeItem(before, after, "type"));
    }

    @Test
    public void testGenerateChangeItemResolution() throws GenericEntityException
    {
        final GenericValue before = UtilsForTests.getTestEntity("Issue", MapBuilder.build("resolution", "5"));
        final GenericValue after = UtilsForTests.getTestEntity("Issue", Maps.newHashMap());

        UtilsForTests.getTestEntity("Resolution", MapBuilder.build("id", "5", "name", "Solved"));
        ComponentAccessor.getConstantsManager().refreshResolutions();

        final ChangeItemBean expected = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "resolution", "5", "Solved", null, null);

        assertEquals(expected, ChangeLogUtils.generateChangeItem(before, after, "resolution"));
    }

    @Test
    public void testGenerateChangeItemPriority() throws GenericEntityException
    {
        final GenericValue before = UtilsForTests.getTestEntity("Issue", MapBuilder.build("priority", "5"));
        final GenericValue after = UtilsForTests.getTestEntity("Issue", Maps.newHashMap());

        UtilsForTests.getTestEntity("Priority", MapBuilder.build("id", "5", "name", "Top Shelf"));
        ComponentAccessor.getConstantsManager().refreshPriorities();

        final ChangeItemBean expected = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "priority", "5", "Top Shelf", null, null);

        assertEquals(expected, ChangeLogUtils.generateChangeItem(before, after, "priority"));
    }

    @Test
    public void testGenerateChangeItemAssignee() throws Exception
    {
        final GenericValue before = UtilsForTests.getTestEntity("Issue", MapBuilder.build("assignee", "bob"));
        final GenericValue after = UtilsForTests.getTestEntity("Issue", Maps.newHashMap());

        final ChangeItemBean expected = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "assignee", "bob", "Bob the Builder", null, null);

        assertEquals(expected, ChangeLogUtils.generateChangeItem(before, after, "assignee"));
    }

    @Test
    public void testGenerateChangeItemReporter() throws Exception
    {
        final GenericValue before = UtilsForTests.getTestEntity("Issue", MapBuilder.build("reporter", "bob"));
        final GenericValue after = UtilsForTests.getTestEntity("Issue", Maps.newHashMap());

        final ChangeItemBean expected = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "reporter", "bob", "Bob the Builder", null, null);

        assertEquals(expected, ChangeLogUtils.generateChangeItem(before, after, "reporter"));
    }

    @Test
    public void testGenerateChangeItemEstimate() throws Exception
    {
        final GenericValue before = UtilsForTests.getTestEntity("Issue", MapBuilder.build("timeestimate", new Long(7200)));
        final GenericValue after = UtilsForTests.getTestEntity("Issue", Maps.newHashMap());

        final ChangeItemBean expected = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "timeestimate", "7200", "7200", null, null);

        assertEquals(expected, ChangeLogUtils.generateChangeItem(before, after, "timeestimate"));
    }

    @Test
    public void testGenerateChangeItemSpent() throws Exception
    {
        final GenericValue before = UtilsForTests.getTestEntity("Issue", Maps.newHashMap());
        final GenericValue after = UtilsForTests.getTestEntity("Issue", MapBuilder.build("timespent", new Long(3600)));

        final ChangeItemBean expected = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "timespent", null, null, "3600", "3600");

        assertEquals(expected, ChangeLogUtils.generateChangeItem(before, after, "timespent"));
    }

    @Test
    public void testGenerateChangeItemStatus() throws Exception
    {
        final GenericValue before = UtilsForTests.getTestEntity("Issue", MapBuilder.build("status", "1"));
        final GenericValue after = UtilsForTests.getTestEntity("Issue", Maps.newHashMap());

        final MockConstantsManager mcm = new MockConstantsManager();
        mockitoContainer.getMockComponentContainer().addMock(ConstantsManager.class, mcm);

        final GenericValue status = UtilsForTests.getTestEntity("Status", MapBuilder.build("id", "1", "name", "high"));
        mcm.addStatus(status);

        final ChangeItemBean expected = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "status", "1", "high", null, null);

        assertEquals(expected, ChangeLogUtils.generateChangeItem(before, after, "status"));
    }

    @Test
    public void testCreateChangeGroupIdentical() throws Exception
    {
        final GenericValue before = UtilsForTests.getTestEntity("Issue", MapBuilder.build("status", "1", "summary", "2"));
        assertNull(ChangeLogUtils.createChangeGroup((ApplicationUser)null, before, (GenericValue) before.clone(), null, true));
    }

    @Test
    public void testCreateChangeGroupNoValues() throws Exception
    {
        final GenericValue before = UtilsForTests.getTestEntity("Issue", MapBuilder.build("id", new Long(1), "status", null));
        final GenericValue after = before;
        assertNull(ChangeLogUtils.createChangeGroup((ApplicationUser)null, before, after, null, true));
    }

    @Test
    public void testCreateChangeGroup() throws Exception
    {
        final GenericValue before = UtilsForTests.getTestEntity("Issue", MapBuilder.build("status", "1", "summary", "2"));
        final GenericValue after = UtilsForTests.getTestEntity("Issue", MapBuilder.build("status", "2", "summary", "4"));

        final GenericValue changeGroup = ChangeLogUtils.createChangeGroup(user, before, after, null, true);
        assertNotNull(changeGroup.getLong("id"));
        assertNotNull(changeGroup.getTimestamp("created"));
        assertEquals("bob", changeGroup.getString("author"));

        final List<GenericValue> changeItems = ofbiz.findByAnd("ChangeItem", MapBuilder.build("group", changeGroup.getLong("id")));
        assertThat(changeItems, hasSize(2));
    }

    @Test
    public void testCreateChangeGroupNoChangeJustList() throws Exception
    {
        final GenericValue before = UtilsForTests.getTestEntity("Issue", MapBuilder.build("id", new Long(1), "status", null));

        final ChangeItemBean cib = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "foo", "bar", "baz");
        final GenericValue changeGroup = ChangeLogUtils.createChangeGroup((ApplicationUser)null, before, (GenericValue) before.clone(), ImmutableList.of(cib), true);
        final List<GenericValue> changeItems = ofbiz.findByAnd("ChangeItem", MapBuilder.build("group", changeGroup.getLong("id")));
        assertThat(changeItems, hasSize(1));
    }

    private User createMockUser(final String userName, final String fullname)
            throws OperationNotPermittedException, InvalidUserException, InvalidCredentialException
    {
        final User user = new MockUser(userName, fullname, "fullname@somewhere.com");
        final CrowdService crowdService = ComponentAccessor.getCrowdService();
        crowdService.addUser(user, "password");
        ((MockUserManager) userManager).addUser(user);
        return user;
    }
}

