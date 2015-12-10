package com.atlassian.jira.issue.search.searchers.renderer;

import java.util.Collection;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.priority.Priority;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operator.Operator;

import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestIssueConstantsSearchRenderer
{
    @Test
    public void testIsShown() throws Exception
    {
        final MockControl mockFieldVisibilityManagerControl = MockClassControl.createStrictControl(FieldVisibilityManager.class);
        final FieldVisibilityManager mockFieldVisibilityManager = (FieldVisibilityManager) mockFieldVisibilityManagerControl.getMock();
        mockFieldVisibilityManager.isFieldHiddenInAllSchemes("priority", null, null);
        mockFieldVisibilityManagerControl.setReturnValue(true);
        mockFieldVisibilityManagerControl.replay();

        final IssueConstantsSearchRenderer<Priority> searchRenderer = new IssueConstantsSearchRenderer<Priority>(SystemSearchConstants.forPriority(), "test", null, null, null, null, mockFieldVisibilityManager)
        {
            public Collection<Priority> getSelectListOptions(final SearchContext searchContext)
            {
                return null;
            }
        };

        assertFalse(searchRenderer.isShown(null, null));
        mockFieldVisibilityManagerControl.verify();
    }

    @Test
    public void testIsRelevantForSearchRequestTrue() throws Exception
    {
        final IssueConstantsSearchRenderer<Priority> searchRenderer = new IssueConstantsSearchRenderer<Priority>(SystemSearchConstants.forPriority(), "testName", null, null, null, null, null)
        {
            public Collection<Priority> getSelectListOptions(final SearchContext searchContext)
            {
                return null;
            }
        };

        final MultiValueOperand operand = new MultiValueOperand("val1", "val2");
        final QueryImpl query = new QueryImpl(new TerminalClauseImpl("priority", Operator.EQUALS, operand));

        final MockControl mockSearchRequestControl = MockClassControl.createControl(SearchRequest.class);
        mockSearchRequestControl.replay();

        assertTrue(searchRenderer.isRelevantForQuery((User)null, query));

        mockSearchRequestControl.verify();
    }

    @Test
    public void testIsRelevantForSearchRequestFalse() throws Exception
    {
        final IssueConstantsSearchRenderer<Priority> searchRenderer = new IssueConstantsSearchRenderer<Priority>(SystemSearchConstants.forPriority(), "teste", null, null, null, null, null)
        {
            public Collection<Priority> getSelectListOptions(final SearchContext searchContext)
            {
                return null;
            }
        };

        final MultiValueOperand operand = new MultiValueOperand("val1", "val2");
        final QueryImpl query = new QueryImpl(new TerminalClauseImpl("someOtherId", Operator.EQUALS, operand));

        final MockControl mockSearchRequestControl = MockClassControl.createControl(SearchRequest.class);
        mockSearchRequestControl.replay();

        assertFalse(searchRenderer.isRelevantForQuery((User) null, query));

        mockSearchRequestControl.verify();
    }
    
}
