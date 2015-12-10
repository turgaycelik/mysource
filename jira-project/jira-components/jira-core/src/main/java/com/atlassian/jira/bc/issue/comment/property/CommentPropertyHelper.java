package com.atlassian.jira.bc.issue.comment.property;

import com.atlassian.fugue.Function2;
import com.atlassian.fugue.Option;
import com.atlassian.jira.entity.property.EntityProperty;
import com.atlassian.jira.entity.property.EntityPropertyHelper;
import com.atlassian.jira.entity.property.EntityPropertyType;
import com.atlassian.jira.event.issue.comment.property.CommentPropertyDeletedEvent;
import com.atlassian.jira.event.issue.comment.property.CommentPropertySetEvent;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.comments.CommentPermissionManager;
import com.atlassian.jira.issue.comments.CommentSearchManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.google.common.base.Function;

/**
 *
 */
public class CommentPropertyHelper implements EntityPropertyHelper<Comment>
{
    private final CommentPermissionManager commentPermissionManager;
    private final CommentSearchManager commentSearchManager;
    private final I18nHelper i18n;
    private final CheckPermissionFunction<Comment> hasEditPermissionFunction = new CheckPermissionFunction<Comment>()
    {
        @Override
        public ErrorCollection apply(ApplicationUser applicationUser, Comment comment)
        {
            ErrorCollection errorCollection = new SimpleErrorCollection();
            if (!commentPermissionManager.hasEditPermission(applicationUser, comment))
            {
                ErrorCollection.Reason reason = applicationUser == null ? ErrorCollection.Reason.NOT_LOGGED_IN : ErrorCollection.Reason.FORBIDDEN;
                errorCollection.addErrorMessage(i18n.getText("comment.service.error.no.comment.visibility.no.user"), reason);
            }
            return errorCollection;
        }
    };
    private final CheckPermissionFunction<Comment> hasReadPermissionFunction = new CheckPermissionFunction<Comment>()
    {
        @Override
        public ErrorCollection apply(ApplicationUser applicationUser, Comment comment)
        {
            ErrorCollection errorCollection = new SimpleErrorCollection();
            if (!commentPermissionManager.hasBrowsePermission(applicationUser, comment))
            {
                ErrorCollection.Reason reason = applicationUser == null ? ErrorCollection.Reason.NOT_LOGGED_IN : ErrorCollection.Reason.FORBIDDEN;
                errorCollection.addErrorMessage(i18n.getText("comment.service.error.no.edit.permission.no.user"), reason);
            }
            return errorCollection;
        }
    };
    private final Function<Long, Option<Comment>> entityByIdFunction = new Function<Long, Option<Comment>>()
    {
        @Override
        public Option<Comment> apply(Long id)
        {
            return Option.option(commentSearchManager.getCommentById(id));
        }
    };
    private final Function2<ApplicationUser, EntityProperty, CommentPropertySetEvent> setPropertyEventFunction = new Function2<ApplicationUser, EntityProperty, CommentPropertySetEvent>()
    {
        @Override
        public CommentPropertySetEvent apply(final ApplicationUser user, final EntityProperty entityProperty)
        {
            return new CommentPropertySetEvent(entityProperty, user);
        }
    };
    private final Function2<ApplicationUser, EntityProperty, CommentPropertyDeletedEvent> deletePropertyEventFunction = new Function2<ApplicationUser, EntityProperty, CommentPropertyDeletedEvent>()
    {
        @Override
        public CommentPropertyDeletedEvent apply(final ApplicationUser user, final EntityProperty entityProperty)
        {
            return new CommentPropertyDeletedEvent(entityProperty, user);
        }
    };

    public CommentPropertyHelper(CommentPermissionManager commentPermissionManager, final CommentSearchManager commentSearchManager, I18nHelper i18n)
    {
        this.commentPermissionManager = commentPermissionManager;
        this.commentSearchManager = commentSearchManager;
        this.i18n = i18n;
    }

    @Override
    public CheckPermissionFunction<Comment> hasEditPermissionFunction()
    {
        return hasEditPermissionFunction;
    }

    @Override
    public CheckPermissionFunction<Comment> hasReadPermissionFunction()
    {
        return hasReadPermissionFunction;
    }

    @Override
    public Function<Long, Option<Comment>> getEntityByIdFunction()
    {
        return entityByIdFunction;
    }

    @Override
    public Function2<ApplicationUser, EntityProperty, CommentPropertySetEvent> createSetPropertyEventFunction()
    {
        return setPropertyEventFunction;
    }

    @Override
    public Function2<ApplicationUser, EntityProperty, CommentPropertyDeletedEvent> createDeletePropertyEventFunction()
    {
        return deletePropertyEventFunction;
    }

    @Override
    public EntityPropertyType getEntityPropertyType()
    {
        return EntityPropertyType.COMMENT_PROPERTY;
    }
}
