package com.atlassian.jira.appconsistency.integrity;

import java.util.List;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.jira.appconsistency.integrity.check.SchemePermissionCheck;
import com.atlassian.jira.appconsistency.integrity.exception.IntegrityException;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.security.JiraAuthenticationContext;

import com.google.common.collect.ImmutableMap;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class TestSchemePermissionCheck
{
    @Rule
    public final RuleChain mockitoMocks = MockitoMocksInContainer.forTest(this);

    @AvailableInContainer
    private OfBizDelegator ofBizDelegator  = new MockOfBizDelegator();

    @Mock
    @AvailableInContainer
    private JiraAuthenticationContext authenticationContext;

    private SchemePermissionCheck schemePermissionCheck;

    @Before
    public void setUp() throws Exception
    {
        Mockito.when(authenticationContext.getI18nHelper()).thenReturn(new MockI18nHelper());
        schemePermissionCheck = new SchemePermissionCheck(ofBizDelegator, 1);

        addTestEntity(1000, 1000, "type1");
        addTestEntity(1001, 1001, "type1");
        addTestEntity(1002, 1001, "type1");
        addTestEntity(1003, 1002, "type3");
        addTestEntity(1004, 1002, "type3");
    }

    private void addTestEntity(long id, long schemeId, String type)
    {
        UtilsForTests.getTestEntity("SchemePermissions",
                ImmutableMap.of("id", new Long(id), "scheme", new Long(schemeId), "permission", new Long(1000), "type", type, "parameter", "parameter1"));
    }

    @Test
    public void testPreviewReturnsProperAmountOfAmendments() throws IntegrityException
    {
        List amendments = schemePermissionCheck.preview();
        assertThat(amendments.size(), Matchers.equalTo(2));
    }

    @Test
    public void testPreviewIsNotRemovingAnythingFromDatabase() throws Exception
    {
        schemePermissionCheck.preview();
        assertThat(ofBizDelegator.findAll("SchemePermissions").size(), equalTo(5));
    }

    @Test
    public void testCorrectReturnsProperAmountOfAmendments() throws Exception
    {
        List amendments = schemePermissionCheck.correct();
        assertThat(amendments.size(), equalTo(2));
    }

    @Test
    public void testCorrectRemovesEntriesInDatabase() throws Exception
    {
        schemePermissionCheck.correct();
        assertThat(ofBizDelegator.findAll("SchemePermissions").size(), equalTo(3));
    }

    @Test
    public void testAfterCorrectPreviewReturnsEmptyList() throws Exception
    {
        schemePermissionCheck.correct();
        final List previewAmendments = schemePermissionCheck.preview();
        assertTrue("After correct(), preview() should return empty list", previewAmendments.isEmpty());
    }

}