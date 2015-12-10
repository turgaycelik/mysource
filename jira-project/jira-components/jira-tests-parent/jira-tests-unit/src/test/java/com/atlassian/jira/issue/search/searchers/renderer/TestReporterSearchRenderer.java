package com.atlassian.jira.issue.search.searchers.renderer;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.local.runner.ListeningMockitoRunner;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.web.FieldVisibilityManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * @since v5.2
 */
@RunWith (ListeningMockitoRunner.class)
public class TestReporterSearchRenderer
{
    @Mock private FieldVisibilityManager fieldVisibilityManager;
    private ReporterSearchRenderer reporterSearchRenderer;

    @Before
    public void setUp()
    {
        new MockComponentWorker().init();
        reporterSearchRenderer = new ReporterSearchRenderer(null, null, null,
                null, null, null, null, fieldVisibilityManager);
    }

    @Test
    public void testIsShown()
    {
        when(fieldVisibilityManager.isFieldHiddenInAllSchemes(anyString(),
                any(SearchContext.class), any(User.class))).thenReturn(true);
        assertFalse(reporterSearchRenderer.isShown(null, null));

        when(fieldVisibilityManager.isFieldHiddenInAllSchemes(anyString(),
                any(SearchContext.class), any(User.class))).thenReturn(false);
        assertTrue(reporterSearchRenderer.isShown(null, null));
    }
}
