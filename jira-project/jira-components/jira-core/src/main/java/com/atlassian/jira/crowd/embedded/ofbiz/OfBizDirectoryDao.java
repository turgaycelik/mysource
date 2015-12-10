package com.atlassian.jira.crowd.embedded.ofbiz;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.atlassian.cache.CacheManager;
import com.atlassian.cache.CachedReference;
import com.atlassian.cache.Supplier;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.OperationType;
import com.atlassian.crowd.embedded.spi.DirectoryDao;
import com.atlassian.crowd.exception.DirectoryNotFoundException;
import com.atlassian.crowd.exception.runtime.OperationFailedException;
import com.atlassian.crowd.model.directory.DirectoryImpl;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.atlassian.crowd.search.query.entity.restriction.MatchMode;
import com.atlassian.crowd.search.query.entity.restriction.NullRestriction;
import com.atlassian.crowd.search.query.entity.restriction.Property;
import com.atlassian.crowd.search.query.entity.restriction.TermRestriction;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.ofbiz.OfBizDelegator;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;

import org.ofbiz.core.entity.EntityUtil;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.jira.crowd.embedded.ofbiz.DirectoryEntity.getData;
import static com.atlassian.jira.crowd.embedded.ofbiz.OfBizDirectory.from;
import static com.atlassian.jira.crowd.embedded.ofbiz.PrimitiveMap.caseInsensitive;
import static com.atlassian.jira.crowd.embedded.ofbiz.PrimitiveMap.of;

public class OfBizDirectoryDao implements DirectoryDao
{
    @VisibleForTesting
    private final OfBizDelegator ofBizDelegator;

    private final CachedReference<List<Directory>> directoryCache;

    public OfBizDirectoryDao(final OfBizDelegator ofBizDelegator, final CacheManager cacheManager)
    {
        this.ofBizDelegator = ofBizDelegator;
        directoryCache = buildReference(cacheManager);
    }

    private CachedReference<List<Directory>> buildReference(final CacheManager cacheManager)
    {
        return cacheManager.getCachedReference(OfBizDirectoryDao.class,  "directoryCache",
                new Supplier<List<Directory>>()
                {
                    @Override
                    public List<Directory> get()
                    {
                        // This is already an Immutable List
                        return getAllDirectories();
                    }
                });
    }

    public Directory findById(final long id) throws DirectoryNotFoundException
    {
        for (Directory directory : directoryCache.get())
        {
            if (directory.getId().longValue() == id)
            {
                return directory;
            }
        }
        throw new DirectoryNotFoundException(id);
    }

    public Directory findByName(final String name) throws DirectoryNotFoundException
    {
        for (Directory directory : directoryCache.get())
        {
            if (directory.getName().equalsIgnoreCase(name))
            {
                return directory;
            }
        }
        throw new DirectoryNotFoundException(name);
    }

    public List<Directory> findAll()
    {
        return directoryCache.get();
    }

    private Directory buildDirectory(final GenericValue directoryGenericValue)
    {
        final List<GenericValue> attributesGenericValues = findAttributesGenericValues(directoryGenericValue.getLong(DirectoryEntity.DIRECTORY_ID));
        final List<GenericValue> operationGenericValues = findOperations(directoryGenericValue.getLong(DirectoryEntity.DIRECTORY_ID));
        return from(directoryGenericValue, attributesGenericValues, operationGenericValues);
    }

    private GenericValue findDirectoryByName(final String name) throws DirectoryNotFoundException
    {
        try
        {
            final GenericValue directoryGenericValue = EntityUtil.getOnly(findDirectories(caseInsensitive(DirectoryEntity.LOWER_NAME, name)));
            if (directoryGenericValue != null)
            {
                return directoryGenericValue;
            }
            else
            {
                throw new DirectoryNotFoundException(name);
            }
        }
        catch (final GenericEntityException e)
        {
            throw new DirectoryNotFoundException(name, e);
        }
    }

    @SuppressWarnings("unchecked")
    private List<GenericValue> findAttributesGenericValues(final long directoryId)
    {
        return ofBizDelegator.findByAnd(DirectoryAttributeEntity.ENTITY, of(DirectoryAttributeEntity.DIRECTORY_ID, directoryId));
    }

    @SuppressWarnings("unchecked")
    private List<GenericValue> findOperations(final long directoryId)
    {
        return ofBizDelegator.findByAnd(DirectoryOperationEntity.ENTITY, of(DirectoryAttributeEntity.DIRECTORY_ID, directoryId));
    }

