package com.atlassian.jira.plugin.webfragment.model;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.atlassian.fugue.Effect;
import com.atlassian.fugue.Option;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.web.ExecutingHttpRequest;

import com.google.common.collect.ImmutableMap;

/**
 * Specific implementation of JiraHelper which holds issue and comment.
 *
 * @since 6.2
 */
public class CommentHelper extends JiraHelper
{
    private final Option<Issue> issue;
    private final Option<Comment> comment;

    public CommentHelper(HttpServletRequest request, Project project, Option<Issue> issue, Option<Comment> comment)
    {
        super(request, project);
        this.issue = issue;
        this.comment = comment;
    }

    @Override
    public Map<String, Object> getContextParams()
    {
        final ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        builder.putAll(super.getContextParams());
        issue.foreach(new Effect<Issue>()
        {
            @Override
            public void apply(final Issue issue)
            {
                builder.put("issue", issue);
            }
        });
        comment.foreach(new Effect<Comment>()
        {
            @Override
            public void apply(final Comment comment)
            {
                builder.put("comment", comment);
            }
        });
        return builder.build();
    }

    public static CommentHelperBuilder builder()
    {
        return new CommentHelperBuilder();
    }

    public static class CommentHelperBuilder
    {
        private Issue issue;
        private Comment comment;

        public CommentHelperBuilder issue(final Issue issue)
        {
            this.issue = issue;
            return this;
        }

        public CommentHelperBuilder comment(final Comment comment)
        {
            this.comment = comment;
            return this;
        }

        public CommentHelper build()
        {
            if (issue != null)
            {
                return new CommentHelper(ExecutingHttpRequest.get(), issue.getProjectObject(), Option.option(issue), Option.option(comment));
            }
            // bulk edit, no issue in context
            else
            {
                return new CommentHelper(ExecutingHttpRequest.get(), null, Option.<Issue>none(), Option.<Comment>none());
            }
        }
    }
}
