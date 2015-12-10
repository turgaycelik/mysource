package com.atlassian.jira.avatar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.atlassian.cache.Cache;
import com.atlassian.cache.CacheLoader;
import com.atlassian.cache.CacheManager;
import com.atlassian.jira.cluster.ClusterSafe;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.util.map.CacheObject;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

/**
 * Avatar store which converts legacy avatar files to the new tagged format during retrieval
 *
 * @since v6.2
 */
public class CachingTaggingAvatarStore implements AvatarStore
{
    private static final Logger log = Logger.getLogger(CachingTaggingAvatarStore.class);

    public static final String AVATAR_ENTITY = "Avatar";
    public static final String ID = "id";
    public static final String FILE_NAME = "fileName";
    public static final String CONTENT_TYPE = "contentType";
    public static final String AVATAR_TYPE = "avatarType";
    public static final String OWNER = "owner";
    public static final String SYSTEM_AVATAR = "systemAvatar";
    private static final String TAGGED_AVATAR_FILE_SUFFIX = "jrvtg.png";
    private static final int IS_SYSTEM = 1;
    private static final int NOT_SYSTEM = 0;

    private final Cache<Long, CacheObject<Avatar>> avatars;
    private final Cache<Long, CacheObject<Avatar>> taggedAvatars;

    private OfBizDelegator ofBizDelegator;
    private final AvatarTagger avatarTagger;

    // Because tagging an avatar file is too expensive when we're requesting multiple files in a short space of time
    // (e.g. user pickers) we provide a means of asking for an avatar either way, and a means of asking for an avatar
    // that must be tagged. Once an avatar is tagged, all subsequent requests for the avatar will get the tagged avatar.
    public CachingTaggingAvatarStore(final OfBizDelegator ofBizDelegator,
            final AvatarTagger avatarTagger, CacheManager cacheManager)
    {
        final String classPrefix = CachingTaggingAvatarStore.class.getName();
        this.taggedAvatars = cacheManager.getCache(classPrefix + ".taggedAvatars", new TagAndRetrieve());
        this.avatars = cacheManager.getCache(classPrefix + ".avatars", new CheckForTaggedVersionFallbackToDb());

        this.ofBizDelegator = ofBizDelegator;
        this.avatarTagger = avatarTagger;
    }

    private class CheckForTaggedVersionFallbackToDb implements CacheLoader<Long, CacheObject<Avatar>>
    {
        @ClusterSafe ("This use of Cache.getKeys() here is just a sort of optimisation and is safe.")
        @Override
        public CacheObject<Avatar> load(@Nullable Long id)
        {
            if (taggedAvatars.getKeys().contains(id))
            {
                return taggedAvatars.get(id);
            }
            else
            {
                final GenericValue gv = ofBizDelegator.findById(AVATAR_ENTITY, id);
                if (gv == null)
                {
                    return new CacheObject<Avatar>(null);
                }
                return new CacheObject<Avatar>(gvToAvatar(gv));
            }
        }
    }

    // By supplying the "tag and retrieve" action to a guava cache as a function, and keying off the id,
    // we prevent concurrent attempts to tag the file without having to write our own concurrency logic
    private class TagAndRetrieve implements CacheLoader<Long, CacheObject<Avatar>>
    {
        @Override
        public CacheObject<Avatar> load(@Nullable Long id)
        {
            final GenericValue gv = ofBizDelegator.findById(AVATAR_ENTITY, id);
            if (gv == null)
            {
                return new CacheObject<Avatar>(null);
            }
            tagLegacyAvatar(gv);
            return new CacheObject<Avatar>(gvToAvatar(gv));
        }
    }

    public Avatar getById(final Long avatarId)
    {
        return avatars.get(avatarId).getValue();
    }

    @Override
    public Avatar getByIdTagged(final Long avatarId)
    {
        Avatar taggedAvatar = taggedAvatars.get(avatarId).getValue();
        avatars.remove(avatarId);
        return taggedAvatar;
    }

    public boolean delete(final Long avatarId)
    {
        Assertions.notNull("avatarId", avatarId);
        try
        {
            int numRemoved = ofBizDelegator.removeByAnd(AVATAR_ENTITY, FieldMap.build(ID, avatarId));
            return numRemoved != 0;
        }
        finally
        {
            taggedAvatars.remove(avatarId);
            avatars.remove(avatarId);
        }
    }

