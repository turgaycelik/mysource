package com.atlassian.jira.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.annotation.Nullable;

import com.atlassian.cache.CacheManager;
import com.atlassian.cache.CachedReference;
import com.atlassian.cache.Supplier;
import com.atlassian.event.api.EventListener;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.entity.EntityUtils;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.issue.TextIssueConstant;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.issuetype.IssueTypeImpl;
import com.atlassian.jira.issue.priority.Priority;
import com.atlassian.jira.issue.resolution.Resolution;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.collect.MapBuilder;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.EntityUtil;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.length;

@EventComponent
public class DefaultConstantsManager implements ConstantsManager
{
    private static final Logger log = Logger.getLogger(DefaultConstantsManager.class);
    private static final List<String> DEFAULT_CONSTANTS_SORT = Collections.singletonList("sequence ASC");
    private static final List<String> DEFAULT_ISSUE_TYPE_SORT = ImmutableList.of("style", "name");

    private CachedReference<ConstantsCache<Priority>> priorityCache;
    private CachedReference<ConstantsCache<Resolution>> resolutionCache;
    private CachedReference<ConstantsCache<Status>> statusCache;
    private CachedReference<IssueTypeCache> issueTypeCache;

    private final IssueConstant UNRESOLVED_RESOLUTION;

    private final JiraAuthenticationContext authenticationContext;
    private final OfBizDelegator ofBizDelegator;
    private final IssueConstantFactory issueConstantFactory;

    public DefaultConstantsManager(final JiraAuthenticationContext authenticationContext,
            final OfBizDelegator ofBizDelegator, final IssueConstantFactory issueConstantFactory, final CacheManager cacheManager)
    {
        this.issueConstantFactory = issueConstantFactory;
        this.authenticationContext = authenticationContext;
        this.ofBizDelegator = ofBizDelegator;
        UNRESOLVED_RESOLUTION = new TextIssueConstant("common.status.unresolved", "common.status.unresolved", null, authenticationContext);

        createCachedReferences(cacheManager);
    }

    private void createCachedReferences(final CacheManager cacheManager)
    {
        priorityCache = cacheManager.getCachedReference(DefaultConstantsManager.class,  "priorityCache", new PriorityCacheLoader());
        resolutionCache = cacheManager.getCachedReference(DefaultConstantsManager.class,  "resolutionCache", new ResolutionCacheLoader());
        statusCache = cacheManager.getCachedReference(DefaultConstantsManager.class,  "statusCache", new StatusCacheLoader());
        issueTypeCache = cacheManager.getCachedReference(DefaultConstantsManager.class,  "issueTypeCache", new IssueTypeCacheLoader());
    }

    @SuppressWarnings ({ "UnusedDeclaration" })
    @EventListener
    public void onClearCache(ClearCacheEvent ignored)
    {
        refresh();
    }

    public Collection<GenericValue> getStatuses()
    {
        return statusCache.get().getGenericValues();
    }

    public Collection<Status> getStatusObjects()
    {
        return statusCache.get().getObjects();
    }

    public GenericValue getStatus(String id)
    {
        return convertToGenericValue(getStatusObject(id));
    }

    public Status getStatusObject(String id)
    {
        return statusCache.get().getObject(id);
    }

    public void refreshStatuses()
    {
        statusCache.reset();
    }

    public IssueConstant getConstantObject(String constantType, String id)
    {
        if (PRIORITY_CONSTANT_TYPE.equalsIgnoreCase(constantType))
        {
            return getPriorityObject(id);
        }
        else if (STATUS_CONSTANT_TYPE.equalsIgnoreCase(constantType))
        {
            return getStatusObject(id);
        }
        else if (RESOLUTION_CONSTANT_TYPE.equalsIgnoreCase(constantType))
        {
            return getResolutionObject(id);
        }
        else if (ISSUE_TYPE_CONSTANT_TYPE.equalsIgnoreCase(constantType))
        {
            return getIssueTypeObject(id);
        }

        return null;
    }

