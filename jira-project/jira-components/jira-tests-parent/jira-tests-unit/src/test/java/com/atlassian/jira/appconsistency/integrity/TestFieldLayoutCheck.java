package com.atlassian.jira.appconsistency.integrity;

import java.util.List;

import com.atlassian.core.ofbiz.util.CoreTransactionUtil;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.appconsistency.integrity.check.FieldLayoutCheck;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static com.atlassian.core.ofbiz.test.UtilsForTests.getTestEntity;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestFieldLayoutCheck
{
    private FieldLayoutCheck flCheck;
    @Mock private FieldManager mockFieldManager;
    @Mock private I18nHelper mockI18nHelper;

    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
        CoreTransactionUtil.setUseTransactions(false);

        final JiraAuthenticationContext mockJiraAuthenticationContext = mock(JiraAuthenticationContext.class);
        when(mockJiraAuthenticationContext.getI18nHelper()).thenReturn(mockI18nHelper);

        final OfBizDelegator ofBizDelegator = new MockOfBizDelegator();
        new MockComponentWorker().init()
                .addMock(JiraAuthenticationContext.class, mockJiraAuthenticationContext)
                .addMock(OfBizDelegator.class, ofBizDelegator);

        when(mockFieldManager.isOrderableField("customfield_1")).thenReturn(true);
        when(mockFieldManager.isOrderableField("customfield_2")).thenReturn(false);
        when(mockFieldManager.isOrderableField("customfield_3")).thenReturn(false);

        getTestEntity("FieldLayoutItem", EasyMap.build("id", new Long(1001), "fieldidentifier", "customfield_1"));
        getTestEntity("FieldLayoutItem", EasyMap.build("id", new Long(1002), "fieldidentifier", "customfield_2"));
        getTestEntity("FieldLayoutItem", EasyMap.build("id", new Long(1003), "fieldidentifier", "customfield_3"));

        flCheck = new FieldLayoutCheck(ofBizDelegator, 1);
        flCheck.setFieldManager(mockFieldManager);
    }

    private void assertNoProblems() throws Exception
    {
        when(mockFieldManager.isOrderableField("customfield_1")).thenReturn(true);

        List amendments = flCheck.preview();
        assertEquals(0, amendments.size());
    }

    @Test
    public void testPreview() throws Exception
    {
        // Invoke
        List amendments = flCheck.preview();

        // Check
        assertEquals(2, amendments.size());
    }

    @Test
    public void testValidateCorrect() throws Exception
    {
        // This should correct the problem
        List amendments = flCheck.correct();

        // Check
        assertEquals(2, amendments.size());
        assertNoProblems();
    }

    @After
    public void tearDown() {
        ComponentAccessor.initialiseWorker(null);
        CoreTransactionUtil.setUseTransactions(true);
    }
}
