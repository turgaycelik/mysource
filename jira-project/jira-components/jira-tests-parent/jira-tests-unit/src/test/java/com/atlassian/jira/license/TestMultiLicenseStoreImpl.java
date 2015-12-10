package com.atlassian.jira.license;

import java.util.List;

import com.atlassian.jira.config.CoreFeatures;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.entity.Delete;
import com.atlassian.jira.entity.EntityEngine;
import com.atlassian.jira.junit.rules.InitMockitoMocks;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.Mock;

import static com.atlassian.jira.entity.Delete.from;
import static com.atlassian.jira.entity.Entity.PRODUCT_LICENSE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Test case for {@link com.atlassian.jira.license.MultiLicenseStoreImpl}.
 *
 * @since v6.3
 */
public class TestMultiLicenseStoreImpl
{
    @Rule public final TestRule initMocks = new InitMockitoMocks(this);

    @Mock private EntityEngine entityEngine;
    @Mock private JiraLicenseStore jiraLicenseStore;
    @Mock private EntityEngine.SelectFromContext<ProductLicense> selectFromContext;
    @Mock private EntityEngine.WhereContext<ProductLicense> whereContext;
    @Mock private FeatureManager featureManager;

    @Test
    public void licensesReplacedUponStoring()
    {
        when(featureManager.isEnabled(CoreFeatures.LICENSE_ROLES_ENABLED)).thenReturn(true);
        Delete.DeleteWhereContext from = from(PRODUCT_LICENSE).all();
        MultiLicenseStoreImpl multiLicenseStore = new MultiLicenseStoreImpl(entityEngine, jiraLicenseStore, featureManager);

        String licenseKey1 = "licenseKey1";
        String licenseKey2 = "licenseKey2";
        multiLicenseStore.store(ImmutableSet.of(licenseKey1, licenseKey2));

        verify(entityEngine).delete(from);
        verify(entityEngine).createValue(PRODUCT_LICENSE, new ProductLicense(licenseKey1));
        verify(entityEngine).createValue(PRODUCT_LICENSE, new ProductLicense(licenseKey2));
    }

    @Test
    public void absentLicenseIsRepresentedAsEmptyList()
    {
        when(entityEngine.selectFrom(PRODUCT_LICENSE)).thenReturn(selectFromContext);
        when(selectFromContext.findAll()).thenReturn(whereContext);
        when(whereContext.list()).thenReturn(Lists.<ProductLicense>newArrayList());
        when(jiraLicenseStore.retrieve()).thenReturn(null);

        MultiLicenseStoreImpl multiLicenseStore = new MultiLicenseStoreImpl(entityEngine, jiraLicenseStore, featureManager);
        Iterable<String> retrieve = multiLicenseStore.retrieve();

        assertEquals(Iterables.size(retrieve), 0);
    }

    @Test
    public void legacyLicensesAreClearedUponStore()
    {
        when(jiraLicenseStore.retrieve()).thenReturn(".");
        when(featureManager.isEnabled(CoreFeatures.LICENSE_ROLES_ENABLED)).thenReturn(true);

        MultiLicenseStoreImpl multiLicenseStore = new MultiLicenseStoreImpl(entityEngine, jiraLicenseStore, featureManager);
        multiLicenseStore.store(ImmutableSet.of(""));

        verify(jiraLicenseStore).remove();
    }

    @Test
    public void multipleLicensesAreRetrieved()
    {
        when(featureManager.isEnabled(CoreFeatures.LICENSE_ROLES_ENABLED)).thenReturn(true);
        when(entityEngine.selectFrom(PRODUCT_LICENSE)).thenReturn(selectFromContext);
        when(selectFromContext.findAll()).thenReturn(whereContext);
        List<ProductLicense> productLicenseEntities = Lists.newArrayList(new ProductLicense("a1"), new ProductLicense("a2"));
        when(whereContext.orderBy("id")).thenReturn(productLicenseEntities);

        MultiLicenseStoreImpl multiLicenseStore = new MultiLicenseStoreImpl(entityEngine, jiraLicenseStore, featureManager);
        Iterable<String> retrieve = multiLicenseStore.retrieve();

        assertTrue(Iterables.contains(retrieve, "a1"));
        assertTrue(Iterables.contains(retrieve, "a2"));
        assertEquals(Iterables.size(retrieve), 2);
    }

