package com.atlassian.jira.issue.fields.screen;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.util.FieldPredicates;
import com.atlassian.jira.issue.operation.IssueOperations;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.util.Predicate;
import com.atlassian.jira.util.Predicates;

import com.opensymphony.workflow.loader.ActionDescriptor;

import org.easymock.classextension.IMocksControl;
import org.junit.Test;

import static org.easymock.classextension.EasyMock.createControl;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.expect;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;
import static org.junit.Assert.assertSame;

/**
 * Test for {@link com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactoryImpl}.
 *
 * @since v4.1
 */
public class TestFieldScreenRendererFactoryImpl
{
    @Test
    public void testGetFieldScreenRendererWithAll() throws Exception
    {
        final MockIssue mockIssue = new MockIssue(1);
        final MockFieldScreenRenderer screenRenderer = new MockFieldScreenRenderer();

        final StandardFieldScreenRendererFactory stdFactory = createMock(StandardFieldScreenRendererFactory.class);
        expect(stdFactory.createFieldScreenRenderer(mockIssue, IssueOperations.VIEW_ISSUE_OPERATION, Predicates.truePredicate())).andReturn(screenRenderer);
        replay(stdFactory);

        FieldScreenRendererFactoryImpl factory = new FieldScreenRendererFactoryImpl(null, stdFactory);

        final FieldScreenRenderer actualRenderer = factory.getFieldScreenRenderer(mockIssue, IssueOperations.VIEW_ISSUE_OPERATION);
        assertSame(screenRenderer, actualRenderer);

        verify(stdFactory);
    }

    @Test
    public void testGetFieldScreenRendererWithAllLegacy() throws Exception
    {
        final MockIssue mockIssue = new MockIssue(1);
        final MockFieldScreenRenderer screenRenderer = new MockFieldScreenRenderer();

        final StandardFieldScreenRendererFactory stdFactory = createMock(StandardFieldScreenRendererFactory.class);
        expect(stdFactory.createFieldScreenRenderer(mockIssue, IssueOperations.VIEW_ISSUE_OPERATION, Predicates.truePredicate())).andReturn(screenRenderer);
        replay(stdFactory);

        FieldScreenRendererFactoryImpl factory = new FieldScreenRendererFactoryImpl(null, stdFactory);

        final FieldScreenRenderer actualRenderer = factory.getFieldScreenRenderer(null, mockIssue, IssueOperations.VIEW_ISSUE_OPERATION, false);
        assertSame(screenRenderer, actualRenderer);

        verify(stdFactory);
    }

    @Test
    public void testGetFieldScreenRendererWithCustomOnly() throws Exception
    {
        final MockIssue mockIssue = new MockIssue(1);
        final MockFieldScreenRenderer screenRenderer = new MockFieldScreenRenderer();

        final StandardFieldScreenRendererFactory stdFactory = createMock(StandardFieldScreenRendererFactory.class);
        expect(stdFactory.createFieldScreenRenderer(mockIssue, IssueOperations.VIEW_ISSUE_OPERATION, FieldPredicates.isCustomField())).andReturn(screenRenderer);
        replay(stdFactory);

        FieldScreenRendererFactoryImpl factory = new FieldScreenRendererFactoryImpl(null, stdFactory);

        final FieldScreenRenderer actualRenderer = factory.getFieldScreenRenderer(mockIssue, IssueOperations.VIEW_ISSUE_OPERATION, FieldPredicates.isCustomField());
        assertSame(screenRenderer, actualRenderer);

        verify(stdFactory);
    }

    @Test
    public void testGetFieldScreenRendererWithCustomOnlyLegacy() throws Exception
    {
        final MockIssue mockIssue = new MockIssue(1);
        final MockFieldScreenRenderer screenRenderer = new MockFieldScreenRenderer();

        final StandardFieldScreenRendererFactory stdFactory = createMock(StandardFieldScreenRendererFactory.class);
        expect(stdFactory.createFieldScreenRenderer(mockIssue, IssueOperations.VIEW_ISSUE_OPERATION, FieldPredicates.isCustomField())).andReturn(screenRenderer);
        replay(stdFactory);

        FieldScreenRendererFactoryImpl factory = new FieldScreenRendererFactoryImpl(null, stdFactory);

        final FieldScreenRenderer actualRenderer = factory.getFieldScreenRenderer(null, mockIssue, IssueOperations.VIEW_ISSUE_OPERATION, true);
        assertSame(screenRenderer, actualRenderer);

        verify(stdFactory);
    }

