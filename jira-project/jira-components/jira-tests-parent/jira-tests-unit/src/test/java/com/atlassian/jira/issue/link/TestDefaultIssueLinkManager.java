package com.atlassian.jira.issue.link;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hamcrest.collection.IsEmptyCollection;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import com.atlassian.cache.CacheManager;
import com.atlassian.cache.memory.MemoryCacheManager;
import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.crowd.exception.embedded.InvalidGroupException;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.index.DefaultSearchExtractorRegistrationManager;
import com.atlassian.jira.index.SearchExtractorRegistrationManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.issue.util.IssueUpdateBean;
import com.atlassian.jira.issue.util.IssueUpdater;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoContainer;
import com.atlassian.jira.mock.MockApplicationProperties;
import com.atlassian.jira.mock.MockIssueManager;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockCrowdService;
import com.atlassian.jira.user.MockGroup;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.CollectionReorderer;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

public class TestDefaultIssueLinkManager
{
    @Rule
    public MockitoContainer mockitoContainer = new MockitoContainer(this);

    @Mock
    @AvailableInContainer
    private FieldVisibilityManager visibilityManager;

    @Mock
    @AvailableInContainer
    private IssueManager mockIssueManager;

    @Mock
    @AvailableInContainer
    private IssueLinkTypeManager mockIssueLinkTypeManager;

    @Mock
    @AvailableInContainer
    private IssueUpdater mockIssueUpdater;

    @Mock
    @AvailableInContainer
    private PermissionManager permissionManager;

    @Mock
    @AvailableInContainer
    private IssueIndexManager indexManager;

    @Mock
    private IssueManager issueManagerMock;

    @AvailableInContainer
    private CacheManager cacheManager = new MemoryCacheManager();

    @AvailableInContainer
    private final OfBizDelegator ofbiz = new MockOfBizDelegator();

    @AvailableInContainer
    private final ApplicationProperties ap = new MockApplicationProperties();

    @AvailableInContainer
    private final MockIssueManager issueManager = new MockIssueManager();

    @AvailableInContainer
    private final SearchExtractorRegistrationManager searchExtractorManager = new DefaultSearchExtractorRegistrationManager();

    @AvailableInContainer
    CrowdService crowdService = new MockCrowdService();

    private DefaultIssueLinkManager dilm;
    private final CollectionReorderer collectionReorderer = new CollectionReorderer();
    private GenericValue sourceIssue;
    private MockIssue sourceIssueObject;
    private GenericValue destinationIssue;
    private MockIssue destinationIssueObject;

    @Before
    public void setUp() throws Exception
    {
        ComponentAccessor.getApplicationProperties().setString(APKeys.JIRA_I18N_LANGUAGE_INPUT, APKeys.Languages.ENGLISH);

        when(visibilityManager.isFieldHidden((String) anyObject(), (Issue) anyObject())).thenReturn(false);
        when(visibilityManager.isFieldVisible((String) anyObject(), (Issue) anyObject())).thenReturn(true);

        sourceIssue = UtilsForTests.getTestEntity("Issue", ImmutableMap.of("summary", "test source summary", "key", "TST-1", "id", new Long(1)));
        sourceIssueObject = new MockIssue();
        sourceIssueObject.setGenericValue(sourceIssue);

        destinationIssue = UtilsForTests.getTestEntity("Issue", ImmutableMap.of("summary", "test destination summary", "key", "TST-2", "id", new Long(2)));
        destinationIssueObject = new MockIssue();
        destinationIssueObject.setGenericValue(destinationIssue);

        issueManager.addIssue(sourceIssue);
        issueManager.addIssue(destinationIssue);

    }

    @Test
    public void testCreateIssueLinkSystemLinkTypeCreatesGenericValue() throws CreateException, GenericEntityException
    {
        final Long testSourceId = new Long(0);
        final Long testDestinationId = new Long(1);
        final Long testSequence = new Long(0);
        final Long testLinkType = new Long(11);

        final Map<String, Long> expectedFields = ImmutableMap.of("linktype", testLinkType, "source", testSourceId, "destination", testDestinationId, "sequence",
                testSequence);
        final GenericValue issueLinkGV = new MockGenericValue("IssueLink", new HashMap<String, Long>(expectedFields));
        final MockOfBizDelegator delegator = new MockOfBizDelegator(null, ImmutableList.of(issueLinkGV));

        final GenericValue issueLinkTypeGV = new MockGenericValue("IssueLinkType", ImmutableMap.of("linkname", "test name", "outward", "test outward",
                "inward", "test inward", "style", "jira_test style"));
        final MockIssueManager issueManager = new MockIssueManager();
        issueManager.addIssue(sourceIssue);
        issueManager.addIssue(destinationIssue);

        when(mockIssueLinkTypeManager.getIssueLinkType(anyLong())).thenReturn(new IssueLinkTypeImpl(issueLinkTypeGV));
        when(mockIssueLinkTypeManager.getIssueLinkType(anyLong(), anyBoolean())).thenReturn(new IssueLinkTypeImpl(issueLinkTypeGV));

        final IssueLinkCreator issueLinkCreator = new IssueLinkCreator()
        {
            @Override
            public IssueLink createIssueLink(final GenericValue issueLinkGV)
            {
                return new IssueLinkImpl(issueLinkGV, mockIssueLinkTypeManager, issueManager);
            }
        };

        dilm = new DefaultIssueLinkManager(delegator, issueLinkCreator, mockIssueLinkTypeManager, collectionReorderer, null, indexManager,
                ap, cacheManager)
        {
            @Override
            protected void reindexLinkedIssues(final IssueLink issueLink)
            {
            }
        };
        dilm.createIssueLink(testSourceId, testDestinationId, testLinkType, testSequence, null);
        delegator.verify();
    }

