package com.atlassian.jira.issue.search.searchers.renderer;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.util.DateSearcherConfig;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.MockApplicationProperties;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.velocity.DefaultVelocityRequestContextFactory;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.jira.web.action.util.CalendarLanguageUtil;
import com.atlassian.query.Query;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;

import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test for {@link TestSystemDateSearchRenderer}.
 *
 * @since v4.0
 */
public class TestSystemDateSearchRenderer extends MockControllerTestCase
{
    private static final String FIELD_NAME = "duedate";

    @Test
    public void testIsShownTrueWhenFieldCares()
    {
        SearchContext context = mockController.getMock(SearchContext.class);

        final MockControl mockFieldVisibilityManagerControl = MockClassControl.createControl(FieldVisibilityManager.class);
        final FieldVisibilityManager mockFieldVisibilityManager = (FieldVisibilityManager) mockFieldVisibilityManagerControl.getMock();

        mockFieldVisibilityManager.isFieldHiddenInAllSchemes(FIELD_NAME, context, null);
        mockFieldVisibilityManagerControl.setReturnValue(false);
        mockFieldVisibilityManagerControl.replay();

        final DateSearchRenderer dateSearchRenderer = createRenderer(FIELD_NAME, mockFieldVisibilityManager);
        assertTrue(dateSearchRenderer.isShown(null, context));

        mockFieldVisibilityManagerControl.verify();
    }

    @Test
    public void testIsShownFalseWhenFieldCares()
    {
        SearchContext context = mockController.getMock(SearchContext.class);

        final MockControl mockFieldVisibilityManagerControl = MockClassControl.createControl(FieldVisibilityManager.class);
        final FieldVisibilityManager mockFieldVisibilityManager = (FieldVisibilityManager) mockFieldVisibilityManagerControl.getMock();

        mockFieldVisibilityManager.isFieldHiddenInAllSchemes(FIELD_NAME, context, null);
        mockFieldVisibilityManagerControl.setReturnValue(true);
        mockFieldVisibilityManagerControl.replay();

        final DateSearchRenderer dateSearchRenderer = createRenderer(FIELD_NAME, mockFieldVisibilityManager);
        assertFalse(dateSearchRenderer.isShown(null, context));

        mockFieldVisibilityManagerControl.verify();
    }

    @Test
    public void testIsRelevantForSearchRequest()
    {
        final MockControl mockFieldVisibilityManagerControl = MockClassControl.createControl(FieldVisibilityManager.class);
        final FieldVisibilityManager mockFieldVisibilityManager = (FieldVisibilityManager) mockFieldVisibilityManagerControl.getMock();
        mockFieldVisibilityManagerControl.replay();

        final DateSearchRenderer dateSearchRenderer = createRenderer(FIELD_NAME, mockFieldVisibilityManager);

        Query query = new QueryImpl(new TerminalClauseImpl(FIELD_NAME + "SoNot", Operator.LESS_THAN_EQUALS, "test"));

        assertFalse(dateSearchRenderer.isRelevantForQuery((User)null, query));

        query = new QueryImpl(new AndClause(
                new TerminalClauseImpl(FIELD_NAME + "SoNot", Operator.LESS_THAN_EQUALS, "test"),
                new TerminalClauseImpl(FIELD_NAME, Operator.LESS_THAN_EQUALS, "test")));

        assertTrue(dateSearchRenderer.isRelevantForQuery((User)null, query));
    }

    private DateSearchRenderer createRenderer(final String id, final FieldVisibilityManager fieldVisibilityManager)
    {
        ApplicationProperties renderProperties = new MockApplicationProperties();

        final VelocityTemplatingEngine mockTemplatingEngine = mockController.getMock(VelocityTemplatingEngine.class);
        final CalendarLanguageUtil calendarUtils = mockController.getMock(CalendarLanguageUtil.class);

        mockController.replay();

        return new DateSearchRenderer(SystemSearchConstants.forDueDate(), new DateSearcherConfig(id, SystemSearchConstants.forDueDate().getJqlClauseNames(),
                SystemSearchConstants.forDueDate().getJqlClauseNames().getPrimaryName()), "mykey",
                new DefaultVelocityRequestContextFactory(renderProperties), renderProperties, mockTemplatingEngine,
                calendarUtils, fieldVisibilityManager);
    }
}
