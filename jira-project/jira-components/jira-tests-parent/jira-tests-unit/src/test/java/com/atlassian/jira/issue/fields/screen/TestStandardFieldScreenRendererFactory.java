package com.atlassian.jira.issue.fields.screen;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.MockFieldManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.layout.field.MockFieldLayout;
import com.atlassian.jira.issue.fields.layout.field.MockFieldLayoutManager;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeManager;
import com.atlassian.jira.issue.fields.screen.tab.FieldScreenTabRendererFactoryImpl;
import com.atlassian.jira.issue.fields.screen.tab.IssueTabRendererDto;
import com.atlassian.jira.issue.operation.IssueOperation;
import com.atlassian.jira.issue.operation.IssueOperations;
import com.atlassian.jira.junit.rules.InitMockitoMocks;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.util.Predicate;
import com.atlassian.jira.util.Predicates;

import com.google.common.collect.Lists;
import com.opensymphony.workflow.loader.ActionDescriptor;

import org.easymock.classextension.IMocksControl;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;

import static org.easymock.classextension.EasyMock.createControl;
import static org.easymock.classextension.EasyMock.expect;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test for {@link com.atlassian.jira.issue.fields.screen.StandardFieldScreenRendererFactory}.
 *
 * @since v4.1
 */
public class TestStandardFieldScreenRendererFactory
{

    @Rule
    public InitMockitoMocks initMockitoMocks = new InitMockitoMocks(this);
    @Mock
    private FieldScreenTabRendererFactoryImpl fieldScreenTabRendererFactory;

    @Test
    public void testCreateFieldScreenRendererOperation() throws Exception
    {
        final IMocksControl mocksControl = createControl();

        final FieldScreenRenderer mockRenderer = new MockFieldScreenRenderer();
        final FieldScreen mockFieldScreen = new MockFieldScreen();
        final Issue mockIssue = new MockIssue(5);
        final FieldScreenScheme mockScreenScheme = mocksControl.createMock(FieldScreenScheme.class);
        final IssueTypeScreenSchemeManager manager = mocksControl.createMock(IssueTypeScreenSchemeManager.class);
        final Predicate<Field> mockPredicate = new Predicate<Field>()
        {
            public boolean evaluate(final Field input)
            {
                return false;
            }
        };

        expect(manager.getFieldScreenScheme(mockIssue)).andReturn(mockScreenScheme);
        expect(mockScreenScheme.getFieldScreen(IssueOperations.VIEW_ISSUE_OPERATION)).andReturn(mockFieldScreen);

        mocksControl.replay();

        final StandardFieldScreenRendererFactory factory = new StandardFieldScreenRendererFactory(null, null, manager, null, fieldScreenTabRendererFactory)
        {
            @Override
            FieldScreenRenderer createFieldScreenRenderer(final Issue issue, final FieldScreen fieldScreen, final IssueOperation operation, final Predicate<? super Field> condition)
            {
                assertEquals(mockIssue, issue);
                assertSame(mockFieldScreen, fieldScreen);
                assertEquals(IssueOperations.VIEW_ISSUE_OPERATION, operation);
                assertSame(mockPredicate, condition);

                return mockRenderer;
            }
        };

        final FieldScreenRenderer actionRender = factory.createFieldScreenRenderer(mockIssue, IssueOperations.VIEW_ISSUE_OPERATION, mockPredicate);
        assertSame(mockRenderer, actionRender);

        mocksControl.verify();
    }