    private GenericValue findDirectoryById(final Long id) throws DirectoryNotFoundException
    {
        try
        {
            final GenericValue directoryGenericValue = EntityUtil.getOnly(findDirectories(of(DirectoryEntity.DIRECTORY_ID, id)));
            if (directoryGenericValue != null)
            {
                return directoryGenericValue;
            }
            else
            {
                throw new DirectoryNotFoundException(id);
            }
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private List<GenericValue> findDirectories(final Map<String, Object> filter) throws GenericEntityException
    {
        return ofBizDelegator.findByAnd(DirectoryEntity.ENTITY, filter);
    }

    public synchronized Directory add(final Directory directory)
    {
        final long directoryId;
        try
        {
            DirectoryImpl directoryToSave = new DirectoryImpl(directory);
            directoryToSave.setCreatedDateToNow();
            directoryToSave.setUpdatedDateToNow();

            final Map<String, Object> map = getData(directoryToSave);

            // Create and store the directory
            final GenericValue directoryGenericValue = ofBizDelegator.createValue(DirectoryEntity.ENTITY, map);

            directoryId = directoryGenericValue.getLong(DirectoryEntity.DIRECTORY_ID);

            for (final Map.Entry<String, String> entry : directory.getAttributes().entrySet())
            {
                final GenericValue genericValue = ofBizDelegator.makeValue(DirectoryAttributeEntity.ENTITY, DirectoryAttributeEntity.getData(directoryId,
                    entry.getKey(), entry.getValue()));
                genericValue.create();
            }

            for (final OperationType operationType : directory.getAllowedOperations())
            {
                final GenericValue genericValue = ofBizDelegator.makeValue(DirectoryOperationEntity.ENTITY, DirectoryOperationEntity.getData(directoryId,
                    operationType));
                genericValue.create();
            }
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }

        // JRA-32558 - We just created this directory.  Make doubly sure it is empty.
        removeDirectoryContents(directoryId);

        // Rebuild the full cache, we need this now, so the resequeence works properly.
        flushCache();

        // Force the directory to the last position
        try
        {
            updateDirectoryPosition(directoryId, directoryCache.get().size());
        }
        catch (DirectoryNotFoundException e)
        {
            // Very unlikely unless a very unlucky race condition.
            throw new OperationFailedException(e);
        }

        // Find the saved copy of the Directory
        try
        {
            return findById(directoryId);
        }
        catch (DirectoryNotFoundException e)
        {
            // Very unlikely unless a very unlucky race condition.
            throw new OperationFailedException(e);
        }
    }

    public synchronized Directory update(final Directory directory) throws DirectoryNotFoundException
    {
        final GenericValue gv = DirectoryEntity.setData(directory, findDirectoryById(directory.getId()));
        gv.set(DirectoryEntity.UPDATED_DATE, new Timestamp(System.currentTimeMillis()));
        storeDirectory(gv);
        storeAttributes(directory);
        storeOperations(directory);
        // Rebuild the full cache (this would be very rare)
        directoryCache.reset();
        // Find the latest copy of the Directory
        return findById(directory.getId());
    }

    private void storeAttributes(final Directory directory)
    {
        final List<GenericValue> attributeGenericValues = new ArrayList<GenericValue>();
        for (final Map.Entry<String, String> entry : directory.getAttributes().entrySet())
        {
            attributeGenericValues.add(ofBizDelegator.makeValue(DirectoryAttributeEntity.ENTITY, DirectoryAttributeEntity.getData(directory.getId(),
                    entry.getKey(), entry.getValue())));
        }
        ofBizDelegator.removeByAnd(DirectoryAttributeEntity.ENTITY, of(DirectoryAttributeEntity.DIRECTORY_ID, directory.getId()));
        ofBizDelegator.storeAll(attributeGenericValues);
    }

    private void storeOperations(final Directory directory)
    {
        final List<GenericValue> operationGenericValues = new ArrayList<GenericValue>();
        for (final OperationType operationType : directory.getAllowedOperations())
        {
            operationGenericValues.add(ofBizDelegator.makeValue(DirectoryOperationEntity.ENTITY, DirectoryOperationEntity.getData(directory.getId(),
                    operationType)));
        }

        ofBizDelegator.removeByAnd(DirectoryOperationEntity.ENTITY, of(DirectoryOperationEntity.DIRECTORY_ID, directory.getId()));
        ofBizDelegator.storeAll(operationGenericValues);
    }

    private GenericValue storeDirectory(final GenericValue directoryGenericValue)
    {
        ofBizDelegator.store(directoryGenericValue);
        return directoryGenericValue;
    }

    public synchronized void remove(final Directory directory) throws DirectoryNotFoundException
    {
        final long directoryId = directory.getId();
        findDirectoryById(directoryId);  // To trigger DirectoryNotFoundException
        removeDirectoryContents(directory.getId());

        // Remove Directory Attributes, Operations, and then finally the Directory itself
        ofBizDelegator.removeByAnd(DirectoryAttributeEntity.ENTITY, of(DirectoryAttributeEntity.DIRECTORY_ID, directoryId));
        ofBizDelegator.removeByAnd(DirectoryOperationEntity.ENTITY, of(DirectoryOperationEntity.DIRECTORY_ID, directoryId));
        ofBizDelegator.removeByAnd(DirectoryEntity.ENTITY, of(DirectoryEntity.DIRECTORY_ID, directoryId));

        // rebuild the full cache
        directoryCache.reset();
    }


    /**
     * Removes all dependent content from this directory to make sure that it is clean.
     * <em>WARNING</em>: The caller is responsible for obtaining this object's lock and
     * for calling {@link #flushCache()} afterwards.
     *
     * @param directoryId the directory from which to purge all existing users, groups,
     *          and memberships
     */
    private void removeDirectoryContents(final long directoryId)
    {
        // Remove Memberships
        ofBizDelegator.removeByAnd(MembershipEntity.ENTITY, of(MembershipEntity.DIRECTORY_ID, directoryId));

        // Remove User Attributes and Users
        ofBizDelegator.removeByAnd(UserAttributeEntity.ENTITY, of(UserAttributeEntity.DIRECTORY_ID, directoryId));
        ofBizDelegator.removeByAnd(UserEntity.ENTITY, of(UserEntity.DIRECTORY_ID, directoryId));

        // Remove Group Attributes and Groups
        ofBizDelegator.removeByAnd(GroupAttributeEntity.ENTITY, of(GroupAttributeEntity.DIRECTORY_ID, directoryId));
        ofBizDelegator.removeByAnd(GroupEntity.ENTITY, of(GroupEntity.DIRECTORY_ID, directoryId));

    }

    public List<Directory> search(final EntityQuery<Directory> query)
    {
        // JIRA does not support anything but an empty Query
        if (query == null || query.getSearchRestriction() == null || query.getSearchRestriction() instanceof NullRestriction)
        {
            return directoryCache.get();
        }
        if (query.getSearchRestriction() instanceof TermRestriction)
        {
            final TermRestriction termRestriction = (TermRestriction) query.getSearchRestriction();
            final Property property = termRestriction.getProperty();

            if (!property.getPropertyName().equals("name"))
            {
                throw new UnsupportedOperationException("Searching on '" + property.getPropertyName() + "' not supported.");
            }
            final MatchMode matchMode = termRestriction.getMatchMode();
            switch (matchMode)
            {
                case EXACTLY_MATCHES:
                    return searchByName((String) termRestriction.getValue());
                default:
                    throw new UnsupportedOperationException("Unsupported MatchMode " + matchMode);
            }
        }
        throw new UnsupportedOperationException("Complex Directory searching is not supported.");
    }

    private List<Directory> searchByName(final String value)
    {
        final List<Directory> results = new ArrayList<Directory>(1);
        for (Directory directory : directoryCache.get())
        {
            if (directory.getName().equals(value))
            {
                results.add(directory);
            }
        }
        return results;
    }

    private List<Directory> getAllDirectories()
    {
        final ImmutableList.Builder<Directory> directories = ImmutableList.builder();
        @SuppressWarnings("unchecked")
        final List<GenericValue> directoryGenericValues = ofBizDelegator.findByAnd(DirectoryEntity.ENTITY, Collections.EMPTY_MAP, Collections.singletonList("position"));

        for (final GenericValue directoryGenericValue : directoryGenericValues)
        {
            directories.add(buildDirectory(directoryGenericValue));
        }
        return directories.build();
    }

    /**
     * Invoked by {@link OfBizCacheFlushingManager} to ensure caches are being flushed in the right order on
     * {@link com.atlassian.crowd.event.migration.XMLRestoreFinishedEvent}
     */
    public synchronized void flushCache()
    {
        directoryCache.reset();
    }

    public void updateDirectoryPosition(long directoryId, int position) throws DirectoryNotFoundException
    {
        List<Directory> directories = new ArrayList<Directory>(findAll());
        int currentPos = getCurrentDirectoryPosition(directories, directoryId);
        if (currentPos == -1)
        {
            throw new IllegalArgumentException("Directory to set position of does not exist");
        }
        if (position != currentPos)
        {
            Directory directory = directories.remove(currentPos);
            if (position < 0)
            {
                position = 0;
            }
            else if (position > directories.size())
            {
                position = directories.size();
            }
            directories.add(position, directory);
            resequenceDirectories(directories);
        }
        else
        {
            // Position = current position, nothing to do
        }
        flushCache();
    }

    /**
     * Get the current position of the directory in the list of directories.
     * @param directories List of all directories
     * @param directoryId Directory to find in the list
     *
     * @return position in the directory list, zero based.
     */
    private int getCurrentDirectoryPosition(List<Directory> directories, long directoryId)
    {
        for (int i = 0; i < directories.size(); i++)
        {
            if (directories.get(i).getId().equals(directoryId))
            {
                return i;
            }
        }
        return -1;
    }

    private void resequenceDirectories(List<Directory> directories) throws DirectoryNotFoundException
    {
        long i = 0;
        for (Directory directory : directories)
        {
            storeDirectoryPosition(directory.getId(), i);
            i++;
        }
    }

    private void storeDirectoryPosition(final Long id, final long position) throws DirectoryNotFoundException
    {
        final GenericValue gv = findDirectoryById(id);
        gv.set("position", position);
        storeDirectory(gv);
    }


}
