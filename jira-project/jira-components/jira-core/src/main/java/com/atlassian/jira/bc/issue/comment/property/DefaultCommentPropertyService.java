package com.atlassian.jira.bc.issue.comment.property;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.entity.property.BaseEntityPropertyService;
import com.atlassian.jira.entity.property.JsonEntityPropertyManager;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.util.I18nHelper;

/**
 * @since 6.2
 */
public class DefaultCommentPropertyService extends BaseEntityPropertyService<Comment> implements CommentPropertyService
{
    public DefaultCommentPropertyService(JsonEntityPropertyManager jsonEntityPropertyManager, I18nHelper i18n,
                                         EventPublisher eventPublisher, CommentPropertyHelper entityPropertyHelper)
    {
        super(jsonEntityPropertyManager, i18n, eventPublisher, entityPropertyHelper);
    }
}
