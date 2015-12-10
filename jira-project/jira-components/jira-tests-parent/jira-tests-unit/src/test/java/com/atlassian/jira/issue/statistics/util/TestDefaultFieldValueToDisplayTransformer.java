package com.atlassian.jira.issue.statistics.util;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.issue.statistics.FilterStatisticsValuesGenerator;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.I18nHelper;

import org.easymock.classextension.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.ofbiz.core.entity.GenericValue;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for {@link com.atlassian.jira.issue.statistics.util.DefaultFieldValueToDisplayTransformer}
 * @since v4.1
 */
public class TestDefaultFieldValueToDisplayTransformer
{
    final I18nHelper i18nBean = new MockI18nHelper();
    final CustomFieldManager customFieldManager = EasyMock.createMock(CustomFieldManager.class);
    final FieldValueToDisplayTransformer<String> transformer = new DefaultFieldValueToDisplayTransformer(i18nBean,
            customFieldManager);
    final String url = "http://www.lolcats.com";
    final GenericValue mockGenericValue = EasyMock.createMock(GenericValue.class);
    final String expectedDisplayValue = "LOLCATS";

    @Before
    public void setUp() throws Exception {
        EasyMock.reset(mockGenericValue);
    }

    @Test
    public void testTransformVersionAndAllVersion() throws Exception
    {
        final Version version = EasyMock.createMock(Version.class);

        EasyMock.expect(version.getName()).andReturn(expectedDisplayValue);
        EasyMock.replay(version);
        final String versionResult = ObjectToFieldValueMapper.transform(FilterStatisticsValuesGenerator.VERSION, version, url, transformer);
        assertEquals(versionResult, expectedDisplayValue);

        EasyMock.reset(version);
        EasyMock.expect(version.getName()).andReturn(expectedDisplayValue);
        EasyMock.replay(version);        
        final String allVersionResult = ObjectToFieldValueMapper.transform(FilterStatisticsValuesGenerator.ALLVERSION, version, url, transformer);
        assertEquals(allVersionResult, expectedDisplayValue);

        final String noVersionResult = ObjectToFieldValueMapper.transform(FilterStatisticsValuesGenerator.VERSION, null, url, transformer);
        assertEquals(noVersionResult, i18nBean.getText("gadget.filterstats.raisedin.unscheduled"));

        final String noAllVersionResult = ObjectToFieldValueMapper.transform(FilterStatisticsValuesGenerator.ALLVERSION, null, url, transformer);
        assertEquals(noAllVersionResult, i18nBean.getText("gadget.filterstats.raisedin.unscheduled"));
        
    }

    @Test
    public void testTransformFixForAndAllFixFor() throws Exception
    {
        final Version version = EasyMock.createMock(Version.class);

        EasyMock.expect(version.getName()).andReturn(expectedDisplayValue);
        EasyMock.replay(version);
        final String fixForResult = ObjectToFieldValueMapper.transform(FilterStatisticsValuesGenerator.FIXFOR, version, url, transformer);
        assertEquals(fixForResult, expectedDisplayValue);

        EasyMock.reset(version);
        EasyMock.expect(version.getName()).andReturn(expectedDisplayValue);
        EasyMock.replay(version);
        final String allFixForResult = ObjectToFieldValueMapper.transform(FilterStatisticsValuesGenerator.ALLFIXFOR, version, url, transformer);
        assertEquals(allFixForResult, expectedDisplayValue);

        final String noFixForResult = ObjectToFieldValueMapper.transform(FilterStatisticsValuesGenerator.FIXFOR, null, url, transformer);
        assertEquals(noFixForResult, i18nBean.getText("gadget.filterstats.fixfor.unscheduled"));

        final String noAllFixForResult = ObjectToFieldValueMapper.transform(FilterStatisticsValuesGenerator.ALLFIXFOR, null, url, transformer);
        assertEquals(noAllFixForResult, i18nBean.getText("gadget.filterstats.fixfor.unscheduled"));
    }

    @Test
    public void testIrrelevant() throws Exception
    {
        final String result = ObjectToFieldValueMapper.transform(null, FilterStatisticsValuesGenerator.IRRELEVANT, null, transformer);
        assertEquals(result, i18nBean.getText("common.concepts.irrelevant"));
    }

    @Test
    public void testProject() throws Exception
    {
        EasyMock.expect(mockGenericValue.getString(EasyMock.eq("name"))).andReturn(expectedDisplayValue);
        EasyMock.replay(mockGenericValue);
        final String result = ObjectToFieldValueMapper.transform(FilterStatisticsValuesGenerator.PROJECT, mockGenericValue, null, transformer);
        assertEquals(result, expectedDisplayValue);
    }