    @Test
    public void testCreateFieldScreenRendererAction() throws Exception
    {
        final IMocksControl mocksControl = createControl();

        final FieldScreenRenderer mockRenderer = new MockFieldScreenRenderer();
        final FieldScreen mockFieldScreen = new MockFieldScreen();
        final Issue mockIssue = new MockIssue(5);
        final ActionDescriptor mockDescriptor = mocksControl.createMock(ActionDescriptor.class);

        mocksControl.replay();

        final StandardFieldScreenRendererFactory factory = new StandardFieldScreenRendererFactory(null, null, null, null, fieldScreenTabRendererFactory)
        {
            @Override
            FieldScreenRenderer createFieldScreenRenderer(final Issue issue, final FieldScreen fieldScreen, final IssueOperation operation, final Predicate<? super Field> condition)
            {
                assertEquals(mockIssue, issue);
                assertSame(mockFieldScreen, fieldScreen);
                assertNull(operation);
                assertEquals(Predicates.<Object>truePredicate(), condition);

                return mockRenderer;
            }

            @Override
            FieldScreen getScreenFromAction(final ActionDescriptor descriptor)
            {
                assertSame(mockDescriptor, descriptor);
                return mockFieldScreen;
            }
        };

        final FieldScreenRenderer screenRenderer = factory.createFieldScreenRenderer(mockIssue, mockDescriptor);
        assertSame(mockRenderer, screenRenderer);
        mocksControl.verify();
    }

    @Test
    public void testCreateScreenRendererIssue() throws Exception
    {
        final FieldScreenRenderer mockRenderer = new MockFieldScreenRenderer();
        final Issue mockIssue = new MockIssue(5);

        final StandardFieldScreenRendererFactory factory = new StandardFieldScreenRendererFactory(null, null, null, null, fieldScreenTabRendererFactory)
        {
            @Override
            FieldScreenRenderer createFieldScreenRenderer(final Issue issue, final Collection<FieldScreenTab> tabs, final IssueOperation operation, final Predicate<? super Field> condition)
            {
                assertSame(mockIssue, issue);
                assertTrue(tabs.isEmpty());
                assertNull(operation);
                assertEquals(Predicates.<Object>truePredicate(), condition);

                return mockRenderer;
            }
        };

        final FieldScreenRenderer screenRenderer = factory.createFieldScreenRenderer(mockIssue);
        assertSame(mockRenderer, screenRenderer);
    }

    @Test
    public void testCreateScreenRendererFieldScreenNull() throws Exception
    {
        final FieldScreenRenderer mockRenderer = new MockFieldScreenRenderer();
        final Issue mockIssue = new MockIssue(5);

        final StandardFieldScreenRendererFactory factory = new StandardFieldScreenRendererFactory(null, null, null, null, fieldScreenTabRendererFactory)
        {
            @Override
            FieldScreenRenderer createFieldScreenRenderer(final Issue issue, final Collection<FieldScreenTab> tabs, final IssueOperation operation, final Predicate<? super Field> condition)
            {
                assertSame(mockIssue, issue);
                assertTrue(tabs.isEmpty());
                assertEquals(IssueOperations.EDIT_ISSUE_OPERATION, operation);
                assertEquals(Predicates.falsePredicate(), condition);

                return mockRenderer;
            }
        };

        final FieldScreenRenderer screenRenderer = factory.createFieldScreenRenderer(mockIssue, (FieldScreen) null, IssueOperations.EDIT_ISSUE_OPERATION, Predicates.falsePredicate());
        assertSame(mockRenderer, screenRenderer);
    }

    @Test
    public void testCreateScreenRendererFieldScreenNotNull() throws Exception
    {
        final FieldScreen mockScreen = new MockFieldScreen();
        mockScreen.addTab("test");
        mockScreen.addTab("test2");

        final FieldScreenRenderer mockRenderer = new MockFieldScreenRenderer();
        final Issue mockIssue = new MockIssue(5);

        final StandardFieldScreenRendererFactory factory = new StandardFieldScreenRendererFactory(null, null, null, null, fieldScreenTabRendererFactory)
        {
            @Override
            FieldScreenRenderer createFieldScreenRenderer(final Issue issue, final Collection<FieldScreenTab> tabs, final IssueOperation operation, final Predicate<? super Field> condition)
            {
                assertSame(mockIssue, issue);
                assertEquals(mockScreen.getTabs(), tabs);
                assertEquals(IssueOperations.EDIT_ISSUE_OPERATION, operation);
                assertEquals(Predicates.falsePredicate(), condition);

                return mockRenderer;
            }
        };

        final FieldScreenRenderer screenRenderer = factory.createFieldScreenRenderer(mockIssue, mockScreen, IssueOperations.EDIT_ISSUE_OPERATION, Predicates.falsePredicate());
        assertSame(mockRenderer, screenRenderer);
    }

