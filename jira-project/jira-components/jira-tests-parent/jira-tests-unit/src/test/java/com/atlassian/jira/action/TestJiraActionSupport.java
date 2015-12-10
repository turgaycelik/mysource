package com.atlassian.jira.action;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.ofbiz.core.entity.DelegatorInterface;
import org.ofbiz.core.entity.GenericDelegator;

import webwork.action.Action;
import webwork.action.ActionSupport;
import webwork.dispatcher.ActionResult;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 1 Unit test of {@link JiraActionSupport}.
 */
public class TestJiraActionSupport
{
    @Rule
    public RuleChain mockitoMocksInContainer = MockitoMocksInContainer.forTest(this);

    @Mock
    @AvailableInContainer
    private I18nHelper i18nHelper;

    @Mock
    @AvailableInContainer
    private JiraAuthenticationContext jiraAuthenticationContext;

    @Mock
    @AvailableInContainer (interfaceClass = DelegatorInterface.class)
    private GenericDelegator mockDelegatorInterface;

    private JiraActionSupport support;

    private static class MockJiraActionSupport extends JiraActionSupport
    {
        @Override
        public User getLoggedInUser()
        {
            throw new UnsupportedOperationException();
        }
    }

    @Before
    public void setUp() throws Exception
    {
        when(jiraAuthenticationContext.getI18nHelper()).thenReturn(i18nHelper);

        support = new MockJiraActionSupport();
    }

    @Test
    public void gettingResourceBundleShouldDelegateToI18nHelperFromContainer()
    {
        // given:
        final ResourceBundle mockResourceBundle = mock(ResourceBundle.class);
        when(i18nHelper.getResourceBundle()).thenReturn(mockResourceBundle);

        // when:
        final ResourceBundle resourceBundle = support.getResourceBundle();

        // then:
        assertSame(mockResourceBundle, resourceBundle);
    }


    @Test
    public void defaultActionNameShouldBeItsConcreteClassName()
    {
        assertEquals("TestJiraActionSupport$MockJiraActionSupport", support.getActionName());
    }

    @Test
    public void addingErrorsShouldChangeResult()
    {
        support = new MockJiraActionSupport();
        assertEquals(Action.SUCCESS, support.getResult());

        support = new MockJiraActionSupport();
        support.addErrorMessage("An error has occurred");
        assertEquals(Action.ERROR, support.getResult());

        support = new MockJiraActionSupport();
        support.addError("foo", "An error has occurred");
        assertEquals(Action.ERROR, support.getResult());
    }


    @Test
    public void errorMessagesShouldBeMaintainedInOrder()
    {
        // given:
        assertEquals(0, support.getErrorMessages().size());
        final List<String> newErrorMessages = ImmutableList.of("foo", "bar");

        // when:
        support.addErrorMessages(newErrorMessages);

        // then:
        Assert.assertEquals(support.getErrorMessages(), newErrorMessages);
    }

    @Test
    public void testAddErrors()
    {
        support = new MockJiraActionSupport();
        assertEquals(0, support.getErrors().size());

        final Map<String, String> errors = ImmutableMap.<String, String>builder()
                .put("foo", "bar").put("baz", "bat").build();

        support.addErrors(errors);
        assertEquals(support.getErrors(), errors);
    }

    @Test
    public void testGetDelegator() throws Exception
    {
        // given:
        when(mockDelegatorInterface.getDelegatorName()).thenReturn("test successful");

        // then:
        assertNotNull(support.getDelegator());
        assertEquals("test successful", support.getDelegator().getDelegatorName());
    }

    @Test
    public void testAddErrorMessagesFromAResult()
    {
        ActionResult result = new ActionResult(Action.SUCCESS, null, null, null);

        support = new MockJiraActionSupport();
        support.addErrorMessages(result);

        assertEquals(0, support.getErrorMessages().size());

        // now create a dummy action with an error message and check it gets added
        final ActionSupport mockAction = mock(ActionSupport.class);
        when(mockAction.getErrorMessages()).thenReturn(Collections.singletonList("an error message"));

        result = new ActionResult(Action.ERROR, null, Collections.singletonList(mockAction), null);
        support.addErrorMessages(result);

        assertEquals(1, support.getErrorMessages().size());
        assertEquals("an error message", Iterables.getFirst(support.getErrorMessages(), null));
    }

    @Test
    public void testRemoveKeyOrAddError()
    {
        final Map params = new HashMap();
        when(i18nHelper.getText(Mockito.anyString())).thenAnswer(new Answer<Object>()
        {
            @Override
            public Object answer(final InvocationOnMock invocationOnMock) throws Throwable
            {
                return invocationOnMock.getArguments()[0];
            }
        });

        support = new MockJiraActionSupport();
        support.removeKeyOrAddError(params, "absentparam", "the.error");
        assertTrue("parameter map is not supposed to change", params.isEmpty());
        assertEquals(1, support.getErrorMessages().size());
        assertEquals("the.error", Iterables.getFirst(support.getErrorMessages(), null));

        support = new MockJiraActionSupport();
        final Map<String, String> existing = Maps.newHashMap();
        existing.put("count", "5");
        support.removeKeyOrAddError(existing, "count", "admin.errors.workflows.cannot.find.count.bogus");
        assertTrue("The only existing parameter should be removed.", existing.isEmpty());
        assertEquals(0, support.getErrorMessages().size());

        support.removeKeyOrAddError(existing, "count", "admin.errors.workflows.cannot.find.count.bogus");
        assertTrue("The only existing parameter should be removed.", existing.isEmpty());
        assertEquals(1, support.getErrorMessages().size());
        assertEquals("admin.errors.workflows.cannot.find.count.bogus", Iterables.getFirst(support.getErrorMessages(), null));
    }
}
