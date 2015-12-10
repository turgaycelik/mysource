package com.atlassian.jira.issue.fields.config.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.context.persistence.FieldConfigContextPersister;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.persistence.FieldConfigSchemePersister;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.local.MockControllerTestCase;

import com.google.common.collect.Lists;
import com.mockobjects.constraint.Constraint;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;

import org.easymock.MockControl;
import org.junit.Test;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

@SuppressWarnings("deprecation")
public class TestFieldConfigSchemeManagerImpl extends MockControllerTestCase
{
    @Test
    public void testGetConfigSchemeForFieldConfig() throws Exception
    {
        final FieldConfigContextPersister fieldConfigContextPersister = mockController.getMock(FieldConfigContextPersister.class);
        final FieldConfigSchemePersister fieldConfigSchemePersister = mockController.getMock(FieldConfigSchemePersister.class);

        final FieldConfig fieldConfig = mockController.getMock(FieldConfig.class);
        final FieldConfigScheme configScheme = mockController.getMock(FieldConfigScheme.class);

        fieldConfigSchemePersister.getConfigSchemeForFieldConfig(fieldConfig);
        mockController.setReturnValue(configScheme);

        mockController.replay();

        final FieldConfigSchemeManagerImpl configSchemeManager = new FieldConfigSchemeManagerImpl(fieldConfigSchemePersister,
            fieldConfigContextPersister, null, null);
        configSchemeManager.getConfigSchemeForFieldConfig(fieldConfig);

        mockController.verify();
    }

    @Test
    public void testGetConfigSchemeForFieldConfigDataAccessException() throws Exception
    {
        final FieldConfigContextPersister fieldConfigContextPersister = mockController.getMock(FieldConfigContextPersister.class);
        final FieldConfigSchemePersister fieldConfigSchemePersister = mockController.getMock(FieldConfigSchemePersister.class);

        final FieldConfig fieldConfig = mockController.getMock(FieldConfig.class);

        fieldConfigSchemePersister.getConfigSchemeForFieldConfig(fieldConfig);
        mockController.setThrowable(new DataAccessException("blarg"));

        mockController.replay();

        final FieldConfigSchemeManagerImpl configSchemeManager = new FieldConfigSchemeManagerImpl(fieldConfigSchemePersister,
            fieldConfigContextPersister, null, null);
        assertNull(configSchemeManager.getConfigSchemeForFieldConfig(fieldConfig));

        mockController.verify();
    }

    @Test
    public void testNullArgumentToGetInvalidFieldConfigSchemesForIssueTypeRemoval()
    {
        final FieldConfigSchemeManagerImpl fcsm = new FieldConfigSchemeManagerImpl(null, null, null, null);
        try
        {
            fcsm.getInvalidFieldConfigSchemesForIssueTypeRemoval(null);
            fail("getInvalidFieldConfigSchemesForIssueTypeRemoval - should have thrown (IllegalArgumentException");
        }
        catch (final IllegalArgumentException e)
        {}
    }

    @Test
    public void testNullArgumentToRemoveInvalidFieldConfigSchemesForIssueType()
    {
        final FieldConfigSchemeManagerImpl fcsm = new FieldConfigSchemeManagerImpl(null, null, null, null);
        try
        {
            fcsm.removeInvalidFieldConfigSchemesForIssueType(null);
            fail("removeInvalidFieldConfigSchemesForIssueType - should have thrown IllegalArgumentException ");
        }
        catch (final IllegalArgumentException e)
        {}
    }

    @Test
    public void testCallThrough()
    {
        final Mock mockIssueType = getMockIssueType("123");
        final IssueType issueType = (IssueType) mockIssueType.proxy();

        final Mock mockFieldConfigSchemePersister = new Mock(FieldConfigSchemePersister.class);
        mockFieldConfigSchemePersister.expectAndReturn("getInvalidFieldConfigSchemeAfterIssueTypeRemoval", new Constraint[] { P.eq(issueType) },
            Collections.EMPTY_LIST);

        final FieldConfigSchemeManagerImpl fcsm = new FieldConfigSchemeManagerImpl(
            (FieldConfigSchemePersister) mockFieldConfigSchemePersister.proxy(), null, null, null);
        fcsm.getInvalidFieldConfigSchemesForIssueTypeRemoval(issueType);

        mockFieldConfigSchemePersister.verify();
    }

