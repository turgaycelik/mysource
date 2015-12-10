package com.atlassian.jira.project.util;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.jira.entity.Delete;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

import org.ofbiz.core.entity.EntityUtil;
import org.ofbiz.core.entity.GenericValue;

import static com.google.common.collect.ImmutableSortedMap.copyOf;
import static java.lang.String.CASE_INSENSITIVE_ORDER;

/**
 *
 * @since v6.1
 */
public class ProjectKeyStoreImpl implements ProjectKeyStore
{
    public static final String PROJECT_KEY = "projectKey";
    public static final String PROJECT_ID = "projectId";
    private static String ENTITY_NAME = "ProjectKey";

    private final OfBizDelegator ofBizDelegator;

    public ProjectKeyStoreImpl(OfBizDelegator ofBizDelegator) {
        this.ofBizDelegator = ofBizDelegator;
    }

    @Override
    public Long getProjectId(final String key)
    {
        final GenericValue gv = EntityUtil.getOnly(ofBizDelegator.findByAnd(ENTITY_NAME, FieldMap.build(PROJECT_KEY, key)));
        return gv != null ? gv.getLong(PROJECT_ID) : null;
    }

    @Override
    public void addProjectKey(final Long projectId, final String projectKey)
    {
        ofBizDelegator.createValue(ENTITY_NAME, ImmutableMap.<String, Object>of(PROJECT_ID, projectId, PROJECT_KEY, projectKey));
    }

    @Override
    public void deleteProjectKeys(final Long projectId)
    {
        Delete.from("ProjectKey")
                .whereEqual(PROJECT_ID, projectId)
                .execute(ofBizDelegator);
    }

    @Nonnull
    @Override
    public Map<String, Long> getAllProjectKeys()
    {
        final List<GenericValue> keys = ofBizDelegator.findAll(ENTITY_NAME);
        if (keys == null) {
            return Collections.emptyMap();
        }
        final Map<String, Long> result = Maps.newHashMapWithExpectedSize(keys.size());
        for(GenericValue key : keys) {
            result.put(key.getString(PROJECT_KEY), key.getLong(PROJECT_ID));
        }
        return result;
    }

    @Nullable
    @Override
    public Long getProjectIdByKeyIgnoreCase(final String projectKey)
    {
        final Map<String, Long> projectKeys = copyOf(getAllProjectKeys(), CASE_INSENSITIVE_ORDER);
        return projectKeys.get(projectKey);
    }

    @Override
    @Nonnull
    public Set<String> getProjectKeys(final Long projectId)
    {
        final List<GenericValue> keys = ofBizDelegator.findByAnd(ENTITY_NAME, FieldMap.build(PROJECT_ID, projectId));
        if (keys == null)
        {
            return Collections.emptySet();
        }
        return ImmutableSet.copyOf(Collections2.transform(keys, new Function<GenericValue, String>()
        {
            @Override
            public String apply(GenericValue input)
            {
                return input.getString(PROJECT_KEY);
            }
        }));
    }

    @Override
    public void refresh()
    {
    }
}
