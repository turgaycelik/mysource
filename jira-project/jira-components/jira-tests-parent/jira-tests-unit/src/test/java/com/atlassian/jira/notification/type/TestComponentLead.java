package com.atlassian.jira.notification.type;

import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.mock.security.MockSimpleAuthenticationContext;
import com.atlassian.jira.notification.type.enterprise.ComponentLead;

import org.junit.Test;

import static com.google.common.collect.Maps.newHashMap;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests to test the ComponentLead notification type.
 */
public class TestComponentLead extends AbstractNotificationTestCase
{
    // Call the getRecipients method of ComponentLead with a component that has no lead and make
    // sure that no exception is thrown JRA-9993 and that the list is empty
    @Test
    public void testGetRecipientsWithNoLeadSetOnComponent()
    {
        final ProjectComponent component = mock(ProjectComponent.class);
        final IssueEvent event = new IssueEvent(issue, newHashMap(), null, null);

        issue.setComponentObjects(asList(component));

        final ComponentLead componentLead = new ComponentLead(new MockSimpleAuthenticationContext(null));
        assertEquals(0, componentLead.getRecipients(event, null).size());
    }

    @Test
    public void testGetRecipientsWithNoUserForComponentLead()
    {
        final ProjectComponent component = mock(ProjectComponent.class);
        final IssueEvent event = new IssueEvent(issue, newHashMap(), null, null);

        issue.setComponentObjects(asList(component));
        when(component.getLead()).thenReturn("No such user");

        final ComponentLead componentLead = new ComponentLead(new MockSimpleAuthenticationContext(null));
        checkRecipients(componentLead.getRecipients(event, null));
    }

    @Test
    public void testGetRecipientsWithValidUserForComponentLead()
    {
        final ProjectComponent component = mock(ProjectComponent.class);
        final IssueEvent event = new IssueEvent(issue, newHashMap(), null, null);

        issue.setComponentObjects(asList(component));
        when(component.getLead()).thenReturn(user.getKey());

        final ComponentLead componentLead = new ComponentLead(new MockSimpleAuthenticationContext(null));
        checkRecipients(componentLead.getRecipients(event, null), user);
    }
}
