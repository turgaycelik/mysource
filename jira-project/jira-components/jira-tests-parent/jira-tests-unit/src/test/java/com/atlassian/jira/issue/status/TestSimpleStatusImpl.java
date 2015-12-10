package com.atlassian.jira.issue.status;

import com.atlassian.jira.issue.status.category.StatusCategory;
import com.atlassian.jira.issue.status.category.StatusCategoryImpl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Test Case for SimpleStatusImpl
 *
 * @since v6.1
 */
public class TestSimpleStatusImpl
{
    @Mock Status status;

    private final String MOCK_STATUS_ID = "2";
    private final String MOCK_STATUS_NAME = "Status name";
    private final String MOCK_STATUS_DESCRIPTION = "A short status description";
    private final String MOCK_STATUS_ICON_URL = "/images/status-icon.png";

    @Before
    public void setup()
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testSimpleStatusImplCreation()
    {
        StatusCategory statusCategory = StatusCategoryImpl.getDefault();

        when(status.getId()).thenReturn(MOCK_STATUS_ID);
        when(status.getNameTranslation()).thenReturn(MOCK_STATUS_NAME);
        when(status.getDescTranslation()).thenReturn(MOCK_STATUS_DESCRIPTION);
        when(status.getCompleteIconUrl()).thenReturn(MOCK_STATUS_ICON_URL);
        when(status.getStatusCategory()).thenReturn(statusCategory);

        SimpleStatus simpleStatus = new SimpleStatusImpl(status);

        assertEquals(MOCK_STATUS_ID, simpleStatus.getId());
        assertEquals(MOCK_STATUS_NAME, simpleStatus.getName());
        assertEquals(MOCK_STATUS_DESCRIPTION, simpleStatus.getDescription());
        assertEquals(MOCK_STATUS_ICON_URL, simpleStatus.getIconUrl());
        assertTrue("There should be only one statusCategory object", simpleStatus.getStatusCategory().equals(statusCategory));
    }
}