    public Collection getConstantObjects(String constantType)
    {
        if (PRIORITY_CONSTANT_TYPE.equalsIgnoreCase(constantType))
        {
            return getPriorityObjects();
        }
        else if (STATUS_CONSTANT_TYPE.equalsIgnoreCase(constantType))
        {
            return getStatusObjects();
        }
        else if (RESOLUTION_CONSTANT_TYPE.equalsIgnoreCase(constantType))
        {
            return getResolutionObjects();
        }
        else if (ISSUE_TYPE_CONSTANT_TYPE.equalsIgnoreCase(constantType))
        {
            return getAllIssueTypeObjects();
        }

        return null;
    }

    public List<IssueConstant> convertToConstantObjects(String constantType, Collection ids)
    {
        if (ids != null && !ids.isEmpty() && constantType != null)
        {
            List<IssueConstant> list = new ArrayList<IssueConstant>(ids.size());
            for (Object o : ids)
            {
                String id;
                if (o instanceof GenericValue)
                {
                    id = ((GenericValue) o).getString("id");
                }
                else
                {
                    id = (String) o;
                }

                IssueConstant constant = null;
                if (PRIORITY_CONSTANT_TYPE.equalsIgnoreCase(constantType))
                {
                    constant = getPriorityObject(id);
                }
                else if (STATUS_CONSTANT_TYPE.equalsIgnoreCase(constantType))
                {
                    constant = getStatusObject(id);
                }
                else if (RESOLUTION_CONSTANT_TYPE.equalsIgnoreCase(constantType))
                {
                    if ("-1".equals(id))
                    {
                        constant = UNRESOLVED_RESOLUTION;
                    }
                    else
                    {
                        constant = getResolutionObject(id);
                    }
                }
                else if (ISSUE_TYPE_CONSTANT_TYPE.equalsIgnoreCase(constantType))
                {
                    constant = getIssueTypeObject(id);
                }

                if (constant != null)
                {
                    list.add(constant);
                }
                else
                {
                    log.warn(id + " returned a null constant of type " + constantType);
                }
            }
            return list;
        }
        else
        {
            return null;
        }
    }

    public boolean constantExists(String constantType, String name)
    {
        return (getIssueConstantByName(constantType, name) != null);
    }

    public IssueConstant getIssueConstantByName(String constantType, String name)
    {
        ConstantsCache<? extends IssueConstant> constantsCache = getConstantsCache(constantType);

        for (IssueConstant issueConstant : constantsCache.getObjects())
        {
            // TODO: Do we really allow null names?
            if (name == null)
            {
                if (issueConstant.getName() == null)
                    return issueConstant;
            }
            else
            {
                if (name.equals(issueConstant.getName()))
                {
                    return issueConstant;
                }
            }
        }
        // Not Found
        return null;
    }

    public GenericValue getConstantByName(String constantType, String name)
    {
        return convertToGenericValue(getIssueConstantByName(constantType, name));
    }

    private GenericValue convertToGenericValue(final IssueConstant issueConstant)
    {
        if (issueConstant == null)
        {
            return null;
        }
        else
        {
            return issueConstant.getGenericValue();
        }
    }

    public IssueConstant getConstantByNameIgnoreCase(final String constantType, final String name)
    {
        ConstantsCache<? extends IssueConstant> constantsCache = getConstantsCache(constantType);

        for (IssueConstant issueConstant : constantsCache.getObjects())
        {
            // TODO: Do we really allow null names?
            if (name == null)
            {
                if (issueConstant.getName() == null)
                    return issueConstant;
            }
            else
            {
                if (name.equalsIgnoreCase(issueConstant.getName()))
                {
                    return issueConstant;
                }
            }
        }
        // Not Found
        return null;
    }

