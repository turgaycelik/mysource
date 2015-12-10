package com.atlassian.jira.security.auth.trustedapps;

import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.PrimitiveMap;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DefaultTrustedApplicationStore implements TrustedApplicationStore
{
    private final OfBizDelegator ofBizDelegator;

    public DefaultTrustedApplicationStore(final OfBizDelegator ofBizDelegator)
    {
        this.ofBizDelegator = ofBizDelegator;
    }

    public TrustedApplicationData getById(final long id)
    {
        return transform(findGenericValue(id));
    }

    public TrustedApplicationData getByApplicationId(final String applicationId)
    {
        return transform(findGenericValue(applicationId));
    }

    public Set<TrustedApplicationData> getAll()
    {
        final List<GenericValue> all = ofBizDelegator.findAll(ENTITY_NAME);
        final Set<TrustedApplicationData> result = new LinkedHashSet<TrustedApplicationData>();
        for (final GenericValue element : all)
        {
            result.add(transform(element));
        }
        return Collections.unmodifiableSet(result);
    }

    public TrustedApplicationData store(final TrustedApplicationData trustedApplicationData)
    {
        final Map<String, Object> map = new TrustedApplicationBuilder().set(trustedApplicationData).toMap();
        final GenericValue oldValue = (trustedApplicationData.getId() > 0) ? findGenericValue(trustedApplicationData.getId()) : null;
        if (oldValue == null)
        {
            final GenericValue newValue = ofBizDelegator.createValue(ENTITY_NAME, map);
            return transform(newValue);
        }
        final GenericValue newValue = new GenericValue(oldValue);
        newValue.setFields(map);
        ofBizDelegator.store(newValue);
        return trustedApplicationData;
    }

    public boolean delete(final long id)
    {
        return ofBizDelegator.removeByAnd(ENTITY_NAME, getIdQuery(id)) > 0;
    }

    @Override
    public boolean delete(String applicationId)
    {
        return ofBizDelegator.removeByAnd(ENTITY_NAME, getApplicationIdQuery(applicationId)) > 0;
    }

    private GenericValue findGenericValue(final String applicationId)
    {
        return find(new PrimitiveMap.Builder().add(Fields.APPLICATION_ID, applicationId).toMap());
    }

    private GenericValue findGenericValue(final long id)
    {
        return find(getIdQuery(id));
    }

    private Map<String, Object> getIdQuery(final long id)
    {
        return new PrimitiveMap.Builder().add(Fields.ID, id).toMap();
    }

    private Map<String, Object> getApplicationIdQuery(final String id)
    {
        return new PrimitiveMap.Builder().add(Fields.APPLICATION_ID, id).toMap();
    }

    private GenericValue find(final Map<String, Object> query)
    {
        final List<GenericValue> list = ofBizDelegator.findByAnd(ENTITY_NAME, query);
        if (list.size() == 0)
        {
            return null;
        }
        ///CLOVER:OFF
        if (list.size() > 1)
        {
            throw new IllegalStateException("There's more than one TrustedApplication in the database with the same ID: " + query);
        }
        ///CLOVER:ON
        return list.get(0);
    }

    private TrustedApplicationData transform(final GenericValue gv)
    {
        return (gv == null) ? null : new TrustedApplicationBuilder().set(gv).toData();
    }
}