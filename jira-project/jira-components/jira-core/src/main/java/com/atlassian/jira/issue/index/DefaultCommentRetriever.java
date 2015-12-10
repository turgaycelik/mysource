/**
 * Copyright 2008 Atlassian Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atlassian.jira.issue.index;

import java.util.List;

import javax.annotation.Nonnull;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.issue.index.DefaultIssueIndexer.CommentRetriever;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

public class DefaultCommentRetriever implements CommentRetriever
{
    private final CommentManager commentManager;

    public DefaultCommentRetriever(@Nonnull final CommentManager commentManager)
    {
        this.commentManager = notNull("commentManager", commentManager);
    }

    @Override
    public List<Comment> apply(final Issue issue)
    {
        return commentManager.getComments(issue);
    }
}