    private ConstantsCache<? extends IssueConstant> getConstantsCache(final String constantType)
    {
        if (PRIORITY_CONSTANT_TYPE.equalsIgnoreCase(constantType))
        {
            return priorityCache.get();
        }
        else if (STATUS_CONSTANT_TYPE.equalsIgnoreCase(constantType))
        {
            return statusCache.get();
        }
        else if (RESOLUTION_CONSTANT_TYPE.equalsIgnoreCase(constantType))
        {
            return resolutionCache.get();
        }
        else if (ISSUE_TYPE_CONSTANT_TYPE.equalsIgnoreCase(constantType))
        {
            return issueTypeCache.get();
        }

        throw new IllegalArgumentException("Unknown constant type '" + constantType + "'.");
    }

    public GenericValue createIssueType(String name, Long sequence, String style, String description, String iconurl) throws CreateException
    {
        GenericValue createdIssueType;
        try
        {
            Map<String, Object> fields = MapBuilder.<String, Object>newBuilder()
                    .add("id", ofBizDelegator.getDelegatorInterface().getNextSeqId(ConstantsManager.ISSUE_TYPE_CONSTANT_TYPE).toString())
                    .add("name", name)
                    .add("sequence", sequence)
                    .add("style", StringUtils.trimToNull(style))
                    .add("description", description)
                    .add("iconurl", iconurl).toMap();
            createdIssueType = EntityUtils.createValue(ISSUE_TYPE_CONSTANT_TYPE, fields);
        }
        catch (DataAccessException e)
        {
            throw new CreateException("Error occurred while creating issue type with name='" + name + "' sequence='" + sequence +
                    "' style='" + style + "' description='" + description + "' iconurl='" + iconurl + "'.", e);
        }
        refreshIssueTypes();
        return createdIssueType;
    }

    private GenericValue createIssueType(final String name, final Long sequence, final String style, final String description, final Long avatarId)
            throws CreateException
    {
        GenericValue createdIssueType;
        try
        {
            Map<String, Object> fields = MapBuilder.<String, Object>newBuilder()
                    .add("id", ofBizDelegator.getDelegatorInterface().getNextSeqId(ConstantsManager.ISSUE_TYPE_CONSTANT_TYPE).toString())
                    .add("name", name)
                    .add("sequence", sequence)
                    .add("style", StringUtils.trimToNull(style))
                    .add("description", description)
                    .add(IssueTypeImpl.AVATAR_FIELD, avatarId).toMap();
            createdIssueType = EntityUtils.createValue(ISSUE_TYPE_CONSTANT_TYPE, fields);
        }
        catch (DataAccessException e)
        {
            throw new CreateException("Error occurred while creating issue type with name='" + name + "' sequence='" + sequence +
                    "' style='" + style + "' description='" + description + "' avatarId='" + avatarId + "'.", e);
        }
        refreshIssueTypes();
        return createdIssueType;
    }

    @Override
    public IssueType insertIssueType(String name, Long sequence, String style, String description, String iconurl) throws CreateException
    {
        GenericValue createdIssueType = createIssueType(name, sequence, style, description, iconurl);
        return issueConstantFactory.createIssueType(createdIssueType);
    }

    @Override
    public IssueType insertIssueType(String name, Long sequence, String style, String description, Long avatarId) throws CreateException
    {
        GenericValue createdIssueType = createIssueType(name, sequence, style, description, avatarId);
        return issueConstantFactory.createIssueType(createdIssueType);
    }

    public void validateCreateIssueType(String name, String style, String description, String iconurl, ErrorCollection errors, String nameFieldName)
    {
        if (isBlank(name))
        {
            errors.addError(nameFieldName, authenticationContext.getI18nHelper().getText("admin.errors.must.specify.name"));
        }
        else if(length(name) > 60)
        {
            errors.addError(nameFieldName, authenticationContext.getI18nHelper().getText("admin.errors.issuetypes.name.must.not.exceed.max.length"));
        }
        else
        {
            for (final IssueType issueType : getAllIssueTypeObjects())
            {
                if (name.trim().equalsIgnoreCase(issueType.getName()))
                {
                    errors.addError(nameFieldName, authenticationContext.getI18nHelper().getText("admin.errors.issue.type.with.this.name.already.exists"));
                    break;
                }
            }
        }

        if (isBlank(iconurl))
        {
            errors.addError("iconurl", authenticationContext.getI18nHelper().getText("admin.errors.must.specify.url.for.issue.type"));
        }
    }

