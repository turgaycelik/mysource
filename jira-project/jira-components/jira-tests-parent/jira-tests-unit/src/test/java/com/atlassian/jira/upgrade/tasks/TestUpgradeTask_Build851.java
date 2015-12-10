package com.atlassian.jira.upgrade.tasks;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.atlassian.core.ofbiz.AbstractOFBizTestCase;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockComponentContainer;
import com.atlassian.jira.local.runner.ListeningMockitoRunner;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;

import com.google.common.collect.ImmutableMap;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test the upgrade of dark feature being stored as text rather then 255
 *
 * @since v5.2.8
 */

@RunWith (ListeningMockitoRunner.class)
public class TestUpgradeTask_Build851 extends AbstractOFBizTestCase
{


    @Rule
    public MockComponentContainer mockComponentContainer = new MockComponentContainer(this);

    @Mock
    @AvailableInContainer
    OfBizDelegator mockOfBizDelegator;

    @Mock
    @AvailableInContainer
    JiraAuthenticationContext mockAuthContext;

    @Mock
    @AvailableInContainer
    UserManager mockUserManager;

    @Mock
    User mockUser;

    @Mock
    ApplicationUser mockAppUser;

    @Before
    public void setup() throws Exception
    {
        super.setUp();
        when(mockUser.getName()).thenReturn("Fred");
        when(mockUserManager.getUserObject("Fred")).thenReturn(mockUser);
        when(mockUserManager.getUserByName("Fred")).thenReturn(mockAppUser);
        when(mockUserManager.getUserEvenWhenUnknown("Fred")).thenReturn(mockUser);
        when(mockAppUser.getKey()).thenReturn("fred");
    }

    @Test
    public void testUpgrade() throws Exception
    {
        UpgradeTask_Build851 upgradeTaskBuild851 = new UpgradeTask_Build851(mockOfBizDelegator, mockAuthContext, mockUserManager );
        setupMockOfBizDelegator();
        upgradeTaskBuild851.doUpgrade(false);
        verifyFeaturesHaveBeenCreated();

        assertTrue(upgradeTaskBuild851.getErrors().isEmpty());
        assertFalse(upgradeTaskBuild851.isReindexRequired());

    }

    private void setupMockOfBizDelegator()
    {
        when(mockOfBizDelegator.findByAnd("OSPropertyEntry", ImmutableMap.of("propertyKey", "user.features.enabled"))).thenReturn(getUserFeaturePropertyEntryGVs());
        when(mockOfBizDelegator.findByAnd("OSPropertyEntry", ImmutableMap.of("propertyKey", "jira.enabled.dark.features"))).thenReturn(getSiteFeaturePropertyEntryGVs());
        when(mockOfBizDelegator.findById("OSPropertyString", 10360L)).thenReturn(getSiteFeatureGV());
        when(mockOfBizDelegator.findById("OSPropertyString", 10536L)).thenReturn(getUserFeatureGV());
        when(mockOfBizDelegator.findById(("ExternalEntity"), 10L)).thenReturn(getExternalEntityGv("Fred"));
    }

    private void verifyFeaturesHaveBeenCreated()
    {
        verify(mockOfBizDelegator).createValue("Feature", fakeFeatureMap("sitencnc", "site", null));
        verify(mockOfBizDelegator).createValue("Feature", fakeFeatureMap("jira.site.darkfeature.admin", "site", null));
        verify(mockOfBizDelegator).createValue("Feature", fakeFeatureMap("jira.user.darkfeature.admin", "site", null));
        verify(mockOfBizDelegator).createValue("Feature", fakeFeatureMap("ghh", "site", null));
        verify(mockOfBizDelegator).createValue("Feature", fakeFeatureMap("jira.site.darkfeature.user", "site", null));
        verify(mockOfBizDelegator).createValue("Feature", fakeFeatureMap("fred.darkfeature", "user", "fred"));
        verify(mockOfBizDelegator).createValue("Feature", fakeFeatureMap("darkfeature.2", "user", "fred"));
        verify(mockOfBizDelegator).createValue("Feature", fakeFeatureMap("blah", "user", "fred"));
    }

    private GenericValue getExternalEntityGv(String user)
    {
        return new MockGv("ExternalEntity", FieldMap.build("id", 213, "name", user));
    }

    GenericValue getSiteFeatureGV()
    {
        return fakePropertyValue("OSPropertyString", 10360L, "sitencnc,jira.site.darkfeature.admin,jira.user.darkfeature.admin,ghh,jira.site.darkfeature.user");

    }

    GenericValue getUserFeatureGV()
    {
        return fakePropertyValue("OSPropertyString", 10536L, "fred.darkfeature,darkfeature.2,blah");
    }


    List<GenericValue> getUserFeaturePropertyEntryGVs()
    {
        return Arrays.asList(
                fakePropertyEntry(10536L, "ExternalEntity", 10L, "user.features.enabled", 5)
        );
    }

    List<GenericValue> getSiteFeaturePropertyEntryGVs()
    {
        return Arrays.asList(
                fakePropertyEntry(10360L, "jira.properties", 1L, "jira.enabled.dark.features", 5)
        );
    }

    private GenericValue fakePropertyValue(String entityName, long id, String propertyValue)
    {
        return new MockGv(entityName, FieldMap.build("id", id).add("value", propertyValue));
    }

    private GenericValue fakeFeature(long id, String featureName, String featureType, String userKey)
    {
        return new MockGv("Feature", FieldMap.build("id", id).add("featureName", featureName).add("featureType", featureType).add("userKey", userKey));
    }

    private FieldMap fakeFeatureMap(String featureName, String featureType, String userKey)
    {
        return FieldMap.build("featureName", featureName).add("featureType", featureType).add("userKey", userKey);
    }

    private GenericValue fakePropertyEntry(long id, String name, long entityId, String propertyKey, int propertyType)
    {
        return new MockGv("OSPropertyEntry", FieldMap.build("id", id).add("entityName", name).add("entityId", entityId).add("propertyKey", propertyKey).add("type", propertyType));
    }

    public static class MockGv extends MockGenericValue
    {
        public MockGv(String entityName, Map fields)
        {
            super(entityName, fields);
        }

        @Override
        public void remove() throws GenericEntityException
        {
            removed = true;
        }
    }
}