    @Test
    public void testComponents() throws Exception
    {
        EasyMock.expect(mockGenericValue.getString(EasyMock.eq("name"))).andReturn(expectedDisplayValue);
        EasyMock.replay(mockGenericValue);
        final String result = ObjectToFieldValueMapper.transform(FilterStatisticsValuesGenerator.COMPONENTS, mockGenericValue, null, transformer);
        assertEquals(result, expectedDisplayValue);

        final String noComponentResult = ObjectToFieldValueMapper.transform(FilterStatisticsValuesGenerator.COMPONENTS, null, null, transformer);
        assertEquals(noComponentResult, i18nBean.getText("gadget.filterstats.component.nocomponent"));

    }

    @Test
    public void testAssigneesAndReporter() throws Exception
    {
        final User mockUser = new MockUser("test", expectedDisplayValue, "someguy@hotmail.com");
        final String result = ObjectToFieldValueMapper.transform(FilterStatisticsValuesGenerator.ASSIGNEES, mockUser, null, transformer);
        assertEquals(result, expectedDisplayValue);

        final String noAssigneeResult = ObjectToFieldValueMapper.transform(FilterStatisticsValuesGenerator.ASSIGNEES, null, null, transformer);
        assertEquals(noAssigneeResult, i18nBean.getText("gadget.filterstats.assignee.unassigned"));

        final String reporterResult = ObjectToFieldValueMapper.transform(FilterStatisticsValuesGenerator.REPORTER, mockUser, null, transformer);
        assertEquals(reporterResult, expectedDisplayValue);

        final String noReporterResult = ObjectToFieldValueMapper.transform(FilterStatisticsValuesGenerator.REPORTER, null, null, transformer);
        assertEquals(noReporterResult, i18nBean.getText("gadget.filterstats.reporter.unknown"));

    }

    @Test
    public void testCreator() throws Exception
    {
        final User mockUser = new MockUser("test", expectedDisplayValue, "someguy@hotmail.com");
        final String result = ObjectToFieldValueMapper.transform(FilterStatisticsValuesGenerator.CREATOR, mockUser, null, transformer);
        assertEquals(result, expectedDisplayValue);

        final String noCreatorResult = ObjectToFieldValueMapper.transform(FilterStatisticsValuesGenerator.CREATOR, null, null, transformer);
        assertEquals(noCreatorResult, i18nBean.getText("gadget.filterstats.creator.unknown"));
    }

    @Test
    public void testCustomField() throws Exception {

        final String noCustomResult = ObjectToFieldValueMapper.transform("Some custom field", null, null, transformer);
        assertEquals(noCustomResult, i18nBean.getText("common.words.none"));
        
    }

    @Test
    public void testPriorities() throws Exception {

        final IssueConstant mockIssueConstant = EasyMock.createMock(IssueConstant.class);
        EasyMock.expect(mockIssueConstant.getNameTranslation()).andReturn(expectedDisplayValue);
        EasyMock.replay(mockIssueConstant);

        final String prioritiesResult = ObjectToFieldValueMapper.transform(FilterStatisticsValuesGenerator.PRIORITIES, mockIssueConstant, null, transformer);
        assertEquals(prioritiesResult, expectedDisplayValue);

        final String noPrioritiesResult = ObjectToFieldValueMapper.transform(FilterStatisticsValuesGenerator.PRIORITIES, null, null, transformer);
        assertEquals(noPrioritiesResult, i18nBean.getText("gadget.filterstats.priority.nopriority"));

    }

    @Test
    public void testIssueTypes() throws Exception {

        final IssueConstant mockIssueConstant = EasyMock.createMock(IssueConstant.class);
        EasyMock.expect(mockIssueConstant.getNameTranslation()).andReturn(expectedDisplayValue);
        EasyMock.replay(mockIssueConstant);

        final String issueTypesResult = ObjectToFieldValueMapper.transform(FilterStatisticsValuesGenerator.ISSUETYPE, mockIssueConstant, null, transformer);
        assertEquals(issueTypesResult, expectedDisplayValue);

    }

    @Test
    public void testResolution() throws Exception {

        final IssueConstant mockIssueConstant = EasyMock.createMock(IssueConstant.class);
        EasyMock.expect(mockIssueConstant.getNameTranslation()).andReturn(expectedDisplayValue);
        EasyMock.replay(mockIssueConstant);

        final String resolutionResult = ObjectToFieldValueMapper.transform(FilterStatisticsValuesGenerator.RESOLUTION, mockIssueConstant, null, transformer);
        assertEquals(resolutionResult, expectedDisplayValue);

        final String noResolutionResult = ObjectToFieldValueMapper.transform(FilterStatisticsValuesGenerator.RESOLUTION, null, null, transformer);
        assertEquals(noResolutionResult, i18nBean.getText("common.resolution.unresolved"));
    }



}