    public void validateCreateIssueTypeWithAvatar(String name, String style, String description, String avatarId, ErrorCollection errors, String nameFieldName)
    {
        if (isBlank(name))
        {
            errors.addError(nameFieldName, authenticationContext.getI18nHelper().getText("admin.errors.must.specify.name"));
        }
        else if(length(name) > 60)
        {
            errors.addError(nameFieldName, authenticationContext.getI18nHelper().getText("admin.errors.issuetypes.name.must.not.exceed.max.length"));
        }
        else
        {
            for (final IssueType issueType : getAllIssueTypeObjects())
            {
                if (name.trim().equalsIgnoreCase(issueType.getName()))
                {
                    errors.addError(nameFieldName, authenticationContext.getI18nHelper().getText("admin.errors.issue.type.with.this.name.already.exists"));
                    break;
                }
            }
        }

        if (isBlank(avatarId))
        {
            errors.addError("avatarId", authenticationContext.getI18nHelper().getText("admin.errors.issue.type.must.specify.avatar"));
        } else {
            try
            {
                Long.valueOf(avatarId);
            }
            catch (NumberFormatException e)
            {
                errors.addError("avatarId", authenticationContext.getI18nHelper().getText("admin.errors.issue.type.must.specify.avatar"));
            }
        }
    }

    public void updateIssueType(String id, String name, Long sequence, String style, String description, String iconurl) throws DataAccessException
    {
        if (id == null)
        {
            throw new IllegalArgumentException("Id cannot be null.");
        }

        try
        {
            // Get the value from the database so that we do not modify the cached value directly
            final GenericValue issueTypeGV = EntityUtil.getOnly(ofBizDelegator.findByAnd(ISSUE_TYPE_CONSTANT_TYPE, new FieldMap("id", id)));

            if (issueTypeGV == null)
            {
                throw new IllegalArgumentException("Issue Type with id '" + id + "' does not exist.");
            }

            issueTypeGV.set("name", name);
            issueTypeGV.set("sequence", sequence);
            issueTypeGV.set("style", style);
            issueTypeGV.set("description", description);
            issueTypeGV.set("iconurl", iconurl);
            issueTypeGV.store();
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException("Error occurred while updating issue type with id '" + id + "'.");
        }
        refreshIssueTypes();
    }

    public void updateIssueType(String id, String name, Long sequence, String style, String description, Long avatarId) throws DataAccessException
    {
        if (id == null)
        {
            throw new IllegalArgumentException("Id cannot be null.");
        }

        try
        {
            // Get the value from the database so that we do not modify the cached value directly
            final GenericValue issueTypeGV = EntityUtil.getOnly(ofBizDelegator.findByAnd(ISSUE_TYPE_CONSTANT_TYPE, new FieldMap("id", id)));

            if (issueTypeGV == null)
            {
                throw new IllegalArgumentException("Issue Type with id '" + id + "' does not exist.");
            }

            issueTypeGV.set("name", name);
            issueTypeGV.set("sequence", sequence);
            issueTypeGV.set("style", style);
            issueTypeGV.set("description", description);
            issueTypeGV.set(IssueTypeImpl.AVATAR_FIELD, avatarId);
            issueTypeGV.store();
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException("Error occurred while updating issue type with id '" + id + "'.");
        }
        refreshIssueTypes();
    }

    public void removeIssueType(String id) throws RemoveException
    {
        final GenericValue issueTypeGV = getIssueType(id);
        if (issueTypeGV != null)
        {
            try
            {
                issueTypeGV.remove();
                refreshIssueTypes();
            }
            catch (GenericEntityException e)
            {
                throw new RemoveException("Error occurred while removing issute type with id '" + id + "'.", e);
            }
        }
        else
        {
            throw new RemoveException("Issue type with id '" + id + "' does not exist.");
        }

    }

