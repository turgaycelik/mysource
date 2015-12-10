package com.atlassian.jira.issue.tabpanels;

import java.util.Locale;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.worklog.Worklog;
import com.atlassian.jira.plugin.issuetabpanel.IssueTabPanelModuleDescriptor;
import com.atlassian.jira.util.JiraDurationUtils;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Unit test of {@link WorklogAction}.
 */
public class TestWorklogAction
{
    private Locale locale = Locale.getDefault();    // final => can't be mocked by Mockito
    private WorklogAction worklogAction;
    @Mock private FieldLayoutManager mockFieldLayoutManager;
    @Mock private Issue mockIssue;
    @Mock private IssueTabPanelModuleDescriptor mockModuleDescriptor;
    @Mock private JiraDurationUtils mockJiraDurationUtils;
    @Mock private RendererManager mockRendererManager;
    @Mock private Worklog mockWorklog;

    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        when(mockWorklog.getIssue()).thenReturn(mockIssue);
        this.worklogAction = new WorklogAction(mockModuleDescriptor, mockWorklog, mockJiraDurationUtils, true, true, mockFieldLayoutManager, mockRendererManager, locale);
    }

    @Test // for JRA-30195
    public void gettingPrettyDurationShouldUseProvidedLocale()
    {
        // Set up
        final String rawDuration = "111";
        final String expectedPrettyDuration = "Oooh, nice!";
        when(mockJiraDurationUtils.getFormattedDuration(new Long(rawDuration), locale)).thenReturn(expectedPrettyDuration);

        // Invoke
        final String actualPrettyDuration = worklogAction.getPrettyDuration(rawDuration);

        // Check
        assertEquals(expectedPrettyDuration, actualPrettyDuration);
    }
}
