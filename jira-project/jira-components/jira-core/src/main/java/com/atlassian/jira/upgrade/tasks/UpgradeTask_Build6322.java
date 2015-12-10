package com.atlassian.jira.upgrade.tasks;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.CachingTaggingAvatarStore;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.entity.GenericValueFunctions;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.ofbiz.core.entity.GenericValue;

/**
 * Add issue type icons as avatars. Complexity O(20).
 * <p/>
 * This upgrade task puts existing issue type icons to avatar table as issuetype system avatars - so they will be
 * available for selection to users. Existing database may contain some system issue type avatars or be partially
 * initialized - is such situation only missing avatars will be added.
 */
public class UpgradeTask_Build6322 extends AbstractUpgradeTask
{
    static final String DEFAULT_SUBTASK_AVATAR_FILENAME = "subtask.png";
    static final String DEFAULT_AVATAR_FILENAME = "genericissue.png";
    // avatars are in default order they will be visible in UI
    public static final List<String> ALL_ISSUE_TYPE_ICONS = ImmutableList.of(
            DEFAULT_AVATAR_FILENAME, "all_unassigned.png", "blank.png", "bug.png", "defect.png", "delete.png",
            "documentation.png", "epic.png", "exclamation.png", "health.png", "improvement.png", "newfeature.png",
            "remove_feature.png", "requirement.png", "sales.png", "story.png", DEFAULT_SUBTASK_AVATAR_FILENAME,
            "subtask_alternate.png", "task.png", "task_agile.png", "undefined.png"
    );


    private final OfBizDelegator ofBizDelegator;
    private final ApplicationProperties applicationProperties;

    public UpgradeTask_Build6322(final OfBizDelegator ofBizDelegator, final ApplicationProperties applicationProperties)
    {
        super(false);

        this.ofBizDelegator = ofBizDelegator;
        this.applicationProperties = applicationProperties;
    }

    @Override
    public String getBuildNumber()
    {
        return "6322";
    }

    @Override
    public String getShortDescription()
    {
        return "Add issue type icons as system avatars";
    }

    @Override
    public void doUpgrade(final boolean setupMode) throws Exception
    {
        final Iterable<String> nonExistingAvatarFilenames = Iterables.filter(
                ALL_ISSUE_TYPE_ICONS,
                removeExistingFilenamesFilter()
        );

        // Map<String, Object> - GenericValue stub (needed by ofBizDelegator)
        final Iterable<Map<String, Object>> newAvatarGvs = Iterables.transform(
                nonExistingAvatarFilenames,
                new AvatarEntityFromFilename()
        );

        for (Map<String, Object> avatarEntity : newAvatarGvs)
        {
            ofBizDelegator.createValue(CachingTaggingAvatarStore.AVATAR_ENTITY, avatarEntity);
        }

        storeDefaultIssueAvatarIds();
    }

    private void storeDefaultIssueAvatarIds()
    {
        long defaultAvatarId = getIdOfAvatar(DEFAULT_AVATAR_FILENAME);
        long subtaskDefaultAvatarId = getIdOfAvatar(DEFAULT_SUBTASK_AVATAR_FILENAME);

        applicationProperties.setString(
                APKeys.JIRA_DEFAULT_ISSUETYPE_AVATAR_ID,
                String.valueOf(defaultAvatarId));
        applicationProperties.setString(
                APKeys.JIRA_DEFAULT_ISSUETYPE_SUBTASK_AVATAR_ID,
                String.valueOf(subtaskDefaultAvatarId));
    }

    private Predicate<String> removeExistingFilenamesFilter()
    {
        final Set<String> existingAvatarPaths = findExistingSystemAvatarFileNames();

        return new Predicate<String>()
        {
            @Override
            public boolean apply(@Nullable final String filename)
            {
                final boolean filenameInDatabase = existingAvatarPaths.contains(filename);
                return !filenameInDatabase;
            }
        };
    }

    private long getIdOfAvatar(final String avatarFilename)
    {
        // i'm looking in database - default avatar may exist - be created in previous (i.e. broken) upgrade
        final ImmutableMap<String, Object> systemIssueTypeAvatarSearchCriteria = ImmutableMap.<String, Object>builder().
                put(CachingTaggingAvatarStore.SYSTEM_AVATAR, 1).
                put(CachingTaggingAvatarStore.AVATAR_TYPE, Avatar.Type.ISSUETYPE.getName()).
                put(CachingTaggingAvatarStore.FILE_NAME, avatarFilename).
                build();

        final List<GenericValue> defaultIssueTypeAvatar = ofBizDelegator.findByAnd(
                CachingTaggingAvatarStore.AVATAR_ENTITY,
                systemIssueTypeAvatarSearchCriteria);

        if (defaultIssueTypeAvatar.size() == 0)
        {
            throw new RuntimeException("Expected default avatar not found in database: " + systemIssueTypeAvatarSearchCriteria);
        }

        // if there is more i'm just picking first
        final GenericValue firstMatchingAvatar = defaultIssueTypeAvatar.get(0);
        return firstMatchingAvatar.getLong(CachingTaggingAvatarStore.ID);
    }

    private Set<String> findExistingSystemAvatarFileNames()
    {
        final ImmutableMap<String, Object> systemIssueTypeAvatarSearchCriteria = ImmutableMap.<String, Object>builder().
                put(CachingTaggingAvatarStore.SYSTEM_AVATAR, 1).
                put(CachingTaggingAvatarStore.AVATAR_TYPE, Avatar.Type.ISSUETYPE.getName()).
                build();

        final List<GenericValue> existingSystemAvatars = ofBizDelegator.findByAnd(
                CachingTaggingAvatarStore.AVATAR_ENTITY,
                systemIssueTypeAvatarSearchCriteria);

        final Function<GenericValue, String> extractItemFileName =
                GenericValueFunctions.getString(CachingTaggingAvatarStore.FILE_NAME);

        return Sets.newHashSet(Lists.transform(existingSystemAvatars, extractItemFileName));
    }

    private static class AvatarEntityFromFilename implements Function<String, Map<String, Object>>
    {

        @Override
        public Map<String, Object> apply(final String input)
        {
            final Map<String, Object> entity = ImmutableMap.<String, Object>builder().
                    put(CachingTaggingAvatarStore.FILE_NAME, input).
                    put(CachingTaggingAvatarStore.CONTENT_TYPE, "image/png").
                    put(CachingTaggingAvatarStore.AVATAR_TYPE, Avatar.Type.ISSUETYPE.getName()).
                    put(CachingTaggingAvatarStore.SYSTEM_AVATAR, 1).build();

            return entity;
        }

    }
}