    public void storeIssueTypes(List<GenericValue> issueTypes) throws DataAccessException
    {
        try
        {
            ofBizDelegator.storeAll(issueTypes);
        }
        catch (DataAccessException e)
        {
            throw new DataAccessException("Error occurred while storing issue types to the database.", e);
        }

        refreshIssueTypes();
    }

    public void refresh()
    {
        invalidateAll();
    }

    public void invalidateAll()
    {
        // Do not load the defaults set all caches to uninitialised
        priorityCache.reset();
        resolutionCache.reset();
        issueTypeCache.reset();
        statusCache.reset();
    }

    public void invalidate(IssueConstant constant)
    {
        // Do not load the defaults set relevant cache to uninitialised
        if (constant instanceof Priority)
        {
            priorityCache.reset();
        }
        else if (constant instanceof Resolution)
        {
            resolutionCache.reset();
        }
        else if (constant instanceof IssueType)
        {
            issueTypeCache.reset();
        }
        else if (constant instanceof Status)
        {
            statusCache.reset();
        }
    }

    public List<String> expandIssueTypeIds(Collection<String> issueTypeIds)
    {
        if (issueTypeIds == null)
        {
            return Collections.emptyList();
        }

        for (final String issueTypeId : issueTypeIds)
        {
            if (ALL_STANDARD_ISSUE_TYPES.equals(issueTypeId))
            {
                return getIds(getIssueTypes());
            }
            else if (ALL_SUB_TASK_ISSUE_TYPES.equals(issueTypeId))
            {
                return getIds(getSubTaskIssueTypes());
            }
            else if (ALL_ISSUE_TYPES.equals(issueTypeId))
            {
                return getAllIssueTypeIds();
            }
        }

        return new ArrayList<String>(issueTypeIds);
    }

    public List<String> getAllIssueTypeIds()
    {
        return issueTypeCache.get().getCachedIds();
    }

    public IssueConstant getIssueConstant(GenericValue issueConstantGV)
    {
        if (issueConstantGV == null)
        {
            return null;
        }

        if (ISSUE_TYPE_CONSTANT_TYPE.equals(issueConstantGV.getEntityName()))
        {
            return getIssueTypeObject(issueConstantGV.getString("id"));
        }
        else if (STATUS_CONSTANT_TYPE.equals(issueConstantGV.getEntityName()))
        {
            return getStatusObject(issueConstantGV.getString("id"));
        }
        else if (PRIORITY_CONSTANT_TYPE.equals(issueConstantGV.getEntityName()))
        {
            return getPriorityObject(issueConstantGV.getString("id"));
        }
        else if (RESOLUTION_CONSTANT_TYPE.equals(issueConstantGV.getEntityName()))
        {
            return getResolutionObject(issueConstantGV.getString("id"));
        }

        throw new IllegalArgumentException("Unknown constant entity name '" + issueConstantGV.getEntityName() + "'.");
    }

    private static List<String> getIds(Collection<GenericValue> issueTypes)
    {
        List<String> ids = new ArrayList<String>(issueTypes.size());
        for (final GenericValue issueTypeGV : issueTypes)
        {
            ids.add(issueTypeGV.getString("id"));
        }
        return ids;
    }

    public Collection<GenericValue> getPriorities()
    {
        return priorityCache.get().getGenericValues();
    }

    public Collection<Priority> getPriorityObjects()
    {
        return priorityCache.get().getObjects();
    }

    public Priority getPriorityObject(String id)
    {
        return priorityCache.get().getObject(id);
    }

    public Priority getDefaultPriorityObject()
    {
        final String defaultPriorityId = (ComponentAccessor.getApplicationProperties()).getString(APKeys.JIRA_CONSTANT_DEFAULT_PRIORITY);
        if (defaultPriorityId == null)
        {
            final Collection<Priority> priorities = getPriorityObjects();
            Priority defaultPriority = null;
            final Iterator<Priority> priorityIt = priorities.iterator();

            int times = (int) Math.ceil((double) priorities.size() / 2d);
            for (int i = 0; i < times; i++)
            {
                defaultPriority = priorityIt.next();
            }

            return defaultPriority;
        }
        else
        {
            return getPriorityObject(defaultPriorityId);
        }

    }
    public GenericValue getDefaultPriority()
    {
        final Priority defaultPriority = getDefaultPriorityObject();
        return defaultPriority == null ? null : defaultPriority.getGenericValue();
    }