    @Test
    public void testCreateIssueLinkNonSystemLinkTypeCreateGVsAndChangeItems()
            throws CreateException, GenericEntityException, OperationNotPermittedException, InvalidUserException, InvalidCredentialException
    {
        final Long testSourceId = new Long(1);
        final Long testDestinationId = new Long(2);
        final Long testSequence = new Long(0);
        final Long testLinkType = new Long(11);

        final GenericValue issueLinkTypeGV = new MockGenericValue("IssueLinkType", ImmutableMap.of("linkname", "test name", "outward", "test outward",
                "inward", "test inward", "style", ""));

        when(mockIssueLinkTypeManager.getIssueLinkType(anyLong())).thenReturn(new IssueLinkTypeImpl(issueLinkTypeGV));
        when(mockIssueLinkTypeManager.getIssueLinkType(anyLong(), anyBoolean())).thenReturn(new IssueLinkTypeImpl(issueLinkTypeGV));


        final GenericValue sourceIssue = new MockGenericValue("Issue", ImmutableMap.of("summary", "test source summary", "number", 1L, "id",
                testSourceId)) {
            @Override
            public List<GenericValue> getRelatedOrderBy(final String relationName, final List orderBy)
                    throws GenericEntityException
            {
                return Lists.newArrayList();
            }
        };
        final GenericValue destinationIssue = new MockGenericValue("Issue", ImmutableMap.of("summary", "test destination summary", "number", 2L,
                "id", testDestinationId)) {
            @Override
            public List<GenericValue> getRelatedOrderBy(final String relationName, final List orderBy)
                    throws GenericEntityException
            {
                return Lists.newArrayList();
            }
        };

        final MockIssueManager mockIssueManager = new MockIssueManager();
        mockIssueManager.addIssue(sourceIssue);
        mockIssueManager.addIssue(destinationIssue);

        final IssueLinkCreator issueLinkCreator = new IssueLinkCreator()
        {
            @Override
            public IssueLink createIssueLink(final GenericValue issueLinkGV)
            {
                return new IssueLinkImpl(issueLinkGV, mockIssueLinkTypeManager, mockIssueManager);
            }
        };

        final User testUser = createMockUser("test user");

        final IssueUpdateBean issueUpdateBean1 = new IssueUpdateBean(sourceIssue, sourceIssue, EventType.ISSUE_UPDATED_ID, testUser);
        issueUpdateBean1.setDispatchEvent(false);
        final ChangeItemBean expectedCib = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "Link", null, null, destinationIssue.getString("key"),
                "This issue " + "test outward" + " " + destinationIssue.getString("key"));
        issueUpdateBean1.setChangeItems(ImmutableList.of(expectedCib));