    @Test
    public void testCreateFieldScreenRendererTabsNoTabs() throws Exception
    {
        final MockIssue mi = new MockIssue(4);
        final MockFieldManager mfm = new MockFieldManager();
        final MockFieldLayoutManager mflm = new MockFieldLayoutManager();
        final MockFieldLayout mfl = mflm.addLayoutItem(mi);

        final StandardFieldScreenRendererFactory factory = new StandardFieldScreenRendererFactory(mfm, mflm, null, null, fieldScreenTabRendererFactory);
        final FieldScreenRenderer screenRenderer = factory.createFieldScreenRenderer(mi, Collections.<FieldScreenTab>emptyList(),
                IssueOperations.CREATE_ISSUE_OPERATION, Predicates.falsePredicate());

        assertTrue(screenRenderer.getFieldScreenRenderTabs().isEmpty());
        assertSame(mfl, screenRenderer.getFieldLayout());
    }

    @Test
    public void testCreateFieldScreenRendererTabsStorageException() throws Exception
    {
        final IMocksControl control = createControl();

        final MockIssue issue = new MockIssue(56);
        final FieldLayoutManager layoutManager = control.createMock(FieldLayoutManager.class);
        //noinspection ThrowableInstanceNeverThrown
        expect(layoutManager.getFieldLayout(issue)).andThrow(new DataAccessException("blah"));

        final StandardFieldScreenRendererFactory factory = new StandardFieldScreenRendererFactory(new MockFieldManager(),
                layoutManager, null, null, fieldScreenTabRendererFactory);

        control.replay();

        try
        {
            factory.createFieldScreenRenderer(issue, Collections.<FieldScreenTab>emptyList(),
                IssueOperations.CREATE_ISSUE_OPERATION, Predicates.falsePredicate());
            fail("Should have thrown the exception.");

        }
        catch (DataAccessException expected)
        {
            //good.
        }

        control.verify();
    }

    @Test
    public void testCreateFieldScreenRendererFields() throws Exception
    {
        final List<String> names = Arrays.asList("a", "b", "c");
        final FieldScreenRenderer mockRenderer = new MockFieldScreenRenderer();
        final Issue mockIssue = new MockIssue(5);

        final StandardFieldScreenRendererFactory factory = new StandardFieldScreenRendererFactory(null, null, null, null, fieldScreenTabRendererFactory)
        {
            @Override
            FieldScreenRenderer createFieldScreenRenderer(final Issue issue, final Collection<FieldScreenTab> tabs, final IssueOperation operation, final Predicate<? super Field> condition)
            {
                assertSame(mockIssue, issue);
                assertEquals(IssueOperations.EDIT_ISSUE_OPERATION, operation);
                assertEquals(Predicates.<Object>truePredicate(), condition);

                assertEquals(1, tabs.size());
                final FieldScreenTab tab = tabs.iterator().next();

                int count = 0;
                final Iterator<String> iterator = names.iterator();
                for (final FieldScreenLayoutItem item : tab.getFieldScreenLayoutItems())
                {
                    assertEquals(iterator.next(), item.getFieldId());
                    assertEquals(count++, item.getPosition());
                    assertSame(tab, item.getFieldScreenTab());
                }

                return mockRenderer;
            }
        };

        final FieldScreenRenderer screenRenderer = factory.createFieldScreenRenderer(names, mockIssue, IssueOperations.EDIT_ISSUE_OPERATION);
        assertSame(mockRenderer, screenRenderer);

    }