    /**
     * @param id the id of a priority
     * @return the name of the priority with the given id, or an i18n'd String indicating that
     * no priority is set (e.g. "None") if the id is null.
     */
    public String getPriorityName(String id)
    {
        if ("-1".equals(id))
        {
            return authenticationContext.getI18nHelper().getText("constants.manager.no.priority");
        }

        return getPriorityObject(id).getName();
    }

    public Resolution getResolutionObject(String id)
    {
        return resolutionCache.get().getObject(id);
    }

    public void refreshPriorities()
    {
        priorityCache.reset();
    }

    public Collection<GenericValue> getResolutions()
    {
        return resolutionCache.get().getGenericValues();
    }

    public Collection<Resolution> getResolutionObjects()
    {
        return resolutionCache.get().getObjects();
    }

    public GenericValue getResolution(String id)
    {
        return convertToGenericValue(getResolutionObject(id));
    }

    public void refreshResolutions()
    {
        resolutionCache.reset();
    }

    public Collection<GenericValue> getIssueTypes()
    {
        return issueTypeCache.get().getRegularIssueTypes();
    }

    public Collection<IssueType> getAllIssueTypeObjects()
    {
        return issueTypeCache.get().getObjects();
    }

    public Collection<IssueType> getRegularIssueTypeObjects()
    {
        return issueTypeCache.get().getRegularIssueTypeObjects();
    }

    public Collection<IssueType> getSubTaskIssueTypeObjects()
    {
        return issueTypeCache.get().getSubTaskIssueTypeObjects();
    }

    public Status getStatusByName(final String name)
    {
        Collection<Status> statusObjects = getStatusObjects();
        return findByName(name, statusObjects);
    }

    public Status getStatusByNameIgnoreCase(final String name)
    {
        Collection<Status> statusObjects = getStatusObjects();
        return findByNameIgnoreCase(name, statusObjects);
    }

    public Status getStatusByTranslatedName(final String name)
    {
        Collection<Status> statusObjects = getStatusObjects();
        Status status = findByTranslatedName(name, statusObjects);
        if (status == null)
        {
            status = findByName(name, statusObjects);
        }
        return status;
    }

    private Status findByName(final String name, Collection<Status> statusObjects)
    {
        try
        {
            return Iterables.find(statusObjects, new Predicate<Status>()
            {
                @Override
                public boolean apply(@Nullable Status statusObject)
                {
                    return statusObject.getName().equals(name);
                }
            });
        }
        catch (NoSuchElementException notFound)
        {
            return null;
        }
    }

    private Status findByNameIgnoreCase(final String name, Collection<Status> statusObjects)
    {
        try
        {
            return Iterables.find(statusObjects, new Predicate<Status>()
            {
                @Override
                public boolean apply(@Nullable Status statusObject)
                {
                    return statusObject.getName().equalsIgnoreCase(name);
                }
            });
        }
        catch (NoSuchElementException notFound)
        {
            return null;
        }
    }

    private Status findByTranslatedName(final String name, Collection<Status> statusObjects)
    {
        try
        {
            return Iterables.find(statusObjects, new Predicate<Status>()
            {
                @Override
                public boolean apply(@Nullable Status statusObject)
                {
                    return statusObject.getNameTranslation().equals(name);
                }
            });
        }
        catch (NoSuchElementException notFound)
        {
            return null;
        }
    }

    /**
     * Retrieve subtask issue types.
     * @return A collection of IssueType {@link GenericValue}s
     */
    public Collection<GenericValue> getSubTaskIssueTypes()
    {
        return issueTypeCache.get().getSubTaskIssueTypes();
    }

