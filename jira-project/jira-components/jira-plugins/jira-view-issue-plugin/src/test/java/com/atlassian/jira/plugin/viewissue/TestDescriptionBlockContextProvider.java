package com.atlassian.jira.plugin.viewissue;

import java.util.Map;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderTab;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderer;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;

import com.google.common.collect.Maps;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.jira.issue.operation.IssueOperations.EDIT_ISSUE_OPERATION;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class TestDescriptionBlockContextProvider
{
    @Mock
    private FieldLayoutManager fieldLayoutManager;
    @Mock
    private RendererManager rendererManager;
    @Mock
    private FieldScreenRendererFactory fieldScreenRendererFactory;
    @Mock
    private IssueManager issueManager;

    @Mock
    private FieldLayout fieldLayout;
    @Mock
    private FieldScreenRenderer fieldScreenRenderer;
    @Mock
    private Issue issue;
    @Mock
    private User user;

    private DescriptionBlockContextProvider contextProvider;

    @Before
    public void setUp()
    {
        contextProvider = new DescriptionBlockContextProvider(fieldLayoutManager,
                rendererManager, fieldScreenRendererFactory, issueManager);

        when(fieldLayoutManager.getFieldLayout(issue)).thenReturn(fieldLayout);
        when(fieldLayout.getFieldLayoutItem(IssueFieldConstants.DESCRIPTION)).thenReturn(null);

        when(issueManager.isEditable(issue, user)).thenReturn(true);
        when(fieldScreenRendererFactory.getFieldScreenRenderer(issue, EDIT_ISSUE_OPERATION)).thenReturn(fieldScreenRenderer);

    }

    @Test
    public void testDescriptionNotEditableWhenFieldNotOnScreen()
    {
        //simulate that the field is not on the edit screen
        when(fieldScreenRenderer.getFieldScreenRenderTabPosition(IssueFieldConstants.DESCRIPTION)).thenReturn(null);

        final Map<String, Object> initialContext = Maps.newHashMap();
        initialContext.put("issue", issue);
        initialContext.put("user", user);

        final Map<String, Object> context = contextProvider.getContextMap(initialContext);

        assertFalse("Simulating that description is empty, but it wasn't so.", context.containsKey("descriptionHtml"));
        assertFalse("The description field is not on the screen, so it should not be editable, but it was.", (Boolean) context.get("isEditable"));
    }

    @Test
    public void testDescriptionEditableWhenFieldOnScreenAndEmpty()
    {
        final FieldScreenRenderTab fieldScreenRenderTab = Mockito.mock(FieldScreenRenderTab.class);
        //simulate that the field is on the edit screen
        when(fieldScreenRenderer.getFieldScreenRenderTabPosition(IssueFieldConstants.DESCRIPTION)).thenReturn(fieldScreenRenderTab);

        final Map<String, Object> initialContext = Maps.newHashMap();
        initialContext.put("issue", issue);
        initialContext.put("user", user);

        final Map<String, Object> context = contextProvider.getContextMap(initialContext);

        assertFalse("Simulating that description is empty, but it wasn't so.", context.containsKey("descriptionHtml"));
        assertTrue("The description field is not on the screen, so it should not be editable, but it was.", (Boolean) context.get("isEditable"));
    }
}