    @Test
    public void shouldCreateTabRendererForEachNonEmptyTab()
    {
        //given
        final Predicate<? super Field> condition = mock(Predicate.class);
        final IssueOperation operation = IssueOperations.VIEW_ISSUE_OPERATION;
        final Issue issue = mock(Issue.class);
        final FieldLayoutManager fieldLayoutManager = mock(FieldLayoutManager.class);
        final Collection<FieldScreenTab> tabs = Lists.newArrayList(
                mock(FieldScreenTab.class),
                mock(FieldScreenTab.class)
        );

        final FieldScreenRenderTabImpl tabRender = mockRenderTabWithItems();
        when(fieldScreenTabRendererFactory.createTabRender(any(IssueTabRendererDto.class)))
                .thenReturn(tabRender);

        //when
        final StandardFieldScreenRendererFactory factory = createStandardFieldScreenRendererFactory(null, fieldLayoutManager, null, null, fieldScreenTabRendererFactory);
        final FieldScreenRenderer fieldScreenRenderer = factory.createFieldScreenRenderer(issue, tabs, operation, condition);

        //then
        assertThat(fieldScreenRenderer.getFieldScreenRenderTabs().size(), equalTo(tabs.size()));
    }

    @Test
    public void shouldIgnoreEmptyTabs()
    {
        //given
        final Predicate<? super Field> condition = mock(Predicate.class);
        final IssueOperation operation = IssueOperations.VIEW_ISSUE_OPERATION;
        final Issue issue = mock(Issue.class);
        final FieldLayoutManager fieldLayoutManager = mock(FieldLayoutManager.class);
        final Collection<FieldScreenTab> tabs = Lists.newArrayList(
                mock(FieldScreenTab.class),
                mock(FieldScreenTab.class)
        );

        final FieldScreenRenderTabImpl tabRender = mockRenderTabsWithoutItems();
        when(fieldScreenTabRendererFactory.createTabRender(any(IssueTabRendererDto.class)))
                .thenReturn(tabRender);

        //when
        final StandardFieldScreenRendererFactory factory = createStandardFieldScreenRendererFactory(null, fieldLayoutManager, null, null, fieldScreenTabRendererFactory);
        final FieldScreenRenderer fieldScreenRenderer = factory.createFieldScreenRenderer(issue, tabs, operation, condition);

        //then
        assertThat(fieldScreenRenderer.getFieldScreenRenderTabs().size(), equalTo(0));
    }

    private FieldScreenRenderTabImpl mockRenderTabsWithoutItems()
    {
        return mockRenderTab(false);
    }

    private FieldScreenRenderTabImpl mockRenderTabWithItems()
    {
        return mockRenderTab(true);
    }

    private FieldScreenRenderTabImpl mockRenderTab(boolean withItems)
    {
        FieldScreenRenderTabImpl tabRender = mock(FieldScreenRenderTabImpl.class);

        final List<FieldScreenRenderLayoutItem> items = Lists.newArrayList();
        if(withItems)
        {
            items.add(mock(FieldScreenRenderLayoutItem.class));
        }

        when(tabRender.getFieldScreenRenderLayoutItems())
                .thenReturn(items);

        return tabRender;
    }

    private StandardFieldScreenRendererFactory createStandardFieldScreenRendererFactory(final FieldManager fieldManager, final FieldLayoutManager fieldLayoutManager,
            final IssueTypeScreenSchemeManager issueTypeScreenSchemeManager, final FieldScreenManager fieldScreenManager,
            final FieldScreenTabRendererFactoryImpl fieldScreenTabRendererFactory)
    {
        return new StandardFieldScreenRendererFactory(fieldManager, fieldLayoutManager, issueTypeScreenSchemeManager, fieldScreenManager, fieldScreenTabRendererFactory);
    }
}