    public List<GenericValue> getEditableSubTaskIssueTypes()
    {
        return ofBizDelegator.findByField(ISSUE_TYPE_CONSTANT_TYPE, "style", SubTaskManager.SUB_TASK_ISSUE_TYPE_STYLE, "name");
    }

    public List<GenericValue> getAllIssueTypes()
    {
        return issueTypeCache.get().getGenericValues();
    }

    public GenericValue getIssueType(String id)
    {
        return convertToGenericValue(getIssueTypeObject(id));
    }

    public IssueType getIssueTypeObject(String id)
    {
        return issueTypeCache.get().getObject(id);
    }

    public void refreshIssueTypes()
    {
        issueTypeCache.reset();
    }

    private List<GenericValue> getConstants(String type)
    {
        return getConstantsWithSort(type, DEFAULT_CONSTANTS_SORT);
    }

    private List<GenericValue> getConstantsWithSort(String type, List<String> sortList)
    {
        try
        {
            //We do not return an immutable list here because the caches take care of this by themselves.
            return ofBizDelegator.findAll(type, sortList);
        }
        catch (Exception e)
        {
            log.error("Error getting constants of type: " + type + " : " + e, e);
        }

        return null;
    }

    static class ConstantsCache<T extends IssueConstant>
    {
        private final List<GenericValue> gvList;
        private final Map<String, T> idObjectMap;
        private final List<String> idList;

        /**
         * Constructs a ConstantsCache instance.
         *
         * <p> The GenericValue List is assumed to be immutable. This is currently taken care of by #getConstantsWithSort().
         *
         * @param gvList A list of GenericValues.
         * @param idObjectMap An ID to Constant Object Map.
         */
        public ConstantsCache(final List<GenericValue> gvList, final Map<String, T> idObjectMap)
        {
            this.gvList = Collections.unmodifiableList(gvList);
            this.idObjectMap = Collections.unmodifiableMap(idObjectMap);
            // Extract and store the ids.
            // There is a big performance hit to rebuild these in complex (many projects/issuetypes/customefields) installations.
            // Only because they can get retrieved > 200,000 times per issue query.
            List<String> ids = getIds(gvList);
            this.idList = Collections.unmodifiableList(ids);
        }

        List<GenericValue> getGenericValues()
        {
            return gvList;
        }

        Collection<T> getObjects()
        {
            return idObjectMap.values();
        }

        T getObject(final String id)
        {
            return idObjectMap.get(id);
        }

        List<String> getCachedIds()
        {
            return idList;
        }
    }

    private static class IssueTypeCache extends ConstantsCache<IssueType>
    {
        private final List<IssueType> regularIssueTypeObjects;
        private final List<GenericValue> regularIssueTypes;
        private final List<IssueType> subTaskIssueTypeObjects;
        private final List<GenericValue> subTaskIssueTypes;

        /**
         * Constructs a CacheData instance.
         *
         * <p> The GenericValue List is assumed to be immutable. This is currently taken care of by getConstantsWithSort().
         *
         * @param gvList      An immutable list of GenericValues.
         * @param idObjectMap An ID to Constant Object Map.
         * @param regularIssueTypeObjects List of regular IssueType Objects ordered by name
         * @param regularIssueTypes List of regular IssueTypes ordered by name
         * @param subTaskIssueTypeObjects List of subTask IssueTypes ordered by name
         * @param subTaskIssueTypes List of subTask IssueTypes ordered by name
         */
        public IssueTypeCache(List<GenericValue> gvList, Map<String, IssueType> idObjectMap, List<IssueType> regularIssueTypeObjects,
                List<GenericValue> regularIssueTypes, List<IssueType> subTaskIssueTypeObjects, List<GenericValue> subTaskIssueTypes)
        {
            super(gvList, idObjectMap);
            this.regularIssueTypeObjects = Collections.unmodifiableList(regularIssueTypeObjects);
            this.regularIssueTypes = Collections.unmodifiableList(regularIssueTypes);
            this.subTaskIssueTypeObjects = Collections.unmodifiableList(subTaskIssueTypeObjects);
            this.subTaskIssueTypes = Collections.unmodifiableList(subTaskIssueTypes);
        }

