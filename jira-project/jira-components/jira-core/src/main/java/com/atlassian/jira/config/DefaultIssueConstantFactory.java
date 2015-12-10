package com.atlassian.jira.config;

import java.net.URI;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.avatar.AvatarImageResolver;
import com.atlassian.jira.avatar.UniversalAvatarsService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.issuetype.IssueTypeImpl;
import com.atlassian.jira.issue.priority.Priority;
import com.atlassian.jira.issue.priority.PriorityImpl;
import com.atlassian.jira.issue.resolution.Resolution;
import com.atlassian.jira.issue.resolution.ResolutionImpl;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.issue.status.StatusImpl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.BaseUrl;
import com.atlassian.jira.web.action.admin.translation.TranslationManager;

import org.ofbiz.core.entity.GenericValue;

/**
 * @since v5.2
 */
public class DefaultIssueConstantFactory implements IssueConstantFactory
{
    private final TranslationManager translationManager;
    private final JiraAuthenticationContext authenticationContext;
    private final BaseUrl baseUrl;
    private final StatusCategoryManager statusCategoryManager;
    private final AvatarManager avatarManager;

    public DefaultIssueConstantFactory(TranslationManager translationManager,
            JiraAuthenticationContext authenticationContext, BaseUrl baseUrl, StatusCategoryManager statusCategoryManager, AvatarManager avatarManager)
    {
        this.translationManager = translationManager;
        this.authenticationContext = authenticationContext;
        this.baseUrl = baseUrl;
        this.statusCategoryManager = statusCategoryManager;
        this.avatarManager = avatarManager;
    }

    @Override
    public Priority createPriority(GenericValue priorityGv)
    {
        return new PriorityImpl(priorityGv, translationManager, authenticationContext, baseUrl);
    }

    @Override
    public IssueType createIssueType(GenericValue issueTypeGv)
    {
        final IssueTypeImpl issueType = new IssueTypeImpl(issueTypeGv, translationManager, authenticationContext, baseUrl, avatarManager);

        final Avatar avatar = issueType.getAvatar();
        if (avatar != null)
        {
// dirty trick to update iconURL to this in avatar
// this shouldn't be here but somewhere in presentation layer where i should transform it to link
// (and apply user permissions - thankfully its not needed for issue types).
// I cannot inject UniversalAvatars here beacause there is circular dependecy project->issue+types->avatars->project.
// if i could put such code to presentation layer it would be possible. of course because of backward compatibility
            final UniversalAvatarsService universalAvatars = ComponentAccessor.getComponent(UniversalAvatarsService.class);
            final AvatarImageResolver uriForIssueType = universalAvatars.getImages(Avatar.Type.ISSUETYPE);

            final URI avatarURI = uriForIssueType.getAvatarRelativeUri(null, avatar, Avatar.Size.SMALL);
            final String avatarURIString = avatarURI.toASCIIString();
            issueType.setIconUrl(avatarURIString);
        }

        return issueType;
    }

    @Override
    public Resolution createResolution(GenericValue resolutionGv)
    {
        return new ResolutionImpl(resolutionGv, translationManager, authenticationContext, baseUrl);
    }

    @Override
    public Status createStatus(GenericValue statusGv)
    {
        return new StatusImpl(statusGv, translationManager, authenticationContext, baseUrl, statusCategoryManager);
    }
}
