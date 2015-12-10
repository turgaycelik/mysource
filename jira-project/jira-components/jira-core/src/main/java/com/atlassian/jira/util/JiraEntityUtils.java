package com.atlassian.jira.util;

import com.atlassian.jira.association.NodeAssociationStore;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.history.ChangeItemBean;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.map.ListOrderedMap;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class JiraEntityUtils
{
    public static final Transformer GV_TO_ID_TRANSFORMER = new Transformer()
    {
        public Object transform(Object object)
        {
            return ((GenericValue) object).getLong("id");
        }
    };

    /**
     * Class has only static members. Constructor is not necessary.
     */
    private JiraEntityUtils()
    {
    }

    /**
     * Transformer for turning GenericValue objects into thier Long ids.
     * Use with {@link org.apache.commons.collections.CollectionUtils#collect(java.util.Collection, org.apache.commons.collections.Transformer)} etc.
     */
    public static final Transformer GENERIC_VALUE_TO_ID_TRANSFORMER = new Transformer()
    {
        public Object transform(Object object)
        {
            GenericValue project = (GenericValue) object;
            return project.getLong("id");
        }
    };

    /**
     * Creates a new collection of IDs from given collection of generic values.
     *
     * @param genericValues a collection of generic values [GenericValue]
     * @return a collection of IDs [Long]
     */
    public static Collection transforToIdsCollection(Collection genericValues)
    {
        return CollectionUtils.collect(genericValues, GENERIC_VALUE_TO_ID_TRANSFORMER);
    }

    /**
     * This method will return the project relevant to a given entity.
     * <p/>
     * For example if you pass in an issue, it will get the related project.
     * If you pass in an action, it will find the issue, and then get the project for it.
     * @param entity Entity to determine the project from
     * @return Project {@link org.ofbiz.core.entity.GenericValue} if it can be found, otherwise null
     * @throws com.atlassian.jira.exception.DataAccessException in the event of some datalayer problem.
     *
     * @deprecated Work with Project, Issue and Comment objects instead. Since v5.0.
     */
    public static GenericValue getProject(GenericValue entity) throws DataAccessException
    {
        if (entity != null)
        {
            if (entity.getEntityName().equals("Project"))
            {
                return entity;
            }
            else if (entity.getEntityName().equals("Issue"))
            {
                return ComponentAccessor.getProjectManager().getProject(entity);
            }
            else if (entity.getEntityName().equals("Action"))
            {
                return getProject(ComponentAccessor.getIssueManager().getIssue(entity.getLong("issue")));
            }
        }

        return null;
    }

    /**
     * Slightly changed from {@link #updateDependentEntities(org.ofbiz.core.entity.GenericValue, java.util.Collection, String, String)}
     * to cope with GV components that have different representation of null and empty strings (eg. Oracle) - JRA-12130
     *
     * Update the dependent entities of this issue (for example versions, fix versions or components)
     *
     * @param issue           The issue to update entities for
     * @param newEntities     The list of new dependent entities ({@link GenericValue}'s)
     * @param relationType    The dependency (association) type to look at, from {@link com.atlassian.jira.issue.IssueRelationConstants}
     * @param changeItemField The fieldname of the change items generated
     * @return A list of {@link ChangeItemBean change items} or an empty list if nothing was changed
     * @throws org.ofbiz.core.entity.GenericEntityException If something real bad happens
     */
    public static List updateDependentEntitiesCheckId(GenericValue issue, Collection newEntities, String relationType, String changeItemField) throws GenericEntityException
    {
        if (newEntities == null)
        {
            newEntities = Collections.EMPTY_LIST;
        }

        List changes = new ArrayList(newEntities.size());

        Collection oldEntities = ComponentAccessor.getIssueManager().getEntitiesByIssue(relationType, issue);

        //compare the ids not the GV - JRA-12130
        Collection oldEntityIds = CollectionUtils.collect(oldEntities, GV_TO_ID_TRANSFORMER);
        Collection newEntityIds = CollectionUtils.collect(newEntities, GV_TO_ID_TRANSFORMER);

        for (final Object newEntity1 : newEntities)
        {
            final GenericValue newEntity = (GenericValue) newEntity1;
            if (!oldEntityIds.contains(newEntity.getLong("id")))
            {
                getNodeAssociationStore().createAssociation(issue, newEntity, relationType);
                changes.add(new ChangeItemBean(ChangeItemBean.STATIC_FIELD, changeItemField, null, null, newEntity.getLong("id").toString(), newEntity.getString("name")));
            }
        }

        // loop through existing entities and remove any that aren't in new entities
        for (final Object oldEntity1 : oldEntities)
        {
            GenericValue oldEntity = (GenericValue) oldEntity1;
            if (!newEntityIds.contains(oldEntity.getLong("id")))
            {
                getNodeAssociationStore().removeAssociation(issue, oldEntity, relationType);
                changes.add(new ChangeItemBean(ChangeItemBean.STATIC_FIELD, changeItemField, oldEntity.getLong("id").toString(), oldEntity.getString("name"), null, null));
            }
        }
        return changes;
    }

    /**
     * Update the dependent entities of this issue (for example versions, fix versions or components)
     *
     * @param issue           The issue to update entities for
     * @param newEntities     The list of new dependent entities
     * @param relationType    The dependency (association) type to look at, from {@link com.atlassian.jira.issue.IssueRelationConstants}
     * @param changeItemField The fieldname of the change items generated
     * @return A list of change items or an empty list if nothing was changed
     * @deprecated please use {@link #updateDependentEntitiesCheckId(org.ofbiz.core.entity.GenericValue, java.util.Collection, String, String)} instead,
     * as it compares ids rather than {@link org.ofbiz.core.entity.GenericValue GenericValues}
     * @throws org.ofbiz.core.entity.GenericEntityException If something real bad happens
     */
    public static List updateDependentEntities(GenericValue issue, Collection newEntities, String relationType, String changeItemField) throws GenericEntityException
    {
        if (newEntities == null)
        {
            newEntities = Collections.EMPTY_LIST;
        }

        List changes = new ArrayList(newEntities.size());

        Collection oldEntities = ComponentAccessor.getIssueManager().getEntitiesByIssue(relationType, issue);

        for (final Object newEntity1 : newEntities)
        {
            GenericValue newEntity = (GenericValue) newEntity1;
            if (!oldEntities.contains(newEntity))
            {
                getNodeAssociationStore().createAssociation(issue, newEntity, relationType);
                changes.add(new ChangeItemBean(ChangeItemBean.STATIC_FIELD, changeItemField, null, null, newEntity.getLong("id").toString(), newEntity.getString("name")));
            }
        }

        // loop through existing entities and remove any that aren't in new entities
        for (final Object oldEntity1 : oldEntities)
        {
            GenericValue oldEntity = (GenericValue) oldEntity1;
            if (!newEntities.contains(oldEntity))
            {
                getNodeAssociationStore().removeAssociation(issue, oldEntity, relationType);
                changes.add(new ChangeItemBean(ChangeItemBean.STATIC_FIELD, changeItemField, oldEntity.getLong("id").toString(), oldEntity.getString("name"), null, null));
            }
        }

        return changes;
    }

    private static NodeAssociationStore getNodeAssociationStore()
    {
        return ComponentAccessor.getComponent(NodeAssociationStore.class);
    }

    /**
     * Convert any list of entities into a sequenced hash map with two fields.
     * <p/>
     * This is most useful for creating ordered maps used to create select boxes
     * in the web interface. (ie createEntityMap(entities, "id", "name") to create an
     * id to name map.
     * @param entities list of {@link org.ofbiz.core.entity.GenericValue GenericValues}
     * @param key the element to use as the key
     * @param value the element to use as the value
     * @return Map iterated in the entities order with key and value pairs as defined by key and value parameters
     */
    public static Map<Object, Object> createEntityMap(List<GenericValue> entities, String key, String value)
    {
        Map<Object, Object> map = new ListOrderedMap();

        for (GenericValue gv : entities)
        {
            map.put(gv.get(key), gv.get(value));
        }

        return map;
    }
}
