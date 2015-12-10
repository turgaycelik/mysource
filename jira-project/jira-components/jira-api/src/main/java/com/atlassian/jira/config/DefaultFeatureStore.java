package com.atlassian.jira.config;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.util.collect.MapBuilder;

import com.google.common.collect.ImmutableMap;

import org.ofbiz.core.entity.GenericValue;

/**
 * Non cached implementation of feature store
 *
 * @since v6.0
 */
public class DefaultFeatureStore implements FeatureStore
{

    private static final String FEATURE_ENTITY = "Feature";
    private static final String ID = "id";
    private static final String KEY = "featureName";
    private static final String TYPE = "featureType";
    private static final String USERKEY = "userKey";

    private final OfBizDelegator ofBizDelegator;
    private static final String SITE_TYPE = "site";
    private static final String USER_TYPE = "user";

    public DefaultFeatureStore(OfBizDelegator ofBizDelegator)
    {
        this.ofBizDelegator = ofBizDelegator;
    }

    @Override
    public void delete(String featureKey, String userKey) throws DataAccessException
    {

        Map<String, Object> featureMap = buildFeatureMap(featureKey, userKey);

        ofBizDelegator.removeByAnd(FEATURE_ENTITY, featureMap);
    }

    @Override
    public void create(String featureKey, String userKey) throws DataAccessException
    {
        Map<String, Object> featureMap = buildFeatureMap(featureKey, userKey);
        ofBizDelegator.createValue(FEATURE_ENTITY,featureMap);
    }

    @Override
    public Set<String> getUserFeatures(@Nonnull String userKey) throws DataAccessException
    {
        List<GenericValue> gvFeatures = ofBizDelegator.findByAnd(FEATURE_ENTITY,ImmutableMap.of(USERKEY,userKey));

        return makeFeatures(gvFeatures);
    }

    @Override
    public Set<String> getSiteFeatures() throws DataAccessException
    {
        List<GenericValue> gvFeatures = ofBizDelegator.findByAnd(FEATURE_ENTITY,ImmutableMap.of(TYPE,SITE_TYPE));

        return makeFeatures(gvFeatures);
    }

    private Map<String, Object> buildFeatureMap(String featureKey, String userKey)
    {
        MapBuilder<String,Object> featureBuilder =  MapBuilder.newBuilder();
        featureBuilder.add(KEY,featureKey);
        if (userKey == null)
        {
            featureBuilder.add(TYPE, SITE_TYPE);
        }
        else
        {
            featureBuilder.add(TYPE,USER_TYPE);
            featureBuilder.add(USERKEY,userKey);
        }
        return featureBuilder.toMap();
    }

    private Set<String> makeFeatures(List<GenericValue> gvFeatures) {
        Set<String> features = new HashSet<String>();

        for (GenericValue gv: gvFeatures)
        {
            features.add(gv.getString(KEY));
        }

        return features;
    }

    private String type(String userkey)
    {
        if (userkey == null)
            return "SITE_TYPE";
        else
            return "user";
    }
}
