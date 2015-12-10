package com.atlassian.jira.web.action.issue;

import java.util.Map;

import com.atlassian.gzipfilter.org.apache.commons.lang.StringUtils;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.descriptors.WebPanelModuleDescriptor;
import com.atlassian.plugin.web.model.WebPanel;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.when;

/**
 * @since v6.3
 */
public class ViewSubtaskFragmentActionTest
{
    public static final String EXPECTED_HTML_OUTPUT = "<strong>content</strong>";

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);

        when(webInterfaceManager.getDisplayableWebPanelDescriptors(Matchers.eq("atl.jira.view.issue.left.context"), anyMap())).thenReturn(ImmutableList.of(webPanelModuleDescriptor1, webPanelModuleDescriptor2));
        when(webPanelModuleDescriptor1.getCompleteKey()).thenReturn("com.atlassian.jira.jira-view-issue-plugin:view-subtasks");
        when(webPanelModuleDescriptor1.getModule()).thenReturn(moduleWebPanel1);
        when(webPanelModuleDescriptor2.getCompleteKey()).thenReturn("com.atlassian.jira.jira-view-issue-plugin:view-subtasks");
        when(webPanelModuleDescriptor2.getModule()).thenReturn(moduleWebPanel2);
    }


    @Rule
    public final RuleChain mockContainer = MockitoMocksInContainer.forTest(this);

    @Mock
    WebInterfaceManager webInterfaceManager;

    @Mock
    WebPanelModuleDescriptor webPanelModuleDescriptor1;

    @Mock
    WebPanelModuleDescriptor webPanelModuleDescriptor2;

    @Mock
    WebPanel moduleWebPanel1;

    @Mock
    WebPanel moduleWebPanel2;

    @Test
    public void firstModuleShouldReturnHtml() throws Exception
    {

        when(moduleWebPanel1.getHtml(anyMap())).thenReturn(EXPECTED_HTML_OUTPUT);
        when(moduleWebPanel2.getHtml(anyMap())).thenReturn(null);

        ViewSubtaskFragmentAction viewSubtaskFragmentAction = new ViewSubtaskFragmentAction(webInterfaceManager) {
            protected Map<String,Object> getWebPanelContext() {
                return Maps.newHashMap();
            }
        };

        assertEquals(viewSubtaskFragmentAction.getHtml(), EXPECTED_HTML_OUTPUT);
    }

    @Test
    public void secondModuleShouldReturnHtml() throws Exception
    {
        when(moduleWebPanel1.getHtml(anyMap())).thenReturn(null);
        when(moduleWebPanel2.getHtml(anyMap())).thenReturn(EXPECTED_HTML_OUTPUT);

        ViewSubtaskFragmentAction viewSubtaskFragmentAction = new ViewSubtaskFragmentAction(webInterfaceManager) {
            protected Map<String,Object> getWebPanelContext() {
                return Maps.newHashMap();
            }
        };

        assertEquals(viewSubtaskFragmentAction.getHtml(), EXPECTED_HTML_OUTPUT);
    }

    @Test
    public void noneModuleShouldReturnHtml() throws Exception
    {
        when(moduleWebPanel1.getHtml(anyMap())).thenReturn(null);
        when(moduleWebPanel2.getHtml(anyMap())).thenReturn(null);

        ViewSubtaskFragmentAction viewSubtaskFragmentAction = new ViewSubtaskFragmentAction(webInterfaceManager) {
            protected Map<String,Object> getWebPanelContext() {
                return Maps.newHashMap();
            }
        };

        assertEquals(viewSubtaskFragmentAction.getHtml(), StringUtils.EMPTY);
    }

    @Test
    public void bothModuleReturnHtmlButOnlyFirstGoes() throws Exception
    {
        when(moduleWebPanel1.getHtml(anyMap())).thenReturn(EXPECTED_HTML_OUTPUT);
        when(moduleWebPanel2.getHtml(anyMap())).thenReturn(EXPECTED_HTML_OUTPUT + "<b>more stuff</b>");

        ViewSubtaskFragmentAction viewSubtaskFragmentAction = new ViewSubtaskFragmentAction(webInterfaceManager) {
            protected Map<String,Object> getWebPanelContext() {
                return Maps.newHashMap();
            }
        };

        assertEquals(viewSubtaskFragmentAction.getHtml(), EXPECTED_HTML_OUTPUT);
    }

    @Test
    public void checkIfErrorsAreHandledByWrapper() throws Exception
    {
        //we do not need any assertions or rules here, if the wrapper fails, test will be red
        final ViewSubtaskFragmentAction viewSubtaskFragmentAction = new ViewSubtaskFragmentAction(webInterfaceManager) {
            public String getHtml() {
                throw new LinkageError("Check if ozymandias is wrapping this problem");
            }
        };
    }

    @Test
    public void checkIfExceptionsAreHandledByWrapper() throws Exception
    {
        //we do not need any assertions or rules here, if the wrapper fails, test will be red
        final ViewSubtaskFragmentAction viewSubtaskFragmentAction = new ViewSubtaskFragmentAction(webInterfaceManager) {
            public String getHtml() {
                throw new IllegalArgumentException("Check if ozymandias is wrapping this problem");
            }
        };
    }
}