        public List<IssueType> getRegularIssueTypeObjects()
        {
            return regularIssueTypeObjects;
        }

        public List<GenericValue> getRegularIssueTypes()
        {
            return regularIssueTypes;
        }

        public List<IssueType> getSubTaskIssueTypeObjects()
        {
            return subTaskIssueTypeObjects;
        }

        public List<GenericValue> getSubTaskIssueTypes()
        {
            return subTaskIssueTypes;
        }
    }

    private class PriorityCacheLoader implements Supplier<ConstantsCache<Priority>>
    {
        public ConstantsCache<Priority> get()
        {
            List<GenericValue> priorities = getConstants(PRIORITY_CONSTANT_TYPE);
            Map<String, Priority> priorityObjectsMap = new LinkedHashMap<String, Priority>();
            for (final GenericValue priorityGV : priorities)
            {
                final Priority priority = issueConstantFactory.createPriority(priorityGV);
                priorityObjectsMap.put(priorityGV.getString("id"), priority);
            }

            return new ConstantsCache<Priority>(priorities, priorityObjectsMap);
        }
    }
    private class ResolutionCacheLoader implements Supplier<ConstantsCache<Resolution>>
    {
        public ConstantsCache<Resolution> get()
        {
            List<GenericValue> resolutions = getConstants(RESOLUTION_CONSTANT_TYPE);
            Map<String, Resolution> resolutionObjectsMap = new LinkedHashMap<String, Resolution>();
            for (final GenericValue resolutionGV : resolutions)
            {
                Resolution resolution = issueConstantFactory.createResolution(resolutionGV);
                resolutionObjectsMap.put(resolutionGV.getString("id"), resolution);
            }

            return new ConstantsCache<Resolution>(resolutions, resolutionObjectsMap);
        }
    }
    private class StatusCacheLoader implements Supplier<ConstantsCache<Status>>
    {
        public ConstantsCache<Status> get()
        {
            List<GenericValue> statuses = getConstants(STATUS_CONSTANT_TYPE);
            Map<String, Status> statusObjectsMap = new LinkedHashMap<String, Status>();
            for (GenericValue statusGV : statuses)
            {
                statusObjectsMap.put(statusGV.getString("id"), issueConstantFactory.createStatus(statusGV));
            }
            return new ConstantsCache<Status>(statuses, statusObjectsMap);
        }
    }
    private class IssueTypeCacheLoader implements Supplier<IssueTypeCache>
    {
        public IssueTypeCache get()
        {
            // Get all issue types from DB
            List<GenericValue> allIssueTypeGVs = getConstantsWithSort(ISSUE_TYPE_CONSTANT_TYPE, DEFAULT_ISSUE_TYPE_SORT);

            Map<String, IssueType> idObjectMap = new LinkedHashMap<String, IssueType>();
            final List<IssueType> regularIssueTypeObjects = new ArrayList<IssueType>();
            final List<GenericValue> regularIssueTypes = new ArrayList<GenericValue>();
            final List<IssueType> subTaskIssueTypeObjects = new ArrayList<IssueType>();
            final List<GenericValue> subTaskIssueTypes = new ArrayList<GenericValue>();

            for (final GenericValue issueTypeGV : allIssueTypeGVs)
            {
                IssueType issueType = issueConstantFactory.createIssueType(issueTypeGV);
                if (issueType.isSubTask())
                {
                    subTaskIssueTypeObjects.add(issueType);
                    subTaskIssueTypes.add(issueTypeGV);
                }
                else
                {
                    regularIssueTypeObjects.add(issueType);
                    regularIssueTypes.add(issueTypeGV);
                }

                idObjectMap.put(issueType.getId(), issueType);
            }
            return new IssueTypeCache(allIssueTypeGVs, idObjectMap, regularIssueTypeObjects, regularIssueTypes, subTaskIssueTypeObjects, subTaskIssueTypes);
        }
    }
}