    @Test
    public void testGetFieldScreenRendererOperation() throws Exception
    {
        final MockIssue mockIssue = new MockIssue(1);
        final MockFieldScreenRenderer screenRenderer = new MockFieldScreenRenderer();

        final Predicate<Field> pred = new Predicate<Field>()
        {
            public boolean evaluate(final Field input)
            {
                return false;
            }
        };

        final StandardFieldScreenRendererFactory stdFactory = createMock(StandardFieldScreenRendererFactory.class);
        expect(stdFactory.createFieldScreenRenderer(mockIssue, IssueOperations.VIEW_ISSUE_OPERATION, pred)).andReturn(screenRenderer);
        replay(stdFactory);

        FieldScreenRendererFactoryImpl factory = new FieldScreenRendererFactoryImpl(null, stdFactory);

        final FieldScreenRenderer actualRenderer = factory.getFieldScreenRenderer(mockIssue, IssueOperations.VIEW_ISSUE_OPERATION, pred);
        assertSame(screenRenderer, actualRenderer);

        verify(stdFactory);
    }

    @Test
    public void testGetFieldScreenRendererActionDescriptor() throws Exception
    {
        final IMocksControl control = createControl();

        final MockIssue mockIssue = new MockIssue(1);
        final MockFieldScreenRenderer screenRenderer = new MockFieldScreenRenderer();

        final ActionDescriptor desc = control.createMock(ActionDescriptor.class);

        final StandardFieldScreenRendererFactory stdFactory = control.createMock(StandardFieldScreenRendererFactory.class);
        expect(stdFactory.createFieldScreenRenderer(mockIssue, desc)).andReturn(screenRenderer);
        control.replay();

        final FieldScreenRendererFactoryImpl factory = new FieldScreenRendererFactoryImpl(null, stdFactory);

        final FieldScreenRenderer actualRenderer = factory.getFieldScreenRenderer(mockIssue, desc);
        assertSame(screenRenderer, actualRenderer);

        control.verify();
    }

    @Test
    public void testGetFieldScreenRendererForIssue() throws Exception
    {
        final IMocksControl control = createControl();

        final MockIssue mockIssue = new MockIssue(1);
        final MockFieldScreenRenderer screenRenderer = new MockFieldScreenRenderer();

        final StandardFieldScreenRendererFactory stdFactory = control.createMock(StandardFieldScreenRendererFactory.class);
        expect(stdFactory.createFieldScreenRenderer(mockIssue)).andReturn(screenRenderer);
        control.replay();

        final FieldScreenRendererFactoryImpl factory = new FieldScreenRendererFactoryImpl(null, stdFactory);

        final FieldScreenRenderer actualRenderer = factory.getFieldScreenRenderer(mockIssue);
        assertSame(screenRenderer, actualRenderer);

        control.verify();
    }

    @Test
    public void testGetFieldScreenRendererForFields() throws Exception
    {
        final IMocksControl control = createControl();

        final MockIssue mockIssue = new MockIssue(1);
        final MockFieldScreenRenderer screenRenderer = new MockFieldScreenRenderer();
        final List<String> fieldIds = Arrays.asList("one", "two", "three");

        final StandardFieldScreenRendererFactory stdFactory = control.createMock(StandardFieldScreenRendererFactory.class);
        expect(stdFactory.createFieldScreenRenderer(fieldIds, mockIssue, IssueOperations.EDIT_ISSUE_OPERATION)).andReturn(screenRenderer);
        control.replay();

        final FieldScreenRendererFactoryImpl factory = new FieldScreenRendererFactoryImpl(null, stdFactory);

        final FieldScreenRenderer actualRenderer = factory.getFieldScreenRenderer(fieldIds, null, mockIssue, IssueOperations.EDIT_ISSUE_OPERATION);
        assertSame(screenRenderer, actualRenderer);

        control.verify();
    }

    @Test
    public void testGetFieldScreenRendererForBulk() throws Exception
    {
        final IMocksControl control = createControl();

        final List<Issue> issues = Arrays.<Issue>asList(new MockIssue(1), new MockIssue(2));

        final ActionDescriptor action = control.createMock(ActionDescriptor.class);
        final BulkFieldScreenRendererImpl renderer = new BulkFieldScreenRendererImpl(Collections.<FieldScreenRenderTab>emptyList());

        final BulkFieldScreenRendererFactory bulkFieldScreenRendererFactory = control.createMock(BulkFieldScreenRendererFactory.class);
        expect(bulkFieldScreenRendererFactory.createRenderer(issues, action)).andReturn(renderer);
        control.replay();

        final FieldScreenRendererFactoryImpl factory = new FieldScreenRendererFactoryImpl(bulkFieldScreenRendererFactory, null);

        final FieldScreenRenderer actualRenderer = factory.getFieldScreenRenderer(issues, action);
        assertSame(renderer, actualRenderer);

        control.verify();
    }
}
