package com.atlassian.jira.association;

import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.util.dbc.Assertions;
import org.ofbiz.core.entity.EntityUtil;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.util.UtilMisc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NodeAssociationStoreImpl implements NodeAssociationStore
{
    private final OfBizDelegator ofBizDelegator;

    public NodeAssociationStoreImpl(OfBizDelegator ofBizDelegator)
    {
        this.ofBizDelegator = ofBizDelegator;
    }

    @Override
    public List<GenericValue> getSinksFromSource(GenericValue source, String sinkEntityName, String associationType)
            throws DataAccessException
    {
        if (source == null)
        {
            throw new IllegalArgumentException("Source GenericValue can not be null.");
        }

        return getSinksFromSource(source.getEntityName(), source.getLong("id"), sinkEntityName, associationType);
    }

    @Override
    public List<GenericValue> getSinksFromSource(String sourceEntityName, Long sourceNodeId, String sinkEntityName, String associationType)
    {
        final List<GenericValue> result = getAssociationsFromSource(sourceEntityName, sourceNodeId, sinkEntityName, associationType);

        final List<GenericValue> outList = new ArrayList<GenericValue>(result.size());
        for (final GenericValue value : result)
        {
            GenericValue genericValue = ofBizDelegator.findByPrimaryKey(sinkEntityName, value.getLong("sinkNodeId"));

            if (genericValue != null)
            {
                outList.add(genericValue);
            }
        }
        return outList;
    }

    @Override
    public List<GenericValue> getSourcesFromSink(GenericValue sink, String sourceName, String associationType)
            throws DataAccessException
    {
        Assertions.notNull("sink", sink);

        final List<GenericValue> result = getAssociationsFromSink(sink, associationType, sourceName);
        final List<GenericValue> outList = new ArrayList<GenericValue>(result.size());
        for (final GenericValue value : result)
        {
            GenericValue genericValue = ofBizDelegator.findByPrimaryKey(sourceName, value.getLong("sourceNodeId"));

            if (genericValue != null)
            {
                outList.add(genericValue);
            }
        }
        return outList;
    }

    /**
     * Create an association between two entities, given a particular association type.
     * <p/>
     * If the association already exists - it will not be created.
     *
     * @return The new association, or the existing association if it already existed.
     */
    @Override
    public GenericValue createAssociation(GenericValue source, GenericValue sink, String associationType)
            throws DataAccessException
    {
        return createAssociation(source.getEntityName(), source.getLong("id"), sink.getEntityName(), sink.getLong("id"), associationType);
    }

    @Override
    public void createAssociation(NodeAssocationType type, Long sourceNodeId, Long sinkNodeId)
    {
        createAssociation(type.getSourceEntityName(), sourceNodeId, type.getSinkEntityName(), sinkNodeId, type.getName());
    }

    @Override
    public GenericValue createAssociation(String sourceNodeEntity, Long sourceNodeId, String sinkNodeEntity, Long sinkNodeId, String associationType)
            throws DataAccessException
    {
        GenericValue association = getAssociation(sourceNodeEntity, sourceNodeId, sinkNodeEntity, sinkNodeId, associationType);
        if (association == null)
        {
            // NodeAssociation does not have an ID field, therefore we can't use ofBizDelegator.createValue()
            association = ofBizDelegator.makeValue("NodeAssociation");
            association.setFields(UtilMisc.toMap(
                    "associationType", associationType,
                    "sourceNodeId", sourceNodeId,
                    "sourceNodeEntity", sourceNodeEntity,
                    "sinkNodeId", sinkNodeId,
                    "sinkNodeEntity", sinkNodeEntity));
            try
            {
                association.create();
            }
            catch (GenericEntityException ex)
            {
                throw new DataAccessException(ex);
            }
        }
        return association;
    }

    @Override
    public void removeAssociation(NodeAssocationType type, Long sourceNodeId, Long sinkNodeId)
    {
        removeAssociation(type.getName(), type.getSourceEntityName(), sourceNodeId, type.getSinkEntityName(), sinkNodeId);
    }

    @Override
    public void removeAssociation(GenericValue source, GenericValue sink, String associationType)
            throws DataAccessException
    {
        removeAssociation(associationType, source.getEntityName(), source.getLong("id"), sink.getEntityName(), sink.getLong("id"));
    }

    private void removeAssociation(String associationType, String sourceEntityName, Long sourceNodeId, String sinkEntityName, Long sinkNodeId)
    {
        final Map<String, Object> fields = new HashMap<String, Object>();
        fields.put("associationType", associationType);
        fields.put("sourceNodeEntity", sourceEntityName);
        fields.put("sourceNodeId", sourceNodeId);
        fields.put("sinkNodeEntity", sinkEntityName);
        fields.put("sinkNodeId", sinkNodeId);
        ofBizDelegator.removeByAnd("NodeAssociation", fields);
    }

    @Override
    public void removeAssociationsFromSource(GenericValue source) throws DataAccessException
    {
        final Map<String, Object> fields = new HashMap<String, Object>();
        fields.put("sourceNodeId", source.getLong("id"));
        fields.put("sourceNodeEntity", source.getEntityName());
        ofBizDelegator.removeByAnd("NodeAssociation", fields);
    }

    @Override
    public void removeAssociationsFromSource(NodeAssocationType nodeAssocationType, Long sourceNodeId)
    {
        final Map<String, Object> fields = new HashMap<String, Object>();
        fields.put("associationType", nodeAssocationType.getName());
        fields.put("sourceNodeEntity", nodeAssocationType.getSourceEntityName());
        fields.put("sinkNodeEntity", nodeAssocationType.getSinkEntityName());
        fields.put("sourceNodeId", sourceNodeId);
        ofBizDelegator.removeByAnd("NodeAssociation", fields);
    }

    @Override
    public void removeAssociationsFromSink(GenericValue sink) throws DataAccessException
    {
        final Map<String, Object> fields = new HashMap<String, Object>();
        fields.put("sinkNodeId", sink.getLong("id"));
        fields.put("sinkNodeEntity", sink.getEntityName());
        ofBizDelegator.removeByAnd("NodeAssociation", fields);
    }

    @Override
    public void swapAssociation(String sourceEntityName, String associationType, GenericValue fromSink, GenericValue toSink)
            throws DataAccessException
    {
        final List<GenericValue> sources = getSourcesFromSink(fromSink, sourceEntityName, associationType);
        swapAssociation(sources, associationType, fromSink, toSink);
    }

    @Override
    public void swapAssociation(List<GenericValue> entities, String associationType, GenericValue fromSink, GenericValue toSink)
            throws DataAccessException
    {
        for (final GenericValue entity : entities)
        {
            createAssociation(entity, toSink, associationType);
            removeAssociation(entity, fromSink, associationType);
        }
    }

    @Override
    public GenericValue getAssociation(GenericValue source, GenericValue sink, String associationType)
            throws DataAccessException
    {
        return getAssociation(source.getEntityName(), source.getLong("id"), sink.getEntityName(), sink.getLong("id"), associationType);
    }

    @Override
    public List<Long> getSinkIdsFromSource(GenericValue source, String sinkEntityName, String associationType)
    {
        List<GenericValue> sinks = getAssociationsFromSource(source.getEntityName(), source.getLong("id"), sinkEntityName, associationType);

        return getIdsFromNodes(sinks, "sinkNodeId");
    }

    @Override
    public List<Long> getSinkIdsFromSource(NodeAssocationType nodeAssocationType, Long sourceNodeId)
    {
        List<GenericValue> sinks = getAssociationsFromSource(nodeAssocationType.getSourceEntityName(), sourceNodeId, nodeAssocationType.getSinkEntityName(), nodeAssocationType.getName());

        return getIdsFromNodes(sinks, "sinkNodeId");
    }

    @Override
    public List<Long> getSourceIdsFromSink(GenericValue sink, String sourceEntity, String associationType)
    {
        List<GenericValue> associations = getAssociationsFromSink(sink, associationType, sourceEntity);

        return getIdsFromNodes(associations, "sourceNodeId");
    }

    @Override
    public List<Long> getSourceIdsFromSink(NodeAssocationType nodeAssocationType, Long sinkNodeId)
    {
        List<GenericValue> associations = getAssociationsFromSink(nodeAssocationType.getSinkEntityName(), sinkNodeId, nodeAssocationType.getSourceEntityName(), nodeAssocationType.getName());

        return getIdsFromNodes(associations, "sourceNodeId");
    }

    private List<Long> getIdsFromNodes(List<GenericValue> nodes, String idFieldName)
    {
        List<Long> ids = new ArrayList<Long>();
        for (final GenericValue node : nodes)
        {
            ids.add(node.getLong(idFieldName));
        }
        return ids;
    }

    private List<GenericValue> getAssociationsFromSource(String sourceEntityName, Long sourceNodeId, String sinkEntityName, String associationType)
    {
        final FieldMap fieldMap = new FieldMap();
        fieldMap.put("sourceNodeEntity", sourceEntityName);
        fieldMap.put("sourceNodeId", sourceNodeId);
        fieldMap.put("sinkNodeEntity", sinkEntityName);
        fieldMap.put("associationType", associationType);
        return getAssociations("NodeAssociation", fieldMap, false);
    }

    private List<GenericValue> getAssociationsFromSink(String sinkEntityName, Long sinkNodeId, String sourceEntityName, String associationType)
    {
        final Map<String, Object> fields = new HashMap<String, Object>();
        fields.put("sinkNodeEntity", sinkEntityName);
        fields.put("sinkNodeId", sinkNodeId);
        fields.put("sourceNodeEntity", sourceEntityName);
        fields.put("associationType", associationType);
        return getAssociations("NodeAssociation", fields, false);
    }

    private List<GenericValue> getAssociationsFromSink(GenericValue sink, String associationType, String sourceName)
    {
        final Map<String, Object> fields = new HashMap<String, Object>();
        fields.put("sinkNodeId", sink.getLong("id"));
        fields.put("sinkNodeEntity", sink.getEntityName());
        fields.put("associationType", associationType);
        fields.put("sourceNodeEntity", sourceName);
        return getAssociations("NodeAssociation", fields, false);
    }

    private List<GenericValue> getAssociations(String associationName, Map<String, Object> fields, boolean useSequence)
            throws DataAccessException
    {
        List<GenericValue> result;
        result = ofBizDelegator.findByAnd(associationName, fields);
        if (result == null)
            return Collections.emptyList();

        if (useSequence)
        {
            result = EntityUtil.orderBy(result, UtilMisc.toList("sequence"));
        }
        return result;
    }

    private GenericValue getAssociation(String sourceNodeEntity, Long sourceNodeId, String sinkNodeEntity, Long sinkNodeId, String associationType)
    {
        return EntityUtil.getOnly(ofBizDelegator.findByAnd("NodeAssociation", UtilMisc.toMap("associationType", associationType, "sourceNodeId", sourceNodeId, "sourceNodeEntity", sourceNodeEntity, "sinkNodeId", sinkNodeId, "sinkNodeEntity", sinkNodeEntity)));
    }
}
