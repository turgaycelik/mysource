/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.managers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.ofbiz.core.entity.EntityUtil;
import org.ofbiz.core.entity.GenericDelegator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import com.atlassian.cache.memory.MemoryCacheManager;
import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.impl.IdentifierUtils;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldException;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.NavigableField;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayout;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutItem;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutItemImpl;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutManager;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutStorageException;
import com.atlassian.jira.issue.fields.layout.column.DefaultColumnLayoutManager;
import com.atlassian.jira.issue.fields.layout.column.EditableSearchRequestColumnLayout;
import com.atlassian.jira.issue.fields.layout.column.EditableSearchRequestColumnLayoutImpl;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectImpl;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.query.QueryImpl;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

@RunWith (MockitoJUnitRunner.class)
public class TestDefaultColumnLayoutManager
{
    @Rule
    public RuleChain mockitoMocks = MockitoMocksInContainer.forTest(this);

    @Mock
    private FieldLayoutManager mockFieldLayoutManager;
    @Mock
    private PermissionManager mockPermissionManager;
    @AvailableInContainer
    @Mock
    private FieldManager mockDefaultFieldManager;
    private ColumnLayoutManager columnLayoutManager;
    private GenericValue cf1;
    private GenericValue cf2;
    private GenericValue cf3;
    private GenericValue cf4;
    private GenericValue project;
    private GenericValue project2;
    private GenericValue issueType;
    private GenericValue issueType2;
    private ApplicationUser mockApplicationUser;
    private ApplicationUser mockAdminApplicationUser;
    @AvailableInContainer
    MockOfBizDelegator mockOfBizDelegator = new MockOfBizDelegator();
    private GenericValue fieldLayout;
    private List projects;
    private ArrayList<ProjectImpl> projectObjs;
    private GenericValue columnLayout;
    private final Map<String, NavigableField> navigableFields = Maps.newHashMap();
    private final Set<String> hiddenFields = Sets.newHashSet();
    private SearchRequest searchRequest;
    @Mock
    @AvailableInContainer
    private UserManager mockUserManager;

    @Before
    public void setUp() throws Exception
    {
        setupDefaultNavigableFields();
        mockApplicationUser = new MockApplicationUser("testUser");
        mockAdminApplicationUser = new MockApplicationUser("adminUser");
        project = createGv("Project", FieldMap.build("key", "ABC"));
        project2 = createGv("Project", FieldMap.build("key", "BCD"));
        projects = EasyList.build(project, project2);
        projectObjs = Lists.newArrayList(new ProjectImpl(project), new ProjectImpl(project2));
        issueType = createGv("IssueType", FieldMap.build("id", 1L, "name", "iType1", "sequence", new Long(1)));
        issueType2 = createGv("IssueType", FieldMap.build("id", 2L, "name", "iType2", "sequence", new Long(2)));

        cf1 = createGv("CustomField", FieldMap.build("id", 1L, "name", "Custom Field 1"));
        cf2 = createGv("CustomField", FieldMap.build("id", 2L, "name", "Custom Field 2", "project", project.getLong("id")));
        cf3 = createGv("CustomField", FieldMap.build("id", 3L, "name", "Custom Field 3", "issuetype",
                issueType.getString("id")));
        cf4 = createGv("CustomField", FieldMap.build("id", 4L, "name", "Custom Field 4", "issuetype",
                issueType2.getString("id")));
        when(mockPermissionManager.hasPermission(eq(Permissions.BROWSE), eq(project), any(User.class))).thenReturn(true);
        when(mockDefaultFieldManager.getAllAvailableNavigableFields()).thenReturn(Sets.newHashSet(navigableFields.values()));
        when(mockDefaultFieldManager.getAvailableNavigableFields(any(User.class))).thenReturn(filterHiddenFields());
        when(mockDefaultFieldManager.isNavigableField(anyString())).thenAnswer(new Answer<Boolean>()
        {
            @Override
            public Boolean answer(final InvocationOnMock invocation) throws Throwable
            {
                final Object[] args = invocation.getArguments();
                return navigableFields.containsKey(args[0]);
            }
        });
        when(mockDefaultFieldManager.getNavigableField(anyString())).thenAnswer(new Answer<NavigableField>()
        {
            @Override
            public NavigableField answer(final InvocationOnMock invocation) throws Throwable
            {
                final Object[] args = invocation.getArguments();
                return navigableFields.get(args[0]);
            }
        });
        when(mockDefaultFieldManager.getField(anyString())).thenAnswer(new Answer<Field>()
        {
            @Override
            public Field answer(final InvocationOnMock invocation) throws Throwable
            {
                final Object[] args = invocation.getArguments();
                return navigableFields.get(args[0]);
            }
        });
        final GenericValue project = mockOfBizDelegator.createValue("Project", FieldMap.build("name", "testproject"));
        when(mockPermissionManager.getProjectObjects(Permissions.BROWSE, mockApplicationUser.getDirectoryUser())).
                thenReturn(Lists.<Project>newArrayList(new ProjectImpl(project)));
        when(mockUserManager.getUserByName(mockApplicationUser.getName())).thenReturn(mockApplicationUser);
        searchRequest = new SearchRequest(new QueryImpl(), mockApplicationUser.getName(), "Test Search Request", "This is a Test Search Request", 13L, 0L);
        columnLayoutManager = new DefaultColumnLayoutManager(mockDefaultFieldManager, mockOfBizDelegator, ComponentAccessor.getUserKeyService(), new MemoryCacheManager());
    }

