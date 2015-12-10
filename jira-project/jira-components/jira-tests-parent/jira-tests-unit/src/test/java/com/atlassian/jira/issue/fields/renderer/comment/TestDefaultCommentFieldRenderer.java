package com.atlassian.jira.issue.fields.renderer.comment;

import com.atlassian.fugue.Option;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.plugin.comment.CommentFieldRendererModuleDescriptor;
import com.atlassian.jira.plugin.webfragment.model.CommentHelper;
import com.atlassian.jira.project.Project;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.web.Condition;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TestDefaultCommentFieldRenderer
{
    @Mock
    private PluginAccessor pluginAccessor;

    private CommentHelper commentHelper;
    private DefaultCommentFieldRenderer commentFieldRenderer;

    @Before
    public void setUp() throws Exception
    {
        Issue issue = mock(Issue.class);
        Project project = mock(Project.class);
        when(issue.getProjectObject()).thenReturn(project);

        this.commentFieldRenderer = new DefaultCommentFieldRenderer(pluginAccessor);
        this.commentHelper = new CommentHelper(mock(HttpServletRequest.class), project, Option.some(issue), Option.<Comment>none());
    }

    @Test
    public void getIssuePageEditHtmlWithSinglePlugin()
    {
        CommentFieldRendererModuleDescriptor moduleDescriptor = mockCommentFieldRendererModule("1", 10);
        when(pluginAccessor.getEnabledModuleDescriptorsByClass(CommentFieldRendererModuleDescriptor.class)).thenReturn(Lists.newArrayList(moduleDescriptor));
        String html = commentFieldRenderer.getIssuePageEditHtml(Maps.<String, Object>newHashMap(), commentHelper);

        assertThat(html, Matchers.containsString("issue-edit-1"));
    }

    @Test
    public void getIssuePageViewHtmlWithSinglePlugin()
    {
        CommentFieldRendererModuleDescriptor moduleDescriptor = mockCommentFieldRendererModule("1", 10);
        when(pluginAccessor.getEnabledModuleDescriptorsByClass(CommentFieldRendererModuleDescriptor.class)).thenReturn(Lists.newArrayList(moduleDescriptor));
        String html = commentFieldRenderer.getIssuePageViewHtml(Maps.<String, Object>newHashMap(), commentHelper);

        assertThat(html, Matchers.containsString("issue-view-1"));
    }

    @Test
    public void getFieldEditHtmlWithSinglePlugin()
    {
        CommentFieldRendererModuleDescriptor moduleDescriptor = mockCommentFieldRendererModule("1", 10);
        when(pluginAccessor.getEnabledModuleDescriptorsByClass(CommentFieldRendererModuleDescriptor.class)).thenReturn(Lists.newArrayList(moduleDescriptor));
        String issuePageEditHtml = commentFieldRenderer.getFieldEditHtml(Maps.<String, Object>newHashMap(), commentHelper);

        assertThat(issuePageEditHtml, Matchers.containsString("field-edit-1"));
    }

    @Test
    public void getFieldViewHtmlWithSinglePlugin()
    {
        CommentFieldRendererModuleDescriptor moduleDescriptor = mockCommentFieldRendererModule("1", 10);
        when(pluginAccessor.getEnabledModuleDescriptorsByClass(CommentFieldRendererModuleDescriptor.class)).thenReturn(Lists.newArrayList(moduleDescriptor));
        String html = commentFieldRenderer.getFieldViewHtml(Maps.<String, Object>newHashMap(), commentHelper);

        assertThat(html, Matchers.containsString("field-view-1"));
    }

    @Test
    public void getHtmlFromSystemWhenNoPluginFound()
    {
        CommentFieldRendererModuleDescriptor systemModuleDescriptor = mockCommentFieldRendererModule("system", -1);
        when(pluginAccessor.getEnabledModuleDescriptorsByClass(CommentFieldRendererModuleDescriptor.class)).thenReturn(Lists.newArrayList(systemModuleDescriptor));
        String html = commentFieldRenderer.getFieldViewHtml(Maps.<String, Object>newHashMap(), commentHelper);

        assertThat(html, Matchers.containsString("field-view-system"));
    }

    @Test
    public void getHtmlFromSystemWhenPluginReturnsNone()
    {
        Condition condition = mock(Condition.class);
        when(condition.shouldDisplay(Mockito.<Map<String, Object>>any())).thenReturn(false);
        CommentFieldRendererModuleDescriptor pluginModuleDescriptor = mock(CommentFieldRendererModuleDescriptor.class);
        when(pluginModuleDescriptor.getCondition()).thenReturn(condition);
        when(pluginModuleDescriptor.getWeight()).thenReturn(10);

        CommentFieldRendererModuleDescriptor systemModuleDescriptor = mockCommentFieldRendererModule("system-module", -1);
        when(pluginAccessor.getEnabledModuleDescriptorsByClass(CommentFieldRendererModuleDescriptor.class)).thenReturn(Lists.newArrayList(pluginModuleDescriptor, systemModuleDescriptor));
        String html = commentFieldRenderer.getFieldViewHtml(Maps.<String, Object>newHashMap(), commentHelper);

        assertThat(html, Matchers.containsString("field-view-system-module"));
    }

    @Test
    public void getHtmlFromPluginWithHighestWeight()
    {
        CommentFieldRendererModuleDescriptor pluginModuleDescriptor1 = mockCommentFieldRendererModule("1", 10);
        CommentFieldRendererModuleDescriptor pluginModuleDescriptor2 = mockCommentFieldRendererModule("2", 20);
        CommentFieldRendererModuleDescriptor systemModuleDescriptor = mockCommentFieldRendererModule("system", -1);
        when(pluginAccessor.getEnabledModuleDescriptorsByClass(CommentFieldRendererModuleDescriptor.class)).thenReturn(Lists.newArrayList(pluginModuleDescriptor1, pluginModuleDescriptor2, systemModuleDescriptor));
        String html = commentFieldRenderer.getFieldViewHtml(Maps.<String, Object>newHashMap(), commentHelper);

        assertThat(html, Matchers.containsString("field-view-2"));
        assertThat(html, Matchers.not(Matchers.containsString("field-view-1")));
        assertThat(html, Matchers.not(Matchers.containsString("field-view-system")));
    }

    @Test
    public void getHtmlFromCommentRendererWithTheSameWeight()
    {
        CommentFieldRendererModuleDescriptor pluginModuleDescriptor1 = mockCommentFieldRendererModule("1", 20);
        when(pluginModuleDescriptor1.getCompleteKey()).thenReturn("a-this-will-be-picked");
        CommentFieldRendererModuleDescriptor pluginModuleDescriptor2 = mockCommentFieldRendererModule("2", 20);
        when(pluginModuleDescriptor2.getCompleteKey()).thenReturn("z-this-wont-be-picked");
        when(pluginAccessor.getEnabledModuleDescriptorsByClass(CommentFieldRendererModuleDescriptor.class)).thenReturn(Lists.newArrayList(pluginModuleDescriptor1, pluginModuleDescriptor2));

        String html = commentFieldRenderer.getFieldViewHtml(Maps.<String, Object>newHashMap(), commentHelper);

        assertThat(html, Matchers.containsString("field-view-1"));
        assertThat(html, Matchers.not(Matchers.containsString("field-view-2")));
    }

    private CommentFieldRendererModuleDescriptor mockCommentFieldRendererModule(final String id, final int weight)
    {
        CommentFieldRendererModuleDescriptor moduleDescriptor = mock(CommentFieldRendererModuleDescriptor.class);

        when(moduleDescriptor.getWeight()).thenReturn(weight);
        when(moduleDescriptor.getFieldEditHtml(Mockito.<Map<String, Object>>any())).thenReturn(Option.some("field-edit-" + id));
        when(moduleDescriptor.getFieldViewHtml(Mockito.<Map<String, Object>>any())).thenReturn(Option.some("field-view-" + id));
        when(moduleDescriptor.getIssuePageEditHtml(Mockito.<Map<String, Object>>any())).thenReturn(Option.some("issue-edit-" + id));
        when(moduleDescriptor.getIssuePageViewHtml(Mockito.<Map<String, Object>>any())).thenReturn(Option.some("issue-view-" + id));

        Condition condition = mock(Condition.class);
        when(condition.shouldDisplay(Mockito.<Map<String, Object>>any())).thenReturn(true);
        when(moduleDescriptor.getCondition()).thenReturn(condition);

        return moduleDescriptor;
    }
}
