/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.appconsistency.integrity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.atlassian.jira.appconsistency.integrity.check.Check;
import com.atlassian.jira.appconsistency.integrity.check.EntityCheck;
import com.atlassian.jira.appconsistency.integrity.check.FieldLayoutCheck;
import com.atlassian.jira.appconsistency.integrity.check.FilterSubscriptionsSavedFilterCheck;
import com.atlassian.jira.appconsistency.integrity.check.FilterSubscriptionsScheduleCheck;
import com.atlassian.jira.appconsistency.integrity.check.IssueLinkCheck;
import com.atlassian.jira.appconsistency.integrity.check.PrimaryEntityRelationCreate;
import com.atlassian.jira.appconsistency.integrity.check.PrimaryEntityRelationDelete;
import com.atlassian.jira.appconsistency.integrity.check.SchemePermissionCheck;
import com.atlassian.jira.appconsistency.integrity.check.SearchRequestRelationCheck;
import com.atlassian.jira.appconsistency.integrity.check.WorkflowCurrentStepCheck;
import com.atlassian.jira.appconsistency.integrity.check.WorkflowIssueStatusNull;
import com.atlassian.jira.appconsistency.integrity.check.WorkflowStateCheck;
import com.atlassian.jira.appconsistency.integrity.integritycheck.EntityIntegrityCheckImpl;
import com.atlassian.jira.appconsistency.integrity.integritycheck.FieldLayoutIntegrityCheck;
import com.atlassian.jira.appconsistency.integrity.integritycheck.FilterSubscriptionIntegrityCheck;
import com.atlassian.jira.appconsistency.integrity.integritycheck.IntegrityCheck;
import com.atlassian.jira.appconsistency.integrity.integritycheck.SchemePermissionIntegrityCheck;
import com.atlassian.jira.appconsistency.integrity.integritycheck.SearchRequestRelationIntegrityCheck;
import com.atlassian.jira.appconsistency.integrity.integritycheck.WorkflowStateIntegrityCheck;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.scheduler.SchedulerService;

public class IntegrityCheckManagerImpl implements IntegrityCheckManager
{
    private final Map<Long, IntegrityCheck> integrityChecks = new HashMap<Long, IntegrityCheck>();
    private final Map<Long, Check> checks = new HashMap<Long, Check>();
    private final ApplicationProperties applicationProperties;
    private final SchedulerService schedulerService;
    private final I18nHelper.BeanFactory i18nFactory;
    private int checkId = 1;
    private int integrityCheckId = 1;

    public IntegrityCheckManagerImpl(final OfBizDelegator ofbizDelegator, final I18nHelper.BeanFactory i18nFactory, final ApplicationProperties applicationProperties, final SchedulerService schedulerService)
    {        
        this.i18nFactory = i18nFactory;
        this.applicationProperties = applicationProperties;
        this.schedulerService = schedulerService;
        // Currently hard code the checks because this is easiest.
        final IntegrityCheck issueRelationsCheck = getIssueRelationsCheck(ofbizDelegator);
        final IntegrityCheck searchRequestRelationsCheck = getSearchRequestRelationsCheck(ofbizDelegator);
        final IntegrityCheck schemePermissionCheck = getSchemePermissionCheck(ofbizDelegator);
        final IntegrityCheck workflowStateCheck = getWorkflowIntegrityCheck(ofbizDelegator);
        final IntegrityCheck fieldLayoutCheck = getFieldLayoutIntegrityCheck(ofbizDelegator);
        final IntegrityCheck filterSubscriptionCheck = getFilterSubscriptionIntegrityCheck(ofbizDelegator);

        integrityChecks.put(issueRelationsCheck.getId(), issueRelationsCheck);
        integrityChecks.put(searchRequestRelationsCheck.getId(), searchRequestRelationsCheck);
        integrityChecks.put(schemePermissionCheck.getId(), schemePermissionCheck);
        integrityChecks.put(workflowStateCheck.getId(), workflowStateCheck);
        integrityChecks.put(fieldLayoutCheck.getId(), fieldLayoutCheck);
        integrityChecks.put(filterSubscriptionCheck.getId(), filterSubscriptionCheck);
    }

