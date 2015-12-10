package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.atlassian.jira.util.collect.MapBuilder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.EntityUtil;
import org.ofbiz.core.entity.GenericDelegator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Change the user and site dark features to be stored as text rather then string to allow for more then 255 characters
 *
 * @since v6.0
 */
public class UpgradeTask_Build6042 extends AbstractUpgradeTask
{

    private static final String USER_FEATURES = "user.features.enabled";
    private static final String SITE_FEATURES = "jira.enabled.dark.features";


    private static final class Table
    {
        private static final String ENTRY = "OSPropertyEntry";
        private static final String STRING = "OSPropertyString";
        private static final String FEATURE = "Feature";
        private static final String APPLICATION_USER = "ApplicationUser";
    }

    private static final class Column
    {
        private static final String ID = "id";
        private static final String VALUE = "value";
        private static final String KEY = "propertyKey";
        private static final String ENTITY_ID = "entityId";

        private static final String FEATURE_NAME = "featureName";
        private static final String FEATURE_TYPE = "featureType";
        private static final String USERKEY = "userKey";
    }


    private final OfBizDelegator ofBizDelegator;
    private final JiraAuthenticationContext authContext;

    public UpgradeTask_Build6042(OfBizDelegator ofBizDelegator, JiraAuthenticationContext authContext)
    {
        super(false);
        this.ofBizDelegator = ofBizDelegator;
        this.authContext = authContext;
    }

    @Override
    public String getBuildNumber()
    {
        return "6042";
    }

    @Override
    public String getShortDescription()
    {
        return "Change the user and site dark features to be stored as text rather then string to allow for more then 255 characters";
    }

    @Override
    public void doUpgrade(boolean setupMode) throws Exception
    {
        ensureFeatureTableConsistent();
        migrateFeatures(USER_FEATURES);
        migrateFeatures(SITE_FEATURES);
    }

    private void migrateFeatures(String featureType) throws GenericEntityException
    {
        GenericValue sourcePropertyGV = getPropertyGV(Table.ENTRY, Column.KEY, featureType);

        if (sourcePropertyGV != null)
        {
            long entityId = sourcePropertyGV.getLong(Column.ENTITY_ID);
            long id = sourcePropertyGV.getLong(Column.ID);
            GenericValue sourceFeatureGV = ofBizDelegator.findById(Table.STRING,id);

            if (sourceFeatureGV != null)
            {
                Set<String> sourceFeatures = deserialize(sourceFeatureGV.getString(Column.VALUE));
                sourceFeatureGV.remove();

                for (String feature: sourceFeatures)
                {
                    MapBuilder<String, Object> propertyMapBuilder = MapBuilder.newBuilder();
                    propertyMapBuilder.add(Column.FEATURE_NAME,feature);
                    propertyMapBuilder.add(Column.FEATURE_TYPE, getFeatureType(featureType));
                    propertyMapBuilder.add(Column.USERKEY, getUserKey(featureType,entityId));
                    final Map<String, Object> featureMap =  propertyMapBuilder.toMap();
                    if (featureNotPresent(featureMap))
                    {
                        ofBizDelegator.createValue(Table.FEATURE,featureMap);
                    }
                }
            }

            sourcePropertyGV.remove();
        }
    }

    private boolean featureNotPresent(final Map<String, Object> featureMap)
    {
        List<GenericValue> features = ofBizDelegator.findByAnd(Table.FEATURE, featureMap);
        return (features == null || features.size() == 0 );
    }

    private String getFeatureType(String property)
    {
        return property.equals(USER_FEATURES) ? "user" : "site";
    }

    private String getUserKey(String featureType, long entityId)
    {
        if (featureType.equals(USER_FEATURES))
        {
            GenericValue usergv = ofBizDelegator.findById(Table.APPLICATION_USER,entityId);
            if (usergv != null)
            {
                return usergv.getString(Column.USERKEY);
            }
        }

        return null;
    }

    private GenericValue getPropertyGV(String table, String column, String value)
    {
        List<GenericValue> properties = ofBizDelegator.findByAnd(table, ImmutableMap.of(column, value));
        if (properties != null && properties.size() > 0)
        {
            return properties.iterator().next();
        }

        return null;
    }

    private Set<String> deserialize(String features)
    {
        if (StringUtils.isBlank(features))
            return Sets.newHashSet();

        String[] featureKeys = features.split(",");

        return Sets.newHashSet(featureKeys);
    }

    private void ensureFeatureTableConsistent() throws GenericEntityException
    {
        List<GenericValue> features = ofBizDelegator.findAll(Table.FEATURE, Lists.newArrayList(Column.ID));
        if (features != null && features.size() > 0)
        {
            long maxId= features.get(features.size()-1).getLong(Column.ID);
            setNextId(Table.FEATURE, ++maxId );
        }

    }

    /**
     * We need to make sure that we account for failed upgrades - the QA-EACJ migration showed that the table may linger but the sequence ids go
     *
     * @param entityName
     * @param nextId
     * @throws GenericEntityException
     */
    private void setNextId(String entityName, Long nextId) throws GenericEntityException
    {
        final GenericDelegator delegator = getDelegator();
        // First ensure we have an entry in SequenecValueItem table
        delegator.getNextSeqId(entityName);
        // Now set it to nextId
        GenericValue sequenceItem = EntityUtil.getOnly(delegator.findByAnd("SequenceValueItem", ImmutableMap.of("seqName", entityName)));
        if (sequenceItem != null)
        {
            sequenceItem.set("seqId", nextId);
            sequenceItem.store();
            delegator.refreshSequencer();
        }
    }
}