    //      The method tests that the column layout returns the default column layout when the search
    //      request does not have a column layout, and the logged in user does not have a custom
    //      column layout
    @Test
    public void testGetColumnLayoutDefaultColumns() throws ColumnLayoutStorageException
    {
        setupDefaultFieldLayout(0);

        final ColumnLayout columnLayout = columnLayoutManager.getColumnLayout(mockApplicationUser.getDirectoryUser(), searchRequest);

        // 10 should be returned - all of the 11 which are the default setup in the AbstractColumnLayoutManager,
        // method without assignee as it hidden by the setupDefaultFieldLayout method invoked above.
        assertEquals(10, columnLayout.getColumnLayoutItems().size());
        assertFalse(columnLayout.contains(mockDefaultFieldManager.getNavigableField(IssueFieldConstants.ASSIGNEE)));
    }

    //     The method tests that the column layout returns the user column layout when the search
    //     request does not have a column layout, but the user does

    @Test
    public void testGetColumnLayoutUserColumns() throws ColumnLayoutStorageException
    {
        setupDefaultFieldLayout(0);

        // Setup layout for the mockApplicationUser
        setupColumnLayoutWithHiddenColumn(mockApplicationUser, 0);

        final ColumnLayout columnLayout = columnLayoutManager.getColumnLayout(mockApplicationUser.getDirectoryUser(), searchRequest);

        // 2 should be returned - the ones that are setup by the setupColumnLayoutWithHiddenColumn method, invoked
        // above, except the ASSIGNEE and ENVIRONMENT fields that are hidden by the setupDefaultFieldLayout invoked above
        assertEquals(2, columnLayout.getColumnLayoutItems().size());
        assertFalse(columnLayout.contains(mockDefaultFieldManager.getNavigableField(IssueFieldConstants.ASSIGNEE)));
        assertFalse(columnLayout.contains(mockDefaultFieldManager.getNavigableField(IssueFieldConstants.ENVIRONMENT)));
    }

    //    The method tests that the column layout returns the user column layout when the search
    //    request does not have a column layout, but the user does
    @Test
    public void testGetColumnLayoutSearchRequestColumns() throws ColumnLayoutStorageException
    {
        setupDefaultFieldLayout(0);

        // Setup layout for the mockApplicationUser
        setupSearchRequestColumnLayoutWithHiddenColumn(searchRequest, 0);

        final ColumnLayout columnLayout = columnLayoutManager.getColumnLayout(mockApplicationUser.getDirectoryUser(), searchRequest);

        // 3 should be returned - the ones that are setup by the setupColumnLayoutWithHiddenColumn method, invoked
        // above, except the ASSIGNEE and ENVIRONMENT fields that are hidden by the setupDefaultFieldLayout invoked above
        assertEquals(3, columnLayout.getColumnLayoutItems().size());
        assertFalse(columnLayout.contains(mockDefaultFieldManager.getNavigableField(IssueFieldConstants.ASSIGNEE)));
        assertFalse(columnLayout.contains(mockDefaultFieldManager.getNavigableField(IssueFieldConstants.ENVIRONMENT)));
    }

