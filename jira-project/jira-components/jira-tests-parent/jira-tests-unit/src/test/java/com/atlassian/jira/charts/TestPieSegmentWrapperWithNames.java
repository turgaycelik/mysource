package com.atlassian.jira.charts;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.plugin.customfield.CustomFieldSearcherModuleDescriptor;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.I18nHelper;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;

import webwork.util.TextUtil;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @since v6.0
 */
@RunWith (value = Parameterized.class)
public class TestPieSegmentWrapperWithNames
{
    private final String nameUnderTest;


    public TestPieSegmentWrapperWithNames(final String testName)
    {
        this.nameUnderTest = testName;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data()
    {
        final List<String> testNames = Arrays.asList(
                "Regular name",
                "Invalid <html \"here\"",
                "Some utf8 ( \u0361\u00b0 \u035c\u0296 \u0361\u00b0) value",
                "Some value with <b>html</b>",
                "encoded html &lt;a href=&quot;http://test/me?x=1&amp;amp;y=2&gt;HTML&lt;a&gt;"
        );

        return Lists.transform(testNames, new Function<String, Object[]>()
        {
            @Override
            public Object[] apply(final String testName)
            {
                return new Object[] { testName };
            }
        });
    }

    @Test
    public void testAsigneesDisplayName()
    {
        testWithUserDisplayNameImpl(nameUnderTest, "assignees");
    }

    @Test
    public void testReporterName()
    {
        testWithUserDisplayNameImpl(nameUnderTest, "reporter");
    }

    @Test
    public void testProjectName()
    {
        final MockGenericValue project = new MockGenericValue("project", EasyMap.build("id", 10000l, "name", nameUnderTest));
        testWithNameImpl(nameUnderTest, project, "project");
    }

    @Test
    public void testResolution()
    {
        testWithNameImpl(nameUnderTest, createIssueConstantMock(nameUnderTest), "resolution");
    }

    @Test
    public void testPriorities()
    {
        testWithNameImpl(nameUnderTest, createIssueConstantMock(nameUnderTest), "priorities");
    }

    @Test
    public void testIssueType()
    {
        testWithNameImpl(nameUnderTest, createIssueConstantMock(nameUnderTest), "issuetype");
    }

    @Test
    public void testStatuses()
    {
        testWithNameImpl(nameUnderTest, createIssueConstantMock(nameUnderTest), "statuses");
    }

    @Test
    public void testComponentName()
    {
        final MockGenericValue project = new MockGenericValue("component", EasyMap.build("id", 10000l, "name", nameUnderTest));
        testWithNameImpl(nameUnderTest, project, "components");
    }

    @Test
    public void testVersionName()
    {
        testWithNameImpl(nameUnderTest, createVersionMock(nameUnderTest), "version");
    }

    @Test
    public void testAllVersionName()
    {
        testWithNameImpl(nameUnderTest, createVersionMock(nameUnderTest), "allVersion");
    }

    @Test
    public void testFixVersionName()
    {
        testWithNameImpl(nameUnderTest, createVersionMock(nameUnderTest), "fixfor");
    }

    @Test
    public void testAllFixVersionName()
    {
        testWithNameImpl(nameUnderTest, createVersionMock(nameUnderTest), "allFixfor");
    }

    @Test
    public void testCustomFieldName()
    {
        final String customFieldId = "customfield_1025";
        final String fieldStatHtml = TextUtil.escapeHTML(nameUnderTest);

        final CustomField customField = mock(CustomField.class);
        final CustomFieldSearcher mockCustomFieldSearcher = mock(CustomFieldSearcher.class);
        final CustomFieldSearcherModuleDescriptor customFieldSearcherModuleDescriptor = mock(CustomFieldSearcherModuleDescriptor.class);
        final CustomFieldManager customFieldManager = mock(CustomFieldManager.class);

        when(customFieldSearcherModuleDescriptor.getStatHtml(Mockito.eq(customField), Mockito.any(), Mockito.isNull(String.class))).thenReturn(fieldStatHtml);
        when(mockCustomFieldSearcher.getDescriptor()).thenReturn(customFieldSearcherModuleDescriptor);
        when(customField.getCustomFieldSearcher()).thenReturn(mockCustomFieldSearcher);
        when(customFieldManager.getCustomFieldObject(customFieldId)).thenReturn(customField);

        testWithNameImpl(nameUnderTest, customField, customFieldId, customFieldManager);
    }

    private void testWithNameImpl(final String name, final Object key, final String statisticType)
    {
        testWithNameImpl(name, key, statisticType, null);
    }

    private void testWithNameImpl(final String name, final Object key, final String statisticType, final CustomFieldManager customFieldManager)
    {
        final I18nHelper mockI18nBean = mock(I18nHelper.class);
        final PieSegmentWrapper pieSegmentWrapper = new PieSegmentWrapper(key, mockI18nBean, statisticType, null, customFieldManager);
        assertEquals(key, pieSegmentWrapper.getKey());
        assertEquals(name, pieSegmentWrapper.getName());
        assertEquals(name, pieSegmentWrapper.toString());
    }

    private void testWithUserDisplayNameImpl(final String displayName, final String statisticType)
    {
        testWithNameImpl(displayName, new MockUser("admin", displayName, ""), statisticType);
    }

    private IssueConstant createIssueConstantMock(final String nameTranslation)
    {
        final IssueConstant issueConstant = mock(IssueConstant.class);
        when(issueConstant.getNameTranslation()).thenReturn(nameTranslation);
        return issueConstant;
    }

    private Version createVersionMock(final String name)
    {
        final Version mockVersion = mock(Version.class);
        when(mockVersion.getName()).thenReturn(name);
        return mockVersion;
    }

}