        final IssueUpdateBean issueUpdateBean2 = new IssueUpdateBean(destinationIssue, destinationIssue, EventType.ISSUE_UPDATED_ID, testUser);
        final ChangeItemBean expectedCib2 = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "Link", null, null, sourceIssue.getString("key"),
                "This issue " + "test inward" + " " + sourceIssue.getString("key"));
        issueUpdateBean2.setDispatchEvent(false);
        issueUpdateBean2.setChangeItems(ImmutableList.of(expectedCib2));

        final List<IssueUpdateBean> issueUpdateBeans = new ArrayList<IssueUpdateBean>();
        final IssueUpdater updater = new IssueUpdater()
        {
            @Override
            public void doUpdate(final IssueUpdateBean issueUpdateBean, final boolean generateChangeItems)
            {
                issueUpdateBeans.add(issueUpdateBean);
            }
        };

        dilm = new DefaultIssueLinkManager(ofbiz, issueLinkCreator, mockIssueLinkTypeManager, collectionReorderer, updater, indexManager,
                ap, cacheManager);
        dilm.createIssueLink(testSourceId, testDestinationId, testLinkType, testSequence, testUser);
        ((MockOfBizDelegator) ofbiz).verify();

        assertThat(issueUpdateBeans, hasSize(2));
        assertThat(issueUpdateBeans, hasItems(issueUpdateBean1));
        assertThat(issueUpdateBeans, hasItems(issueUpdateBean2));
    }

    @Test
    public void testRemoveIssueLinkSystemLinkType() throws RemoveException, GenericEntityException
    {
        final GenericValue issueLinkTypeGV = UtilsForTests.getTestEntity("IssueLinkType", ImmutableMap.of("outward", "test out", "inward",
                "test inward", "linkname", "test name", "style", "jira_some system style"));
        final IssueLinkType issueLinkType = new IssueLinkTypeImpl(issueLinkTypeGV);
        final Long linkTypeId = issueLinkType.getId();

        when(mockIssueLinkTypeManager.getIssueLinkType(eq(linkTypeId))).thenReturn(issueLinkType);

        setupManager(mockIssueLinkTypeManager, null);

        final GenericValue issueLinkGV = UtilsForTests.getTestEntity("IssueLink", ImmutableMap.of("source", new Long(0), "destination", new Long(1),
                "linktype", linkTypeId));
        final IssueLink issueLink = new IssueLinkImpl(issueLinkGV, mockIssueLinkTypeManager, null);
        dilm.removeIssueLink(issueLink, null);

        final List<GenericValue> issueLinkGVs = ofbiz.findAll("IssueLink");
        assertThat(issueLinkGVs, IsEmptyCollection.<GenericValue>empty());

    }

    @Test
    public void testRemoveIssueLinkNonSystemLinkType()
            throws RemoveException, GenericEntityException, OperationNotPermittedException, InvalidUserException, InvalidCredentialException
    {
        final User testUser = createMockUser("test user");
        final GenericValue issueLinkTypeGV = UtilsForTests.getTestEntity("IssueLinkType", ImmutableMap.of("linkname", "test name", "outward",
                "test outward", "inward", "test inward"));
        final IssueLinkType issueLinkType = new IssueLinkTypeImpl(issueLinkTypeGV);

        when(mockIssueLinkTypeManager.getIssueLinkType(eq(issueLinkType.getId()))).thenReturn(issueLinkType);

        final MyIssueUpdater issueUpdater = new MyIssueUpdater()
        {
            int called = 0;
            int expectedCalled = 2;

            @Override
            public void doUpdate(final IssueUpdateBean issueUpdateBean, final boolean generateChangeItems)
            {
                if (called == 0)
                {
                    assertFalse("issueUpdateBean#isDispatchEvent() should be false", issueUpdateBean.isDispatchEvent());
                    assertThat(sourceIssue, is(issueUpdateBean.getOriginalIssue()));
                    assertThat(sourceIssue, is(issueUpdateBean.getChangedIssue()));
                    assertThat(testUser, is(issueUpdateBean.getUser()));
                    assertThat(EventType.ISSUE_UPDATED_ID, is(issueUpdateBean.getEventTypeId()));
                    final ChangeItemBean expectedCib = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "Link", destinationIssue.getString("key"),
                            "This issue " + issueLinkType.getOutward() + " " + destinationIssue.getString("key"), null, null);
                    assertThat(ImmutableList.of(expectedCib), hasItems(issueUpdateBean.getChangeItems().toArray(new ChangeItemBean[]{})));
                    called++;
                }
                else if (called == 1)
                {
                    assertFalse("issueUpdateBean#isDispatchEvent() should be false", issueUpdateBean.isDispatchEvent());
                    assertThat(destinationIssue, is(issueUpdateBean.getOriginalIssue()));
                    assertThat(destinationIssue, is(issueUpdateBean.getChangedIssue()));
                    assertThat(testUser, is(issueUpdateBean.getUser()));
                    assertThat(EventType.ISSUE_UPDATED_ID, is(issueUpdateBean.getEventTypeId()));
                    final ChangeItemBean expectedCib = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "Link", sourceIssue.getString("key"),
                            "This issue " + issueLinkType.getInward() + " " + sourceIssue.getString("key"), null, null);
                    assertThat(ImmutableList.of(expectedCib), hasItems(issueUpdateBean.getChangeItems().toArray(new ChangeItemBean[]{})));
                    called++;
                }
                else
                {
                    fail("doUpdate called " + ++called + " times.");
                }
            }

            @Override
            public void verify()
            {
                if (called != expectedCalled)
                {
                    fail("doUpdate was called '" + called + " times instead of " + expectedCalled + ".");
                }
            }
        };

        final GenericValue issueLinkGV = UtilsForTests.getTestEntity("IssueLink", ImmutableMap.of("source", sourceIssue.getLong("id"), "destination",
                destinationIssue.getLong("id"), "linktype", issueLinkType.getId()));
        final IssueLink issueLink = new IssueLinkImpl(issueLinkGV, mockIssueLinkTypeManager, issueManager);

        setupManager(mockIssueLinkTypeManager, issueUpdater);

        dilm.removeIssueLink(issueLink, testUser);
        issueUpdater.verify();

    }

    @Test
    public void testRemoveIssueLinks() throws RemoveException, GenericEntityException
    {
        // Setup system link type
        final GenericValue issueLinkTypeGV = UtilsForTests.getTestEntity("IssueLinkType", ImmutableMap.of("outward", "test out", "inward",
                "test inward", "linkname", "test name", "style", "jira_some system style"));
        final IssueLinkType issueLinkType = new IssueLinkTypeImpl(issueLinkTypeGV);

        when(mockIssueLinkTypeManager.getIssueLinkType(eq(issueLinkType.getId()))).thenReturn(issueLinkType);

        setupManager(mockIssueLinkTypeManager, null);

        // Create issue links - one with the issue as source the other as destination
        UtilsForTests.getTestEntity("IssueLink", ImmutableMap.of("source", sourceIssue.getLong("id"), "destination", new Long(999), "linktype",
                issueLinkType.getId()));
        UtilsForTests.getTestEntity("IssueLink", ImmutableMap.of("source", new Long(7654), "destination", sourceIssue.getLong("id"), "linktype",
                issueLinkType.getId()));

        dilm.removeIssueLinks(sourceIssue, null);

        final List<GenericValue> issueLinkGVs = ofbiz.findAll("IssueLink");
        assertThat(issueLinkGVs, IsEmptyCollection.<GenericValue>empty());

    }

    @Test
    public void testGetOutwardLinks() throws GenericEntityException
    {
        final List<IssueLinkType> issueLinkTypes = setupIssueLinkTypes(false);

        final List<IssueLink> expectedLinks = setupLinks(issueLinkTypes, false, sourceIssue, destinationIssue);
        mockLinkTypes(issueLinkTypes);

        setupManager(null, null);
        final List<IssueLink> outwardLinks = dilm.getOutwardLinks(sourceIssue.getLong("id"));

        // Compare the contents of the list (order does not matter)
        assertThat(outwardLinks, hasSize(2));
        assertThat(outwardLinks, hasItems(expectedLinks.get(0)));
        assertThat(outwardLinks, hasItems(expectedLinks.get(1)));
    }

    @Test
    public void testGetInwardLinks() throws GenericEntityException
    {
        final List<IssueLinkType> issueLinkTypes = setupIssueLinkTypes(false);
        final List<IssueLink> expectedLinks = setupLinks(issueLinkTypes, false, sourceIssue, destinationIssue);
        mockLinkTypes(issueLinkTypes);

        setupManager(null, null);
        final List<IssueLink> inwardLinks = dilm.getInwardLinks(destinationIssue.getLong("id"));

        // Compare the contents of the list (order does not matter)
        assertThat(inwardLinks, hasSize(2));
        assertThat(inwardLinks, hasItems(expectedLinks.get(0)));
        assertThat(inwardLinks, hasItems(expectedLinks.get(1)));
    }

    @Test
    public void testGetInwardLinksNullId() throws GenericEntityException
    {
        setupManager(null, null);
        final List<IssueLink> inwardLinks = dilm.getInwardLinks(null);
        assertThat(inwardLinks, hasSize(0));
    }

    @Test
    public void testGetOutwardLinksNullId() throws GenericEntityException
    {
        setupManager(null, null);
        final List<IssueLink> outwardLinks = dilm.getOutwardLinks(null);
        assertThat(outwardLinks, hasSize(0));
    }

    @Test
    public void testGetLinkCollection() throws GenericEntityException
    {
        when(permissionManager.hasPermission(eq(Permissions.BROWSE), isA(Issue.class), (User)isNull())).thenReturn(Boolean.TRUE);

        final List<IssueLinkType> expectedIssueLinkTypes = setupIssueLinkTypes(false);
        setupLinks(expectedIssueLinkTypes, true, sourceIssue, destinationIssue);

        mockLinkTypes(expectedIssueLinkTypes);

        setupManager(mockIssueLinkTypeManager, null);

        final LinkCollection linkCollection = dilm.getLinkCollection(sourceIssueObject, null, true);

        final Set<IssueLinkType> resultLinkTypes = linkCollection.getLinkTypes();
        assertThat(resultLinkTypes, hasSize(2));

        // Iterate over the expected link types
        for (final Iterator<IssueLinkType> iterator = expectedIssueLinkTypes.iterator(); iterator.hasNext();)
        {
            final IssueLinkType issueLinkType = iterator.next();
            assertThat(resultLinkTypes, hasItems(issueLinkType));

            // Test Outward issues
            final List<Issue> resultOutwardIssues = linkCollection.getOutwardIssues(issueLinkType.getName());
            assertThat(resultOutwardIssues, hasSize(1));
            final Issue outwardIssue = resultOutwardIssues.get(0);
            assertThat(destinationIssue.getLong("id"), is(outwardIssue.getGenericValue().getLong("id")));

            // Test Inward Links
            final List<Issue> resultInwardIssues = linkCollection.getInwardIssues(issueLinkType.getName());
            assertThat(resultInwardIssues, hasSize(1));
            final Issue inwardIssue = resultInwardIssues.get(0);
            assertThat(destinationIssue.getLong("id"), is(inwardIssue.getGenericValue().getLong("id")));
        }

    }

    @Test
    public void testGetLinkCollectionIssueObject() throws GenericEntityException
    {
        when(permissionManager.hasPermission(eq(Permissions.BROWSE), isA(Issue.class), (User)isNull())).thenReturn(Boolean.TRUE);

        final List<IssueLinkType> expectedIssueLinkTypes = setupIssueLinkTypes(false);
        setupLinks(expectedIssueLinkTypes, true, sourceIssue, destinationIssue);

        mockLinkTypes(expectedIssueLinkTypes);

        setupManager(mockIssueLinkTypeManager, null);

        final LinkCollection linkCollection = dilm.getLinkCollection(sourceIssueObject, null);

        final Set<IssueLinkType> resultLinkTypes = linkCollection.getLinkTypes();
        assertThat(resultLinkTypes, hasSize(2));

        // Iterate over the expected link types
        for (final Iterator<IssueLinkType> iterator = expectedIssueLinkTypes.iterator(); iterator.hasNext();)
        {
            final IssueLinkType issueLinkType = iterator.next();
            assertThat(resultLinkTypes, hasItems(issueLinkType));

            // Test Outward issues
            final List<Issue> resultOutwardIssues = linkCollection.getOutwardIssues(issueLinkType.getName());
            assertThat(resultOutwardIssues, hasSize(1));
            final Issue outwardIssue = resultOutwardIssues.get(0);
            assertThat(destinationIssue.getLong("id"), is(outwardIssue.getGenericValue().getLong("id")));

            // Test Inward Links
            final List<Issue> resultInwardIssues = linkCollection.getInwardIssues(issueLinkType.getName());
            assertThat(resultInwardIssues, hasSize(1));
            final Issue inwardIssue = resultInwardIssues.get(0);
            assertThat(destinationIssue.getLong("id"), is(inwardIssue.getGenericValue().getLong("id")));
        }
    }

    @Test
    public void testGetLinkCollectionIssueObjectOverrideSecurity() throws GenericEntityException
    {
        when(permissionManager.hasPermission(eq(Permissions.BROWSE), isA(Issue.class), (User)isNull())).thenReturn(Boolean.TRUE);

        final List<IssueLinkType> expectedIssueLinkTypes = setupIssueLinkTypes(false);
        setupLinks(expectedIssueLinkTypes, true, sourceIssue, destinationIssue);

        mockLinkTypes(expectedIssueLinkTypes);

        setupManager(mockIssueLinkTypeManager, null);

        final LinkCollection linkCollection = dilm.getLinkCollectionOverrideSecurity(sourceIssueObject);

        final Set<IssueLinkType> resultLinkTypes = linkCollection.getLinkTypes();
        assertThat(resultLinkTypes, hasSize(2));

        // Iterate over the expected link types
        for (final Iterator<IssueLinkType> iterator = expectedIssueLinkTypes.iterator(); iterator.hasNext();)
        {
            final IssueLinkType issueLinkType = iterator.next();
            assertThat(resultLinkTypes, hasItems(issueLinkType));

            // Test Outward issues
            final List<Issue> resultOutwardIssues = linkCollection.getOutwardIssues(issueLinkType.getName());
            assertThat(resultOutwardIssues, hasSize(1));
            final Issue outwardIssue = resultOutwardIssues.get(0);
            assertThat(destinationIssue.getLong("id"), is(outwardIssue.getGenericValue().getLong("id")));

            // Test Inward Links
            final List<Issue> resultInwardIssues = linkCollection.getInwardIssues(issueLinkType.getName());
            assertThat(resultInwardIssues, hasSize(1));
            final Issue inwardIssue = resultInwardIssues.get(0);
            assertThat(destinationIssue.getLong("id"), is(inwardIssue.getGenericValue().getLong("id")));
        }
    }

    @Test
    public void testMoveIssueLink() throws GenericEntityException
    {
        final List<IssueLink> issueLinks = setupIssueLinkSequence();

        final List<IssueLink> expectedIssueLinks = new ArrayList<IssueLink>(issueLinks);

        setupManager(null, null);
        dilm.moveIssueLink(issueLinks, new Long(0), new Long(2));

        // Ensure the sequences are reset
        List<GenericValue> results = ofbiz.findByAnd("IssueLink", ImmutableMap.of("id", expectedIssueLinks.get(0).getId()));
        assertThat(results, hasSize(1));
        GenericValue result = results.get(0);
        assertThat(new Long(2), is(result.getLong("sequence")));

        results = ofbiz.findByAnd("IssueLink", ImmutableMap.of("id", expectedIssueLinks.get(1).getId()));
        assertThat(results, hasSize(1));
        result = results.get(0);
        assertThat(new Long(0), is(result.getLong("sequence")));

        results = ofbiz.findByAnd("IssueLink", ImmutableMap.of("id", expectedIssueLinks.get(2).getId()));
        assertThat(results, hasSize(1));
        result = results.get(0);
        assertThat(new Long(1), is(result.getLong("sequence")));
    }

    @Test
    public void testResetSequences() throws GenericEntityException
    {
        final List<IssueLink> issueLinks = setupIssueLinkSequence();
        final List<IssueLink> expecteIssueLinks = new ArrayList<IssueLink>(issueLinks);
        issueLinks.remove(1);

        setupManager(null, null);
        dilm.resetSequences(issueLinks);

        // Ensure the sequences are reset
        List<GenericValue> results = ofbiz.findByAnd("IssueLink", ImmutableMap.of("id", expecteIssueLinks.get(0).getId()));
        assertThat(results, hasSize(1));
        GenericValue result = results.get(0);
        assertThat(new Long(0), is(result.getLong("sequence")));

        results = ofbiz.findByAnd("IssueLink", ImmutableMap.of("id", expecteIssueLinks.get(2).getId()));
        assertThat(results, hasSize(1));
        result = results.get(0);
        assertThat(new Long(1), is(result.getLong("sequence")));
    }

    @Test
    public void testGetIssueLink()
    {
        final List<IssueLink> expectedIssueLinks = setupIssueLinkSequence();
        setupManager(null, null);

        final IssueLink expectedIssueLink = expectedIssueLinks.get(0);
        final IssueLink result = dilm.getIssueLink(expectedIssueLink.getSourceId(), expectedIssueLink.getDestinationId(),
                expectedIssueLink.getLinkTypeId());

        // As we do not care about which actual link we get as long as the source, destination and link type id are the same just test them
        assertThat(expectedIssueLink.getSourceId(), is(result.getSourceId()));
        assertThat(expectedIssueLink.getDestinationId(), is(result.getDestinationId()));
        assertThat(expectedIssueLink.getLinkTypeId(), is(result.getLinkTypeId()));
    }

    @Test
    public void testChangeIssueLinkTypeNonSystemLink()
            throws RemoveException, GenericEntityException, OperationNotPermittedException, InvalidUserException, InvalidCredentialException
    {
        final User testUser = createMockUser("test use");

        final List<IssueLinkType> issueLinkTypes = setupIssueLinkTypes(false);
        final IssueLinkType issueLinkType = issueLinkTypes.get(0);

        when(mockIssueLinkTypeManager.getIssueLinkType(eq(issueLinkType.getId()))).thenReturn(issueLinkType);

        final GenericValue issueLinkGV = UtilsForTests.getTestEntity("IssueLink", ImmutableMap.of("source", sourceIssue.getLong("id"), "destination",
                destinationIssue.getLong("id"), "linktype", issueLinkType.getId()));

        final IssueLink issueLink = new IssueLinkImpl(issueLinkGV, mockIssueLinkTypeManager, issueManager);
        final IssueLinkType swapIssueLinkType = issueLinkTypes.get(1);

        final MyIssueUpdater issueUpdater = new MyIssueUpdater()
        {
            int called = 0;
            int expectedCalled = 4;

            @Override
            public void doUpdate(final IssueUpdateBean issueUpdateBean, final boolean generateChangeItems)
            {
                if (called == 0)
                {
                    assertFalse("issueUpdateBean#isDispatchEvent() should be false", issueUpdateBean.isDispatchEvent());
                    assertThat(sourceIssue, is(issueUpdateBean.getOriginalIssue()));
                    assertThat(sourceIssue, is(issueUpdateBean.getChangedIssue()));
                    assertThat(testUser, is(issueUpdateBean.getUser()));
                    assertThat(EventType.ISSUE_UPDATED_ID, is(issueUpdateBean.getEventTypeId()));
                    final ChangeItemBean expectedCib = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "Link", destinationIssue.getString("key"),
                            "This issue " + issueLinkType.getOutward() + " " + destinationIssue.getString("key"), null, null);
                    assertThat(ImmutableList.of(expectedCib), hasItems(issueUpdateBean.getChangeItems().toArray(new ChangeItemBean[]{})));
                    called++;
                }
                else if (called == 1)
                {
                    assertFalse("issueUpdateBean#isDispatchEvent() should be false", issueUpdateBean.isDispatchEvent());
                    assertThat(destinationIssue, is(issueUpdateBean.getOriginalIssue()));
                    assertThat(destinationIssue, is(issueUpdateBean.getChangedIssue()));
                    assertThat(testUser, is(issueUpdateBean.getUser()));
                    assertThat(EventType.ISSUE_UPDATED_ID, is(issueUpdateBean.getEventTypeId()));
                    final ChangeItemBean expectedCib = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "Link", sourceIssue.getString("key"),
                            "This issue " + issueLinkType.getInward() + " " + sourceIssue.getString("key"), null, null);
                    assertThat(ImmutableList.of(expectedCib), hasItems(issueUpdateBean.getChangeItems().toArray(new ChangeItemBean[]{})));
                    called++;
                }
                else if (called == 2)
                {
                    assertFalse("issueUpdateBean#isDispatchEvent() should be false", issueUpdateBean.isDispatchEvent());
                    assertThat(sourceIssue, is(issueUpdateBean.getOriginalIssue()));
                    assertThat(sourceIssue, is(issueUpdateBean.getChangedIssue()));
                    assertThat(testUser, is(issueUpdateBean.getUser()));
                    assertThat(EventType.ISSUE_UPDATED_ID, is(issueUpdateBean.getEventTypeId()));
                    final ChangeItemBean expectedCib = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "Link", null, null,
                            destinationIssue.getString("key"), "This issue " + swapIssueLinkType.getOutward() + " " + destinationIssue.getString("key"));
                    assertThat(ImmutableList.of(expectedCib), hasItems(issueUpdateBean.getChangeItems().toArray(new ChangeItemBean[]{})));
                    called++;
                }
                else if (called == 3)
                {
                    assertFalse("issueUpdateBean#isDispatchEvent() should be false", issueUpdateBean.isDispatchEvent());
                    assertThat(destinationIssue, is(issueUpdateBean.getOriginalIssue()));
                    assertThat(destinationIssue, is(issueUpdateBean.getChangedIssue()));
                    assertThat(testUser, is(issueUpdateBean.getUser()));
                    assertThat(EventType.ISSUE_UPDATED_ID, is(issueUpdateBean.getEventTypeId()));
                    final ChangeItemBean expectedCib = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "Link", null, null,
                            sourceIssue.getString("key"), "This issue " + swapIssueLinkType.getInward() + " " + sourceIssue.getString("key"));
                    assertThat(ImmutableList.of(expectedCib), hasItems(issueUpdateBean.getChangeItems().toArray(new ChangeItemBean[]{})));
                    called++;
                }
                else
                {
                    fail("doUpdate called " + ++called + " times.");
                }
            }

            @Override
            public void verify()
            {
                if (called != expectedCalled)
                {
                    fail("doUpdate was called '" + called + " times instead of " + expectedCalled + ".");
                }
            }
        };

        setupManager(null, issueUpdater);
        dilm.changeIssueLinkType(issueLink, swapIssueLinkType, testUser);

        final List<GenericValue> results = ofbiz.findByAnd("IssueLink", ImmutableMap.of("id", issueLink.getId()));
        assertThat(results, hasSize(1));
        final GenericValue result = results.get(0);
        assertThat(swapIssueLinkType.getId(), is(result.getLong("linktype")));

        issueUpdater.verify();
    }

    @Test
    public void testChangeIssueLinkTypeSystemLink()
            throws RemoveException, GenericEntityException, OperationNotPermittedException, InvalidUserException, InvalidCredentialException
    {
        final User testUser = createMockUser("test use");

        final List<IssueLinkType> issueLinkTypes = setupIssueLinkTypes(true);
        final IssueLinkType issueLinkType = issueLinkTypes.get(0);

        when(mockIssueLinkTypeManager.getIssueLinkType(eq(issueLinkType.getId()))).thenReturn(issueLinkType);

        final GenericValue issueLinkGV = UtilsForTests.getTestEntity("IssueLink", ImmutableMap.of("source", sourceIssue.getLong("id"), "destination",
                destinationIssue.getLong("id"), "linktype", issueLinkType.getId()));
        final IssueLink issueLink = new IssueLinkImpl(issueLinkGV, mockIssueLinkTypeManager, issueManager);
        final IssueLinkType swapIssueLinkType = issueLinkTypes.get(1);

        setupManager(null, null);
        dilm.changeIssueLinkType(issueLink, swapIssueLinkType, testUser);

        final List<GenericValue> results = ofbiz.findByAnd("IssueLink", ImmutableMap.of("id", issueLink.getId()));
        assertThat(results, hasSize(1));
        final GenericValue result = results.get(0);
        assertThat(swapIssueLinkType.getId(), is(result.getLong("linktype")));
    }

    private List<IssueLinkType> setupIssueLinkTypes(final boolean system) throws GenericEntityException
    {
        // Setup system link type
        final GenericValue issueLinkTypeGV1 = UtilsForTests.getTestEntity("IssueLinkType", ImmutableMap.of("outward", "test outward", "inward",
                "test inward", "linkname", "test name"));
        final GenericValue issueLinkTypeGV2 = UtilsForTests.getTestEntity("IssueLinkType", ImmutableMap.of("outward", "out test", "inward", "in test",
                "linkname", "another test name"));

        if (system)
        {
            issueLinkTypeGV1.set("style", "jira_some system style");
            issueLinkTypeGV1.store();
            issueLinkTypeGV2.set("style", "jira_some system style");
            issueLinkTypeGV2.store();
        }

        final List<IssueLinkType> issueLinkTypes = new ArrayList<IssueLinkType>();
        issueLinkTypes.add(new IssueLinkTypeImpl(issueLinkTypeGV1));
        issueLinkTypes.add(new IssueLinkTypeImpl(issueLinkTypeGV2));
        return issueLinkTypes;
    }

    private void mockLinkTypes(final List<IssueLinkType> linkTypes)
    {
        when(mockIssueLinkTypeManager.getIssueLinkType(anyLong(), anyBoolean())).thenAnswer(new Answer<IssueLinkType>()
            {
                @Override
                public IssueLinkType answer(final InvocationOnMock invocation) throws Throwable
                {
                    final Long id = (Long) invocation.getArguments()[0];
                    for (final Iterator<IssueLinkType> iterator = linkTypes.iterator(); iterator.hasNext();)
                    {
                        final IssueLinkType issueLinkType = iterator.next();
                        if (issueLinkType.getId().equals(id))
                        {
                            return issueLinkType;
                        }
                    }
                    fail("Invalid sourceIssue link type id '" + id + "'.");
                    return null;
                }

            });
    }

    private void setupManager(final IssueLinkTypeManager issueLinkTypeManager, final IssueUpdater issueUpdater)
    {
        cacheManager = new MemoryCacheManager();
        dilm = new DefaultIssueLinkManager(ofbiz, new DefaultIssueLinkCreator(issueLinkTypeManager, issueManager), issueLinkTypeManager,
                collectionReorderer, issueUpdater, indexManager, ap, cacheManager)
        {
            @Override
            protected void reindexLinkedIssues(final IssueLink issueLink)
            {
            }
        };
    }

    private List<IssueLink> setupIssueLinkSequence()
    {
        final GenericValue issueLinkTypeGV = UtilsForTests.getTestEntity("IssueLinkType", ImmutableMap.of("outward", "test outward", "inward",
                "test inward", "linkname", "test name"));

        final IssueLinkType issueLinkType = new IssueLinkTypeImpl(issueLinkTypeGV);

        final GenericValue issueLinkGV1 = UtilsForTests.getTestEntity("IssueLink", ImmutableMap.of("source", sourceIssue.getLong("id"), "destination",
                destinationIssue.getLong("id"), "linktype", issueLinkType.getId(), "sequence", new Long(0)));
        final GenericValue issueLinkGV2 = UtilsForTests.getTestEntity("IssueLink", ImmutableMap.of("source", sourceIssue.getLong("id"), "destination",
                destinationIssue.getLong("id"), "linktype", issueLinkType.getId(), "sequence", new Long(1)));
        final GenericValue issueLinkGV3 = UtilsForTests.getTestEntity("IssueLink", ImmutableMap.of("source", sourceIssue.getLong("id"), "destination",
                destinationIssue.getLong("id"), "linktype", issueLinkType.getId(), "sequence", new Long(2)));

        final List<IssueLink> issueLinks = new ArrayList<IssueLink>();

        issueLinks.add(new IssueLinkImpl(issueLinkGV1, null, null));
        issueLinks.add(new IssueLinkImpl(issueLinkGV2, null, null));
        issueLinks.add(new IssueLinkImpl(issueLinkGV3, null, null));
        return issueLinks;
    }

    private List<IssueLink> setupLinks(final List<IssueLinkType> linkTypes, final boolean setupBack, final GenericValue ... issues)
    {
        // Create a link from sourceIssue to destinationIssue for every passed issue link type
        final List<IssueLink> issueLinks = new ArrayList<IssueLink>(linkTypes.size());
        for (final Iterator<IssueLinkType> iterator = linkTypes.iterator(); iterator.hasNext();)
        {
            final IssueLinkType issueLinkType = iterator.next();
            final GenericValue issueLinkGV = UtilsForTests.getTestEntity("IssueLink", ImmutableMap.of("source", issues[0].getLong("id"),
                    "destination", issues[1].getLong("id"), "linktype", issueLinkType.getId()));
            issueLinks.add(new IssueLinkImpl(issueLinkGV, mockIssueLinkTypeManager, issueManager));
            if (setupBack)
            {
                final GenericValue issueLinkGVBack = UtilsForTests.getTestEntity("IssueLink", ImmutableMap.of("source", issues[1].getLong("id"),
                    "destination", issues[0].getLong("id"), "linktype", issueLinkType.getId()));
                issueLinks.add(new IssueLinkImpl(issueLinkGVBack, mockIssueLinkTypeManager, issueManager));
            }
        }

        return issueLinks;
    }

    protected User createMockUser(final String userName, final String name, final String email)
        throws OperationNotPermittedException, InvalidUserException, InvalidCredentialException
    {
        final User user = new MockUser(userName, name, email);
        final CrowdService crowdService = ComponentAccessor.getCrowdService();
        crowdService.addUser(user, "password");
        return user;
    }

    protected User createMockUser(final String userName)
            throws OperationNotPermittedException, InvalidUserException, InvalidCredentialException
    {
        return createMockUser(userName, userName, "");
    }

    protected void addUserToGroup(final User user, final Group group)
        throws OperationNotPermittedException, InvalidGroupException
    {
        final CrowdService crowdService = ComponentAccessor.getCrowdService();
        crowdService.addUserToGroup(user, group);
    }

    protected void addUserToGroup(final ApplicationUser user, final Group group)
        throws OperationNotPermittedException, InvalidGroupException
    {
        final CrowdService crowdService = ComponentAccessor.getCrowdService();
        crowdService.addUserToGroup(user != null ? user.getDirectoryUser() : null, group);
    }

    protected Group createMockGroup(final String groupName)
        throws OperationNotPermittedException, InvalidGroupException
    {
        final Group group = new MockGroup(groupName);
        final CrowdService crowdService = ComponentAccessor.getCrowdService();
        crowdService.addGroup(group);
        return group;
    }

}

abstract class MyIssueUpdater implements IssueUpdater
{
    abstract public void verify();
}

