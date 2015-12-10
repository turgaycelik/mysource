package com.atlassian.jira.upgrade.tasks;

import java.util.Arrays;

import com.atlassian.jira.entity.EntityEngineImpl;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.util.collect.MapBuilder;

import org.junit.Test;

public class TestUpgradeTask_Build6306
{

    private static final String DRAFT_ENTITY = "DraftWorkflowSchemeEntity";
    private static final String DRAFT_SCHEME = "DraftWorkflowScheme";

    @Test
    public void testOrphanedDraftWorkflowSchemeEntityRemoved() throws Exception {
        final MockGenericValue validDraftMapping = new MockGenericValue(DRAFT_ENTITY, MapBuilder.<String, Object>build(
                "id", 10001L,
                "scheme", 10000L,
                "workflow", "workflo rida",
                "issuetype", 0L
        ));
        final MockGenericValue orphanedDraftMapping = new MockGenericValue(DRAFT_ENTITY, MapBuilder.<String, Object>build(
                "id", 10000L,
                "scheme", 10001L,
                "workflow", "classic default workflow",
                "issuetype", 0L
        ));
        final MockGenericValue existingScheme = new MockGenericValue(DRAFT_SCHEME, MapBuilder.<String, Object>build(
                "id", 10000L
        ));

        final MockOfBizDelegator delegator = new MockOfBizDelegator(
                Arrays.asList(existingScheme, validDraftMapping, orphanedDraftMapping),
                Arrays.asList(existingScheme, validDraftMapping));

        new UpgradeTask_Build6306(new EntityEngineImpl(delegator)).doUpgrade(false);

        delegator.verifyAll();
    }
}