    public void update(final Avatar avatar)
    {
        Assertions.notNull("avatar", avatar);
        Long avatarId = Assertions.notNull("avatar.id", avatar.getId());
        Assertions.notNull("avatar.fileName", avatar.getFileName());
        Assertions.notNull("avatar.contentType", avatar.getContentType());
        Assertions.notNull("avatar.avatarType", avatar.getAvatarType());
        Assertions.notNull("avatar.owner", avatar.getOwner());

        final GenericValue gv = ofBizDelegator.findById(AVATAR_ENTITY, avatarId);
        gv.setNonPKFields(getNonPkFields(avatar));
        try
        {
            gv.store();
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
        finally
        {
            taggedAvatars.remove(avatarId);
            avatars.remove(avatarId);
        }
    }

    public Avatar create(final Avatar avatar)
    {
        Assertions.notNull("avatar", avatar);
        Assertions.stateTrue("avatar.id must be null", avatar.getId() == null);
        Assertions.notNull("avatar.fileName", avatar.getFileName());
        Assertions.notNull("avatar.contentType", avatar.getContentType());
        Assertions.notNull("avatar.avatarType", avatar.getAvatarType());

        return gvToAvatar(ofBizDelegator.createValue(AVATAR_ENTITY, getNonPkFields(avatar)));
    }

    public List<Avatar> getAllSystemAvatars(final Avatar.Type type)
    {
        return getAvatars(FieldMap.build(SYSTEM_AVATAR, IS_SYSTEM, AVATAR_TYPE, type.getName()));
    }

    public List<Avatar> getCustomAvatarsForOwner(final Avatar.Type type, final String ownerId)
    {
        Assertions.notNull("type", type);
        Assertions.notNull("ownerId", ownerId);

        return getAvatars(FieldMap.build(SYSTEM_AVATAR, NOT_SYSTEM, AVATAR_TYPE, type.getName(), OWNER, ownerId));
    }

    List<Avatar> getAvatars(final Map<String, ?> constraint)
    {
        ArrayList<Avatar> systemAvatars = new ArrayList<Avatar>();
        for (GenericValue gv : ofBizDelegator.findByAnd(AVATAR_ENTITY, constraint))
        {
            systemAvatars.add(gvToAvatar(gv));
        }
        return systemAvatars;
    }

    private Map<String, Object> getNonPkFields(Avatar avatar)
    {
        Map<String, Object> fields = new HashMap<String, Object>();
        fields.put(FILE_NAME, avatar.getFileName());
        fields.put(CONTENT_TYPE, avatar.getContentType());
        fields.put(AVATAR_TYPE, avatar.getAvatarType().getName());
        fields.put(OWNER, avatar.getOwner());
        fields.put(SYSTEM_AVATAR, avatar.isSystemAvatar() ? IS_SYSTEM : NOT_SYSTEM);
        return fields;
    }

    Avatar gvToAvatar(final GenericValue gv)
    {
        return new AvatarImpl(gv.getLong(ID),
                gv.getString(FILE_NAME),
                gv.getString(CONTENT_TYPE),
                Avatar.Type.getByName(gv.getString(AVATAR_TYPE)),
                gv.getString(OWNER),
                gv.getInteger(SYSTEM_AVATAR) != 0);
    }

    void tagLegacyAvatar(GenericValue gv)
    {
        final String fileName = gv.getString(FILE_NAME);
        final Long id = gv.getLong(ID);
        final Integer isSystem = gv.getInteger(SYSTEM_AVATAR);
        final String avatarType = gv.getString(AVATAR_TYPE);

        if (isSystem.equals(IS_SYSTEM) ||
                !avatarType.equals(Avatar.Type.USER.getName()) ||
                fileName.endsWith(TAGGED_AVATAR_FILE_SUFFIX))
        {
            return;
        }

        try
        {
            String newFileName = avatarTagger.tagAvatar(id, fileName);
            gv.setString(FILE_NAME, newFileName);
            gv.setString(CONTENT_TYPE, "image/png");
        }
        catch (IOException e)
        {
            final StringBuilder sb = new StringBuilder();
            sb.append("Could not convert avatar ").append(fileName).append(" to new format with metadata.");
            sb.append("\nThis avatar may be deleted during an upgrade to the next major version of JIRA.");
            sb.append("\nAlso, if this avatar is embedded in reply emails picked up by the JIRA email handler, ");
            sb.append("the handler may attach the avatar file to the associated issue");
            log.warn(sb.toString());
            return;
        }
        try
        {
            gv.store();
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }
}
