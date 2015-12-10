package com.atlassian.jira.avatar;

import java.util.Map;
import java.util.NoSuchElementException;

import com.atlassian.jira.avatar.types.issuetype.IssueTypeAvatarImageResolver;
import com.atlassian.jira.avatar.types.issuetype.IssueTypeTypeAvatarService;
import com.atlassian.jira.avatar.types.project.ProjectAvatarImageResolver;
import com.atlassian.jira.avatar.types.project.ProjectTypeAvatarService;

import com.google.common.collect.ImmutableMap;


public class UniversalAvatarsServiceImpl implements UniversalAvatarsService
{
    private static class TypeAvatarsAndUris
    {
        final TypeAvatarService avatars;
        final AvatarImageResolver uriResolver;

        TypeAvatarsAndUris(final TypeAvatarService avatars, final AvatarImageResolver uriResolver)
        {
            this.avatars = avatars;
            this.uriResolver = uriResolver;
        }
    }

    private final Map<Avatar.Type, TypeAvatarsAndUris> typeAvatars;

    public UniversalAvatarsServiceImpl(
            IssueTypeTypeAvatarService issueTypeAvatars, IssueTypeAvatarImageResolver issueTypeAvatarUris,
            ProjectTypeAvatarService projectAvatars, ProjectAvatarImageResolver projectAvatarUriResolver)
    {
        typeAvatars = ImmutableMap.<Avatar.Type, TypeAvatarsAndUris>builder().
                put(Avatar.Type.ISSUETYPE, new TypeAvatarsAndUris(issueTypeAvatars, issueTypeAvatarUris)).
                put(Avatar.Type.PROJECT, new TypeAvatarsAndUris(projectAvatars, projectAvatarUriResolver)).
                build();
    }

    @Override
    public TypeAvatarService getAvatars(Avatar.Type type)
    {
        final TypeAvatarsAndUris typeItems = typeAvatars.get(type);

        return typeItems==null ? null : typeItems.avatars;
    }

    @Override
    public AvatarImageResolver getImages(Avatar.Type type)
    {
        final TypeAvatarsAndUris typeItems = typeAvatars.get(type);

        return typeItems==null ? null : typeItems.uriResolver;
    }
}