    @Test
    public void testRemoveInvalidFieldConfigSchemesForIssueType()
    {
        final Mock mockIssueType = getMockIssueType("123");
        final IssueType issueType = (IssueType) mockIssueType.proxy();

        final List<FieldConfigScheme> fcsList = new ArrayList<FieldConfigScheme>();
        fcsList.add(getFieldConfigScheme(1001));
        fcsList.add(getFieldConfigScheme(1002));
        fcsList.add(getFieldConfigScheme(1003));

        final MockControl<FieldConfigContextPersister> mockFieldConfigContextPersister = MockControl.createStrictControl(FieldConfigContextPersister.class);
        final FieldConfigContextPersister fieldConfigContextPersister = mockFieldConfigContextPersister.getMock();

        final MockControl<FieldConfigManager> mockFieldConfigManager = MockControl.createControl(FieldConfigManager.class);
        final FieldConfigManager fieldConfigManager = mockFieldConfigManager.getMock();

        final MockControl<FieldConfigSchemePersister> mockFieldConfigSchemePersister = MockControl.createStrictControl(FieldConfigSchemePersister.class);
        final FieldConfigSchemePersister fieldConfigSchemePersister = mockFieldConfigSchemePersister.getMock();
        fieldConfigSchemePersister.getInvalidFieldConfigSchemeAfterIssueTypeRemoval(issueType);
        mockFieldConfigSchemePersister.setReturnValue(fcsList);

        for (final FieldConfigScheme fcs : fcsList)
        {
            fieldConfigSchemePersister.getFieldConfigScheme(fcs.getId());
            mockFieldConfigSchemePersister.setReturnValue(fcs);
            fieldConfigContextPersister.removeContextsForConfigScheme(fcs);
            fieldConfigManager.removeConfigsForConfigScheme(fcs.getId());
            fieldConfigSchemePersister.remove(fcs.getId());
        }
        fieldConfigSchemePersister.removeByIssueType(issueType);

        mockFieldConfigContextPersister.replay();
        mockFieldConfigManager.replay();
        mockFieldConfigSchemePersister.replay();

        final FieldConfigSchemeManagerImpl fcsm = new FieldConfigSchemeManagerImpl(fieldConfigSchemePersister, fieldConfigContextPersister, null,
            fieldConfigManager);
        fcsm.removeInvalidFieldConfigSchemesForIssueType(issueType);

        mockFieldConfigContextPersister.verify();
        mockFieldConfigSchemePersister.verify();
    }

    @Test
    public void testRemoveInvalidFieldConfigSchemesForCustomField()
    {
        final MockControl<FieldConfigSchemePersister> mockFieldConfigSchemePersister = MockControl.createControl(FieldConfigSchemePersister.class);
        final FieldConfigSchemePersister persister = mockFieldConfigSchemePersister.getMock();
        persister.getConfigSchemeIdsForCustomFieldId("customfield_10000");
        mockFieldConfigSchemePersister.setReturnValue(Lists.newArrayList(234L, 567L));
        final FieldConfigScheme fcs1 = getFieldConfigScheme(234L);
        persister.getFieldConfigScheme(234L);
        mockFieldConfigSchemePersister.setReturnValue(fcs1);
        final FieldConfigScheme fcs2 = getFieldConfigScheme(567L);
        persister.getFieldConfigScheme(567L);
        mockFieldConfigSchemePersister.setReturnValue(fcs2);

        final FieldConfigContextPersister configContextPersister = mockController.getMock(FieldConfigContextPersister.class);
        configContextPersister.removeContextsForConfigScheme(fcs1);
        configContextPersister.removeContextsForConfigScheme(fcs2);
        final FieldConfigManager fieldConfigManager = mockController.getMock(FieldConfigManager.class);
        fieldConfigManager.removeConfigsForConfigScheme(234L);
        fieldConfigManager.removeConfigsForConfigScheme(567L);
        persister.remove(234L);
        persister.remove(567L);

        mockFieldConfigSchemePersister.replay();
        mockController.replay();

        final FieldConfigSchemeManagerImpl manager = new FieldConfigSchemeManagerImpl(persister, configContextPersister, null, fieldConfigManager);
        manager.removeInvalidFieldConfigSchemesForCustomField("customfield_10000");

        mockController.verify();
        mockFieldConfigSchemePersister.verify();
    }

    private FieldConfigScheme getFieldConfigScheme(final long id)
    {
        return new FieldConfigScheme.Builder().setName("Name" + id).setDescription("Desc" + id).setId(id).toFieldConfigScheme();
    }

    private Mock getMockIssueType(final String id)
    {
        final Mock mockIssueType = new Mock(IssueType.class);
        mockIssueType.setStrict(true);
        mockIssueType.expectAndReturn("getId", id);
        return mockIssueType;
    }
}