    @Test
    public void legacyLicenseIsUsedAsAFallback()
    {
        when(featureManager.isEnabled(CoreFeatures.LICENSE_ROLES_ENABLED)).thenReturn(true);
        when(entityEngine.selectFrom(PRODUCT_LICENSE)).thenReturn(selectFromContext);
        when(selectFromContext.findAll()).thenReturn(whereContext);
        when(whereContext.list()).thenReturn(Lists.<ProductLicense>newArrayList());

        when(jiraLicenseStore.retrieve()).thenReturn("a3");

        MultiLicenseStoreImpl multiLicenseStore = new MultiLicenseStoreImpl(entityEngine, jiraLicenseStore, featureManager);
        Iterable<String> retrieve = multiLicenseStore.retrieve();

        assertTrue(Iterables.contains(retrieve, "a3"));
        assertEquals(Iterables.size(retrieve), 1);
    }

    @Test
    public void darkFeatureRequiredForStore()
    {
        when(featureManager.isEnabled(CoreFeatures.LICENSE_ROLES_ENABLED)).thenReturn(false);

        MultiLicenseStoreImpl multiLicenseStore = new MultiLicenseStoreImpl(entityEngine, jiraLicenseStore, featureManager);
        multiLicenseStore.store(ImmutableSet.of("d"));

        verify(jiraLicenseStore).store("d");
        verifyNoMoreInteractions(entityEngine);
    }

    @Test
    public void darkFeatureRequiredForRetrieve()
    {
        when(featureManager.isEnabled(CoreFeatures.LICENSE_ROLES_ENABLED)).thenReturn(false);

        when(jiraLicenseStore.retrieve()).thenReturn("e");
        MultiLicenseStoreImpl multiLicenseStore = new MultiLicenseStoreImpl(entityEngine, jiraLicenseStore, featureManager);
        Iterable<String> retrieve = multiLicenseStore.retrieve();

        assertTrue(Iterables.contains(retrieve, "e"));
        assertEquals(1, Iterables.size(retrieve));

        verifyNoMoreInteractions(entityEngine);
    }

    @Test
    public void onlyFirstLicenseEncounteredStoredInLegacyMode()
    {
        when(featureManager.isEnabled(CoreFeatures.LICENSE_ROLES_ENABLED)).thenReturn(false);

        MultiLicenseStoreImpl multiLicenseStore = new MultiLicenseStoreImpl(entityEngine, jiraLicenseStore, featureManager);
        multiLicenseStore.store(Lists.newArrayList("f", null));

        verify(jiraLicenseStore).store("f");
        verifyNoMoreInteractions(jiraLicenseStore);
    }

    @Test
    public void legacyBehaviourStoresNullForNoLicense()
    {
        when(featureManager.isEnabled(CoreFeatures.LICENSE_ROLES_ENABLED)).thenReturn(false);

        MultiLicenseStoreImpl multiLicenseStore = new MultiLicenseStoreImpl(entityEngine, jiraLicenseStore, featureManager);
        multiLicenseStore.store(Lists.<String>newArrayList()); // this should store a null - in production this should fail.

        verify(jiraLicenseStore).store(null);
        verifyNoMoreInteractions(jiraLicenseStore);
    }

    @Test(expected = IllegalArgumentException.class)
    public void noLicenseFails()
    {
        when(featureManager.isEnabled(CoreFeatures.LICENSE_ROLES_ENABLED)).thenReturn(true);

        MultiLicenseStoreImpl multiLicenseStore = new MultiLicenseStoreImpl(entityEngine, jiraLicenseStore, featureManager);
        multiLicenseStore.store(Lists.<String>newArrayList()); // this should store a null - in production this should fail.
    }
}