    @Test
    public void testGetEditableSearchRequestColumnLayoutNullUser() throws ColumnLayoutStorageException
    {
        try
        {
            columnLayoutManager.getEditableSearchRequestColumnLayout(null, searchRequest);
            fail("IllegalArgumentException should have been thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            assertEquals("user should not be null!", e.getMessage());
        }
    }

    @Test
    public void testGetEditableSearchRequestColumnLayoutNullSearchRequest() throws ColumnLayoutStorageException
    {
        try
        {
            columnLayoutManager.getEditableSearchRequestColumnLayout(mockApplicationUser.getDirectoryUser(), null);
            fail("IllegalArgumentException should have been thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            assertEquals("searchRequest should not be null!", e.getMessage());
        }
    }

    //     The method tests that the editable column layout returns the default column layout when the search
    //     request does not have a column layout, and the logged in user does not have a custom
    //     column layout
    @Test
    public void testGetEditableColumnLayoutDefaultColumns() throws ColumnLayoutStorageException
    {
        setupDefaultFieldLayout(0);

        final ColumnLayout columnLayout = columnLayoutManager.getEditableSearchRequestColumnLayout(mockApplicationUser.getDirectoryUser(), searchRequest);

        // 10 should be returned - all of the 11 which are the default setup in the AbstractColumnLayoutManager,
        // method without assignee as it hidden by the setupDefaultFieldLayout method invoked above.
        assertEquals(10, columnLayout.getColumnLayoutItems().size());
        assertFalse(columnLayout.contains(mockDefaultFieldManager.getNavigableField(IssueFieldConstants.ASSIGNEE)));
    }

    //     The method tests that the column layout returns the user column layout when the search
    //     request does not have a column layout, but the user does

    @Test
    public void testEditableGetColumnLayoutUserColumns() throws ColumnLayoutStorageException
    {
        setupDefaultFieldLayout(0);

        // Setup layout for the mockApplicationUser
        setupColumnLayoutWithHiddenColumn(mockApplicationUser, 0);

        final ColumnLayout columnLayout = columnLayoutManager.getEditableSearchRequestColumnLayout(mockApplicationUser.getDirectoryUser(), searchRequest);

        // 2 should be returned - the ones that are setup by the setupColumnLayoutWithHiddenColumn method, invoked
        // above, except the ASSIGNEE and ENVIRONMENT fields that are hidden by the setupDefaultFieldLayout invoked above
        assertEquals(2, columnLayout.getColumnLayoutItems().size());
        assertFalse(columnLayout.contains(mockDefaultFieldManager.getNavigableField(IssueFieldConstants.ASSIGNEE)));
        assertFalse(columnLayout.contains(mockDefaultFieldManager.getNavigableField(IssueFieldConstants.ENVIRONMENT)));
    }

    //     The method tests that the column layout returns the user column layout when the search
    //     request does not have a column layout, but the user does

    @Test
    public void testGetEditableColumnLayoutSearchRequestColumns() throws ColumnLayoutStorageException
    {
        setupDefaultFieldLayout(0);

        // Setup layout for the mockApplicationUser
        setupSearchRequestColumnLayoutWithHiddenColumn(searchRequest, 0);

        final ColumnLayout columnLayout = columnLayoutManager.getEditableSearchRequestColumnLayout(mockApplicationUser.getDirectoryUser(), searchRequest);

        // 3 should be returned - the ones that are setup by the setupColumnLayoutWithHiddenColumn method, invoked
        // above, except the ASSIGNEE and ENVIRONMENT fields that are hidden by the setupDefaultFieldLayout invoked above
        assertEquals(3, columnLayout.getColumnLayoutItems().size());
        assertFalse(columnLayout.contains(mockDefaultFieldManager.getNavigableField(IssueFieldConstants.ASSIGNEE)));
        assertFalse(columnLayout.contains(mockDefaultFieldManager.getNavigableField(IssueFieldConstants.ENVIRONMENT)));
    }


    @Test
    public void testStoreEditableSearchRequestColumnLayoutNoOriginalEntries()
            throws ColumnLayoutStorageException, GenericEntityException
    {
        // Make a Column Layout
        int position = 0;
        final List columnLayoutItems = new ArrayList();
        columnLayoutItems.add(new ColumnLayoutItemImpl(mockDefaultFieldManager.getNavigableField(IssueFieldConstants.ISSUE_KEY), position++));
        columnLayoutItems.add(new ColumnLayoutItemImpl(mockDefaultFieldManager.getNavigableField(IssueFieldConstants.ISSUE_TYPE), position++));
        columnLayoutItems.add(new ColumnLayoutItemImpl(mockDefaultFieldManager.getNavigableField(IssueFieldConstants.PRIORITY), position++));

        final EditableSearchRequestColumnLayout editableSearchRequestColumnLayout = new EditableSearchRequestColumnLayoutImpl(columnLayoutItems,
                mockApplicationUser.getDirectoryUser(), searchRequest);
        columnLayoutManager.storeEditableSearchRequestColumnLayout(editableSearchRequestColumnLayout);

        final GenericValue columnLayoutGV = EntityUtil.getOnly(mockOfBizDelegator.findByAnd("ColumnLayout", FieldMap.build("username", null,
                "searchrequest", searchRequest.getId())));
        assertNotNull(columnLayoutGV);
        final List<GenericValue> columnLayoutItemGVs = mockOfBizDelegator.findByAnd("ColumnLayoutItem", FieldMap.build("columnlayout", columnLayoutGV.getLong("id")));
        assertEquals(3, columnLayoutItemGVs.size());

        GenericValue columnLayoutItemGV = columnLayoutItemGVs.get(0);
        assertEquals(IssueFieldConstants.ISSUE_KEY, columnLayoutItemGV.getString("fieldidentifier"));
        assertEquals(0L, columnLayoutItemGV.getLong("horizontalposition").longValue());
        columnLayoutItemGV = columnLayoutItemGVs.get(1);
        assertEquals(IssueFieldConstants.ISSUE_TYPE, columnLayoutItemGV.getString("fieldidentifier"));
        assertEquals(1L, columnLayoutItemGV.getLong("horizontalposition").longValue());
        columnLayoutItemGV = columnLayoutItemGVs.get(2);
        assertEquals(IssueFieldConstants.PRIORITY, columnLayoutItemGV.getString("fieldidentifier"));
        assertEquals(2L, columnLayoutItemGV.getLong("horizontalposition").longValue());
    }

    @Test
    public void testStoreEditableSearchRequestColumnLayoutWithOriginalEntries()
            throws ColumnLayoutStorageException, GenericEntityException
    {
        final List<GenericValue> childColumnLayoutItems = Lists.newArrayList();
        // Create existing search request
        int position = 0;
        final Long filterId = searchRequest.getId();
        GenericValue columnLayoutGV = createGv("ColumnLayout", FieldMap.build("username", null, "searchrequest", filterId));
        childColumnLayoutItems.add(createGv("ColumnLayoutItem", FieldMap.build("columnlayout", columnLayoutGV.getLong("id"), "fieldidentifier",
                IssueFieldConstants.ENVIRONMENT, "horizontalposition", new Long(position++))));
        childColumnLayoutItems.add(createGv("ColumnLayoutItem", FieldMap.build("columnlayout", columnLayoutGV.getLong("id"), "fieldidentifier",
                IssueFieldConstants.DESCRIPTION, "horizontalposition", new Long(position++))));
        childColumnLayoutItems.add(createGv("ColumnLayoutItem", FieldMap.build("columnlayout", columnLayoutGV.getLong("id"), "fieldidentifier",
                IssueFieldConstants.RESOLUTION, "horizontalposition", new Long(position++))));
        childColumnLayoutItems.add(createGv("ColumnLayoutItem", FieldMap.build("columnlayout", columnLayoutGV.getLong("id"), "fieldidentifier",
                IssueFieldConstants.REPORTER, "horizontalposition", new Long(position++))));
        mockOfBizDelegator.addRelatedMap("ChildColumnLayoutItem", columnLayoutGV, childColumnLayoutItems);
        // Make a Column Layout
        position = 0;

        final List columnLayoutItems = new ArrayList();
        columnLayoutItems.add(new ColumnLayoutItemImpl(mockDefaultFieldManager.getNavigableField(IssueFieldConstants.ISSUE_KEY), position++));
        columnLayoutItems.add(new ColumnLayoutItemImpl(mockDefaultFieldManager.getNavigableField(IssueFieldConstants.ISSUE_TYPE), position++));
        columnLayoutItems.add(new ColumnLayoutItemImpl(mockDefaultFieldManager.getNavigableField(IssueFieldConstants.PRIORITY), position++));

        final EditableSearchRequestColumnLayout editableSearchRequestColumnLayout = new EditableSearchRequestColumnLayoutImpl(columnLayoutItems,
                mockApplicationUser.getDirectoryUser(), searchRequest);
        columnLayoutManager.storeEditableSearchRequestColumnLayout(editableSearchRequestColumnLayout);

        columnLayoutGV = EntityUtil.getOnly(mockOfBizDelegator.findByAnd("ColumnLayout", FieldMap.build("username", null, "searchrequest",
                searchRequest.getId())));
        assertNotNull(columnLayoutGV);

        final List<GenericValue> columnLayoutItemGVs = mockOfBizDelegator.findByAnd("ColumnLayoutItem", FieldMap.build("columnlayout", columnLayoutGV.getLong("id")));
        assertEquals(3, columnLayoutItemGVs.size());

        GenericValue columnLayoutItemGV = columnLayoutItemGVs.get(0);
        assertEquals(IssueFieldConstants.ISSUE_KEY, columnLayoutItemGV.getString("fieldidentifier"));
        assertEquals(0L, columnLayoutItemGV.getLong("horizontalposition").longValue());
        columnLayoutItemGV = columnLayoutItemGVs.get(1);
        assertEquals(IssueFieldConstants.ISSUE_TYPE, columnLayoutItemGV.getString("fieldidentifier"));
        assertEquals(1L, columnLayoutItemGV.getLong("horizontalposition").longValue());
        columnLayoutItemGV = columnLayoutItemGVs.get(2);
        assertEquals(IssueFieldConstants.PRIORITY, columnLayoutItemGV.getString("fieldidentifier"));
        assertEquals(2L, columnLayoutItemGV.getLong("horizontalposition").longValue());
    }

    @Test
    public void testStoreEditableSearchRequestColumnLayoutNoEntries()
            throws ColumnLayoutStorageException, GenericEntityException
    {
        columnLayoutManager.restoreSearchRequestColumnLayout(searchRequest);

        final GenericDelegator genericDelegator = CoreFactory.getGenericDelegator();
        final GenericValue columnLayoutGV = EntityUtil.getOnly(genericDelegator.findByAnd("ColumnLayout", FieldMap.build("username", null,
                "searchrequest", searchRequest.getId())));
        assertNull(columnLayoutGV);
    }

    @Test
    public void testStoreEditableSearchRequestColumnLayoutWithEntries()
            throws ColumnLayoutStorageException, GenericEntityException
    {
        final List<GenericValue> childColumnLayoutItems = Lists.newArrayList();
        // Create exiting search request
        int position = 0;
        final Long filterId = searchRequest.getId();
        GenericValue columnLayoutGV = createGv("ColumnLayout", FieldMap.build("username", null, "searchrequest", filterId));
        final Long columnLayoutId = columnLayoutGV.getLong("id");
        childColumnLayoutItems.add(createGv("ColumnLayoutItem", FieldMap.build("columnlayout", columnLayoutGV.getLong("id"), "fieldidentifier",
                IssueFieldConstants.ENVIRONMENT, "horizontalposition", new Long(position++))));
        childColumnLayoutItems.add(createGv("ColumnLayoutItem", FieldMap.build("columnlayout", columnLayoutGV.getLong("id"), "fieldidentifier",
                IssueFieldConstants.DESCRIPTION, "horizontalposition", new Long(position++))));
        childColumnLayoutItems.add(createGv("ColumnLayoutItem", FieldMap.build("columnlayout", columnLayoutGV.getLong("id"), "fieldidentifier",
                IssueFieldConstants.RESOLUTION, "horizontalposition", new Long(position++))));
        childColumnLayoutItems.add(createGv("ColumnLayoutItem", FieldMap.build("columnlayout", columnLayoutGV.getLong("id"), "fieldidentifier",
                IssueFieldConstants.REPORTER, "horizontalposition", new Long(position++))));
        mockOfBizDelegator.addRelatedMap("ChildColumnLayoutItem", columnLayoutGV, childColumnLayoutItems);
        columnLayoutManager.restoreSearchRequestColumnLayout(searchRequest);

        columnLayoutGV = EntityUtil.getOnly(mockOfBizDelegator.findByAnd("ColumnLayout", FieldMap.build("username", null, "searchrequest",
                searchRequest.getId())));
        assertNull(columnLayoutGV);

        final List columnLayoutItemGVs = mockOfBizDelegator.findByAnd("ColumnLayoutItem", FieldMap.build("id", columnLayoutId));
        assertTrue(columnLayoutItemGVs.isEmpty());
    }

    @Test
    public void testObsoleteCustomFieldLayoutItemsAreIgnored() throws ColumnLayoutStorageException
    {
        // Setup permissions
        when(mockPermissionManager.getProjects(Permissions.BROWSE, mockApplicationUser)).thenReturn(projects);

        // Setup fields
        setupObsoleteCustomFieldColumnItems(null, 0);

        final List columnLayoutItems = columnLayoutManager.getEditableDefaultColumnLayout().getColumnLayoutItems();
        Assert.assertEquals(4, columnLayoutItems.size());
    }

    @Test
    public void testHiddenFieldsNotShown() throws ColumnLayoutStorageException
    {
        // Setup permissions
        when(mockPermissionManager.getProjectObjects(Permissions.BROWSE, mockApplicationUser.getDirectoryUser())).thenReturn(Lists.<Project>newArrayList(projectObjs));

        // Setup fields with environment hiodden
        setupDefaultFieldLayout(0);
        setupColumnLayoutWithHiddenColumn(mockApplicationUser, 0);

        final List columnLayoutItems = columnLayoutManager.getColumnLayout(mockApplicationUser.getDirectoryUser()).getColumnLayoutItems();
        _testFieldMissing(mockDefaultFieldManager.getField(IssueFieldConstants.ENVIRONMENT), columnLayoutItems);
    }

    /**
     * This is a test for TF-103.  Basically one use with less permissions to see fields than another could cause
     * incorrect column layouts to be cached.
     */
    @Test
    public void testGetSearchRequestLayoutCacheConsistency() throws ColumnLayoutStorageException, FieldException
    {
        //first clear the cache.
        columnLayoutManager.refresh();

        final HashSet<NavigableField> allFields = Sets.newHashSet(navigableFields.values());
        final HashMap<String, NavigableField> copyOfNaviableFields = Maps.newHashMap(navigableFields);
        copyOfNaviableFields.remove("summary");
        final HashSet<NavigableField> userFields = Sets.newHashSet(copyOfNaviableFields.values());
        when(mockDefaultFieldManager.getAvailableNavigableFields(mockApplicationUser.getDirectoryUser())).thenReturn(userFields);

        //now lookup a columnlayout as a user that doesn't have permission to see the 'summary' field
        final ColumnLayout userColumnLayout = columnLayoutManager.getColumnLayout(mockApplicationUser.getDirectoryUser(), searchRequest);

        assertEquals(10, userColumnLayout.getColumnLayoutItems().size());

        when(mockDefaultFieldManager.getAvailableNavigableFields(mockAdminApplicationUser.getDirectoryUser())).thenReturn(allFields);

        //now do another lookup with a user that does have permission to see the 'summary' field.
        final ColumnLayout adminColumnLayout = columnLayoutManager.getColumnLayout(mockAdminApplicationUser.getDirectoryUser(), searchRequest);
        assertEquals(11, adminColumnLayout.getColumnLayoutItems().size());
    }

    private List<GenericValue> fromColumnLayoutImpl(final List<ColumnLayoutItemImpl> cliImpl, final GenericValue columnLayoutGV)
    {
        return Lists.transform(cliImpl, new Function<ColumnLayoutItemImpl, GenericValue>()
        {
            @Override
            public GenericValue apply(@Nullable final ColumnLayoutItemImpl input)
            {
                return createGv("ColumnLayoutItem", FieldMap.build("columnlayout", columnLayoutGV.getLong("id"), "fieldidentifier",
                        input.getNavigableField().getId(), "horizontalposition", (long) input.getPosition()));
            }
        });
    }


    private Set<NavigableField> filterHiddenFields()
    {
        return Sets.filter(Sets.newHashSet(navigableFields.values()), new Predicate<NavigableField>()
        {
            @Override
            public boolean apply(@Nullable final NavigableField input)
            {
                return !hiddenFields.contains(input.getId());
            }
        });
    }

    private void setupDefaultNavigableFields()
    {
        addNavigableFieldLayout(IssueFieldConstants.ISSUE_KEY);
        addNavigableFieldLayout(IssueFieldConstants.ISSUE_TYPE);
        addNavigableFieldLayout(IssueFieldConstants.CREATED);
        addNavigableFieldLayout(IssueFieldConstants.UPDATED);
        addNavigableFieldLayout(IssueFieldConstants.DUE_DATE);
        addNavigableFieldLayout(IssueFieldConstants.PRIORITY);
        addNavigableFieldLayout(IssueFieldConstants.COMPONENTS);
        addNavigableFieldLayout(IssueFieldConstants.AFFECTED_VERSIONS);
        addNavigableFieldLayout(IssueFieldConstants.FIX_FOR_VERSIONS);
        addNavigableFieldLayout(IssueFieldConstants.ASSIGNEE);
        addNavigableFieldLayout(IssueFieldConstants.ENVIRONMENT);
        addNavigableFieldLayout(IssueFieldConstants.SUMMARY);
        addNavigableFieldLayout(IssueFieldConstants.REPORTER);
        addNavigableFieldLayout(IssueFieldConstants.STATUS);
        addNavigableFieldLayout(IssueFieldConstants.RESOLUTION);
        addNavigableFieldLayout("1");
        addNavigableFieldLayout("2");
        addNavigableFieldLayout("3");
        addNavigableFieldLayout("4");
        addNavigableFieldLayout("random_string");
    }

    private void addNavigableFieldLayout(final String id)
    {
        final NavigableField mockField = mock(NavigableField.class);
        when(mockField.getId()).thenReturn(id);
        when(mockField.getNameKey()).thenReturn(id);
        navigableFields.put(id, mockField);
    }

    private int setupDefaultFieldLayout(int position)
    {
        fieldLayout = createGv("FieldLayout", FieldMap.build("type", FieldLayoutManager.TYPE_DEFAULT));
        createGv("FieldLayoutItem", FieldMap.build("fieldlayout", fieldLayout.getLong("id"), "fieldidentifier",
                IssueFieldConstants.ISSUE_TYPE, "verticalposition", new Long(position++), "ishidden", Boolean.FALSE.toString(), "isrequired",
                Boolean.TRUE.toString()));
        createGv("FieldLayoutItem", FieldMap.build("fieldlayout", fieldLayout.getLong("id"), "fieldidentifier",
                IssueFieldConstants.DUE_DATE, "verticalposition", new Long(position++), "ishidden", Boolean.FALSE.toString(), "isrequired",
                Boolean.TRUE.toString()));
        createGv("FieldLayoutItem", FieldMap.build("fieldlayout", fieldLayout.getLong("id"), "fieldidentifier",
                IssueFieldConstants.PRIORITY, "verticalposition", new Long(position++), "ishidden", Boolean.FALSE.toString(), "isrequired",
                Boolean.FALSE.toString()));
        createGv("FieldLayoutItem", FieldMap.build("fieldlayout", fieldLayout.getLong("id"), "fieldidentifier",
                IssueFieldConstants.COMPONENTS, "verticalposition", new Long(position++), "ishidden", Boolean.FALSE.toString(), "isrequired",
                Boolean.FALSE.toString()));
        createGv("FieldLayoutItem", FieldMap.build("fieldlayout", fieldLayout.getLong("id"), "fieldidentifier",
                IssueFieldConstants.AFFECTED_VERSIONS, "verticalposition", new Long(position++), "ishidden", Boolean.FALSE.toString(), "isrequired",
                Boolean.FALSE.toString()));
        createGv("FieldLayoutItem", FieldMap.build("fieldlayout", fieldLayout.getLong("id"), "fieldidentifier",
                IssueFieldConstants.FIX_FOR_VERSIONS, "verticalposition", new Long(position++), "ishidden", Boolean.FALSE.toString(), "isrequired",
                Boolean.FALSE.toString()));
        createGv("FieldLayoutItem", FieldMap.build("fieldlayout", fieldLayout.getLong("id"), "fieldidentifier",
                IssueFieldConstants.ASSIGNEE, "verticalposition", new Long(position++), "ishidden", Boolean.TRUE.toString(), "isrequired",
                Boolean.TRUE.toString()));
        hiddenFields.add(IssueFieldConstants.ASSIGNEE);
        createGv("FieldLayoutItem", FieldMap.build("fieldlayout", fieldLayout.getLong("id"), "fieldidentifier",
                IssueFieldConstants.ENVIRONMENT, "verticalposition", new Long(position++), "ishidden", Boolean.TRUE.toString(), "isrequired",
                Boolean.FALSE.toString()));
        hiddenFields.add(IssueFieldConstants.ENVIRONMENT);
        createGv("FieldLayoutItem", FieldMap.build("fieldlayout", fieldLayout.getLong("id"), "fieldidentifier",
                IssueFieldConstants.SUMMARY, "verticalposition", new Long(position++), "ishidden", Boolean.FALSE.toString(), "isrequired",
                Boolean.TRUE.toString()));
        return position;
    }

    private int setupSearchRequestColumnLayoutWithHiddenColumn(final SearchRequest searchRequest, int position)
    {
        final List<GenericValue> childColumnLayoutItems = Lists.newArrayList();
        // Setup search request navigator columns with one hidden column
        columnLayout = createGv("ColumnLayout", FieldMap.build("searchrequest", searchRequest.getId()));
        childColumnLayoutItems.add(createGv("ColumnLayoutItem", FieldMap.build("columnlayout", columnLayout.getLong("id"), "fieldidentifier",
                IssueFieldConstants.ISSUE_KEY, "horizontalposition", new Long(position++))));
        childColumnLayoutItems.add(createGv("ColumnLayoutItem", FieldMap.build("columnlayout", columnLayout.getLong("id"), "fieldidentifier",
                IssueFieldConstants.SUMMARY, "horizontalposition", new Long(position++))));
        childColumnLayoutItems.add(createGv("ColumnLayoutItem", FieldMap.build("columnlayout", columnLayout.getLong("id"), "fieldidentifier",
                IssueFieldConstants.COMPONENTS, "horizontalposition", new Long(position++))));
        childColumnLayoutItems.add(createGv("ColumnLayoutItem", FieldMap.build("columnlayout", columnLayout.getLong("id"), "fieldidentifier",
                IssueFieldConstants.ENVIRONMENT, "horizontalposition", new Long(position++))));
        childColumnLayoutItems.add(createGv("ColumnLayoutItem", FieldMap.build("columnlayout", columnLayout.getLong("id"), "fieldidentifier",
                IssueFieldConstants.ASSIGNEE, "horizontalposition", new Long(position++))));
        mockOfBizDelegator.addRelatedMap("ChildColumnLayoutItem", columnLayout, childColumnLayoutItems);
        return position;
    }

    private GenericValue createGv(final String entityName, final FieldMap fields)
    {
        final GenericValue gv = mockOfBizDelegator.createValue(entityName, fields);
        return gv;
    }

    private int setupCustomFieldLayoutItems(int position)
    {
        position = setupDefaultFieldLayout(position) + 1;
        createGv("FieldLayoutItem", FieldMap.build("fieldlayout", fieldLayout.getLong("id"), "fieldidentifier",
                ComponentAccessor.getCustomFieldManager().getCustomFieldObject(cf1.getLong("id")).getId(), "verticalposition", new Long(position++),
                "ishidden", Boolean.FALSE.toString(), "isrequired", Boolean.FALSE.toString()));
        createGv("FieldLayoutItem", FieldMap.build("fieldlayout", fieldLayout.getLong("id"), "fieldidentifier",
                ComponentAccessor.getCustomFieldManager().getCustomFieldObject(cf2.getLong("id")).getId(), "verticalposition", new Long(position++),
                "ishidden", Boolean.FALSE.toString(), "isrequired", Boolean.FALSE.toString()));
        createGv("FieldLayoutItem", FieldMap.build("fieldlayout", fieldLayout.getLong("id"), "fieldidentifier",
                ComponentAccessor.getCustomFieldManager().getCustomFieldObject(cf3.getLong("id")).getId(), "verticalposition", new Long(position++),
                "ishidden", Boolean.FALSE.toString(), "isrequired", Boolean.TRUE.toString()));
        return position;
    }

    private int setupObsoleteFieldLayoutItems(int position)
    {
        position = setupCustomFieldLayoutItems(position);
        createGv("FieldLayoutItem", FieldMap.build("fieldlayout", fieldLayout.getLong("id"), "fieldidentifier", "random_string",
                "verticalposition", new Long(position++), "ishidden", Boolean.FALSE.toString(), "isrequired", Boolean.FALSE.toString()));
        return position;
    }

    private int setupObsoleteCustomFieldColumnItems(final ApplicationUser user, int position)
    {
        position = setupColumnLayoutWithHiddenColumn(user, position);
        final GenericValue cli1 = createGv("ColumnLayoutItem", FieldMap.build("columnlayout", columnLayout.getLong("id"), "fieldidentifier",
                FieldManager.CUSTOM_FIELD_PREFIX + "1000", "horizontalposition", new Long(position++)));
        mockOfBizDelegator.addRelatedMap("ChildColumnLayoutItem", columnLayout, cli1);
        return position;
    }

    private int setupColumnLayoutWithHiddenColumn(final ApplicationUser user, int position)
    {
        final String username = user != null ? user.getName() : null;
        final String userKey = (username != null) ? IdentifierUtils.toLowerCase(username) : null;
        final List<GenericValue> childColumnLayoutItems = Lists.newArrayList();

        // Setup user navigator columns with one hidden column
        columnLayout = createGv("ColumnLayout", FieldMap.build("username", userKey));
        childColumnLayoutItems.add(createGv("ColumnLayoutItem", FieldMap.build("columnlayout", columnLayout.getLong("id"), "fieldidentifier",
                IssueFieldConstants.ISSUE_KEY, "horizontalposition", new Long(position++))));
        childColumnLayoutItems.add(createGv("ColumnLayoutItem", FieldMap.build("columnlayout", columnLayout.getLong("id"), "fieldidentifier",
                IssueFieldConstants.SUMMARY, "horizontalposition", new Long(position++))));
        childColumnLayoutItems.add(createGv("ColumnLayoutItem", FieldMap.build("columnlayout", columnLayout.getLong("id"), "fieldidentifier",
                IssueFieldConstants.ENVIRONMENT, "horizontalposition", new Long(position++))));
        childColumnLayoutItems.add(createGv("ColumnLayoutItem", FieldMap.build("columnlayout", columnLayout.getLong("id"), "fieldidentifier",
                IssueFieldConstants.ASSIGNEE, "horizontalposition", new Long(position++))));
        mockOfBizDelegator.addRelatedMap("ChildColumnLayoutItem", columnLayout, childColumnLayoutItems);
        return position;
    }

    private void _testFieldMissing(final Field field, final List columnLayoutItems)
    {
        for (int i = 0; i < columnLayoutItems.size(); i++)
        {
            final ColumnLayoutItem columnLayoutItem = (ColumnLayoutItem) columnLayoutItems.get(i);
            if (columnLayoutItem.getNavigableField().equals(field))
            {
                Assert.fail("The field '" + field.getNameKey() + "' should not be present in the list.");
            }
        }
    }

}