    public List<IntegrityCheck> getIntegrityChecks()
    {
        final List<IntegrityCheck> result = new ArrayList<IntegrityCheck>(integrityChecks.values());
        Collections.sort(result);
        return result;
    }

    public Check getCheck(final Long checkId)
    {
        return checks.get(checkId);
    }

    public IntegrityCheck getIntegrityCheck(final Long id)
    {
        return integrityChecks.get(id);
    }

    private IntegrityCheck getIssueRelationsCheck(final OfBizDelegator ofBizDelegator)
    {
        final List<EntityCheck> issueChecks = new ArrayList<EntityCheck>();

        // Create Checks for (Issue Relations)
        EntityCheck check = new PrimaryEntityRelationDelete(ofBizDelegator, checkId++, "Parent", "Project");
        issueChecks.add(check);
        checks.put(check.getId(), check);
        check = new PrimaryEntityRelationCreate(ofBizDelegator, checkId++, "Related", "OSWorkflowEntry", "workflowId", FieldMap.build("name", "jira",
                "state", new Integer(0)));
        issueChecks.add(check);
        checks.put(check.getId(), check);
        check = new IssueLinkCheck(ofBizDelegator, checkId++);
        issueChecks.add(check);
        checks.put(check.getId(), check);

        // Create Issue Relations Checks
        return new EntityIntegrityCheckImpl(integrityCheckId++, getI18nHelper().getText("admin.integrity.check.entity.relation.desc"), "Issue", issueChecks);
    }

    private IntegrityCheck getFilterSubscriptionIntegrityCheck(final OfBizDelegator ofBizDelegator)
    {
        final List<Check> filterChecks = new ArrayList<Check>();
        Check check = new FilterSubscriptionsScheduleCheck(ofBizDelegator, schedulerService, checkId++);
        filterChecks.add(check);
        checks.put(check.getId(), check);
        check = new FilterSubscriptionsSavedFilterCheck(ofBizDelegator, checkId++);
        filterChecks.add(check);
        checks.put(check.getId(), check);

        return new FilterSubscriptionIntegrityCheck(integrityCheckId++, getI18nHelper().getText("admin.integrity.check.filter.subscriptions.desc"),
            filterChecks);
    }

    private IntegrityCheck getWorkflowIntegrityCheck(final OfBizDelegator ofBizDelegator)
    {
        final Check check = new WorkflowStateCheck(ofBizDelegator, checkId++);
        checks.put(check.getId(), check);
        final Check check2 = new WorkflowCurrentStepCheck(ofBizDelegator, checkId++);
        checks.put(check2.getId(), check2);
        final Check check3 = new WorkflowIssueStatusNull(ofBizDelegator, checkId++);
        checks.put(check3.getId(), check3);

        return new WorkflowStateIntegrityCheck(integrityCheckId++, getI18nHelper().getText("admin.integrity.check.workflow.state.desc"), check, check2,
            check3);
    }

    private IntegrityCheck getFieldLayoutIntegrityCheck(final OfBizDelegator ofBizDelegator)
    {
        final Check check = new FieldLayoutCheck(ofBizDelegator, checkId++);
        checks.put(check.getId(), check);

        return new FieldLayoutIntegrityCheck(integrityCheckId++, getI18nHelper().getText("admin.integrity.check.field.layout.desc"), check);
    }

    private IntegrityCheck getSearchRequestRelationsCheck(final OfBizDelegator ofBizDelegator)
    {
        final Check check = new SearchRequestRelationCheck(ofBizDelegator, checkId++);
        checks.put(check.getId(), check);

        return new SearchRequestRelationIntegrityCheck(integrityCheckId++, getI18nHelper().getText("admin.integrity.check.search.request.relation.desc"),
            check);
    }

    private IntegrityCheck getSchemePermissionCheck(final OfBizDelegator ofBizDelegator)
    {
        final Check check = new SchemePermissionCheck(ofBizDelegator, checkId++);
        checks.put(check.getId(), check);

        return new SchemePermissionIntegrityCheck(integrityCheckId++, getI18nHelper().getText("admin.integrity.check.scheme.permission.desc"), check);
    }
    
    private I18nHelper getI18nHelper()
    {
        return i18nFactory.getInstance(applicationProperties.getDefaultLocale());
    }
}
