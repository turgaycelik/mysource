package com.atlassian.jira.upgrade.tasks;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.atlassian.jira.entity.EntityEngine;
import com.atlassian.jira.entity.Select;
import com.atlassian.jira.entity.Update;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.jira.workflow.DefaultWorkflowSchemeManager;
import com.atlassian.jira.workflow.WorkflowDescriptorStore;
import com.atlassian.jira.workflow.WorkflowUtil;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.opensymphony.workflow.FactoryException;
import com.opensymphony.workflow.loader.WorkflowDescriptor;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

/**
 * Migrate the old default workflow to a new scheme/workflow
 *
 * Lives in stable under build number 6097
 *
 * @since v6.1
 */
public class UpgradeTask_Build6123 extends AbstractUpgradeTask
{
    public static final int ALL_UNASSIGNED_ISSUE_TYPES = 0;
    private final OfBizDelegator ofBizDelegator;
    private final EntityEngine entityEngine;
    private final WorkflowDescriptorStore descriptorStore;
    private final DefaultWorkflowSchemeManager schemeManager;

    private enum WorkflowSchemeStatus
    {
        DRAFT
                {
                    @Override
                    String schemeTableName()
                    {
                        return "DraftWorkflowScheme";
                    }

                    @Override
                    String schemeEntityTableName()
                    {
                        return "DraftWorkflowSchemeEntity";
                    }
                },
        LIVE
                {
                    @Override
                    String schemeTableName()
                    {
                        return "WorkflowScheme";
                    }

                    @Override
                    String schemeEntityTableName()
                    {
                        return "WorkflowSchemeEntity";
                    }
                };

        abstract String schemeTableName();
        abstract String schemeEntityTableName();
    }

    public UpgradeTask_Build6123(OfBizDelegator ofBizDelegator, WorkflowDescriptorStore descriptorStore,
            EntityEngine entityEngine, DefaultWorkflowSchemeManager schemeManager)
    {
        super(false);
        this.ofBizDelegator = ofBizDelegator;
        this.descriptorStore = descriptorStore;
        this.entityEngine = entityEngine;
        this.schemeManager = schemeManager;
    }

    @Override
    public String getBuildNumber()
    {
        return "6123";
    }

    @Override
    public String getShortDescription()
    {
        return "Migrate the old default workflow to a new scheme/workflow";
    }

    @Override
    public void doUpgrade(final boolean setupMode) throws Exception
    {
        String classicName = createClassicWorkflow();
        long classicSchemeId = createClassicWorkflowScheme();
        associateWorkflowWithScheme(classicName, classicSchemeId);
        associateSchemeWithProjects(classicSchemeId);
        associateIssueTypesWithScheme(classicName, WorkflowSchemeStatus.DRAFT);
        associateIssueTypesWithScheme(classicName, WorkflowSchemeStatus.LIVE);

        // The scheme manager keeps its own cache, which will not be cleared of the old schemes
        // unless we explicity trigger it here.
        schemeManager.onClearCache(ClearCacheEvent.INSTANCE);
    }

    private void associateIssueTypesWithScheme(String classicWorkflowName, WorkflowSchemeStatus status)
    {
        // Update explicit references to 'jira' (old default) to refer to classic
        Update.WhereContext updateQuery = Update.into(status.schemeEntityTableName()).set("workflow", classicWorkflowName)
                .whereEqual("workflow", "jira");
        entityEngine.execute(updateQuery);

        List<GenericValue> allWorkflowIssueTypeMappingEntries = entityEngine.run(Select.columns("scheme", "issuetype")
                .from(status.schemeEntityTableName())).asList();
        SetMultimap<Long, Long> workflowIssueTypeMappings = HashMultimap.create();
        setDefaultIssueTypeMappingToClassicWorkflow(classicWorkflowName, allWorkflowIssueTypeMappingEntries, workflowIssueTypeMappings, status.schemeEntityTableName());
        setDefaultIssueTypeMappingForUnmappedSchemes(classicWorkflowName, workflowIssueTypeMappings, status);
    }

    private void setDefaultIssueTypeMappingForUnmappedSchemes(final String classicWorkflowName,
            final SetMultimap<Long, Long> workflowIssueTypeMappings,
            final WorkflowSchemeStatus issueTypeMappingTable)
    {
        Set<Long> allMappedSchemes = workflowIssueTypeMappings.keySet();
        for (GenericValue scheme : entityEngine.run(Select.columns("id").from(issueTypeMappingTable.schemeTableName())).asList())
        {
            final Long schemeId = scheme.getLong("id");
            if (!allMappedSchemes.contains(schemeId))
            {
                ofBizDelegator.createValue(issueTypeMappingTable.schemeEntityTableName(),
                        ImmutableMap.<String, Object>of("scheme", schemeId,
                                "workflow", classicWorkflowName, "issuetype", 0L));
            }
        }
    }

    private void setDefaultIssueTypeMappingToClassicWorkflow(final String classicWorkflowName,
            final List<GenericValue> allWorkflowIssueTypeMappingEntries,
            final SetMultimap<Long, Long> issueTypeMappings, final String issueTypeMappingTableName)
    {
        for (GenericValue mappingEntry : allWorkflowIssueTypeMappingEntries)
        {
            final Long schemeId = mappingEntry.getLong("scheme");
            final Long issueType = Long.parseLong(mappingEntry.getString("issuetype"));
            issueTypeMappings.put(schemeId, issueType);
        }

        for (Map.Entry<Long, Collection<Long>> mapping : issueTypeMappings.asMap().entrySet())
        {
            if (!mapping.getValue().contains(0L))
            {
                ofBizDelegator.createValue(issueTypeMappingTableName,
                        ImmutableMap.<String, Object>of("scheme", mapping.getKey(),
                                "workflow", classicWorkflowName, "issuetype", 0L));
            }
        }
    }

    private String createClassicWorkflow() throws FactoryException, IOException
    {
        WorkflowDescriptor descriptor = WorkflowUtil.convertXMLtoWorkflowDescriptor(defaultClassicXml());
        String workflowName = uniqueWorkflowName();
        descriptor.getMetaAttributes().put("jira.description", workflowDescription());

        // Using the store instead of talking directly to the db because we need the store to put it in its cache.
        descriptorStore.saveWorkflow(workflowName, descriptor, false);

        // Add information about the visual layout of the workflow, so it doesn't appear all jumbled.
        Long layoutEntryKey = ofBizDelegator.createValue("OSPropertyEntry",
                ImmutableMap.<String, Object>of("entityName", "com.atlassian.jira.plugins.jira-workflow-designer", "entityId", 1, "propertyKey", layoutKey(workflowName), "type", 6)).getLong("id");
        ofBizDelegator.createValue("OSPropertyText", ImmutableMap.<String, Object>of("id", layoutEntryKey, "value", defaultLayoutJson()));
        return workflowName;
    }

    private long createClassicWorkflowScheme()
    {
        ImmutableMap<String, Object> newSchemeValue = ImmutableMap.<String, Object>of("name",
                uniqueSchemeName(), "description", schemeDescription());
        return (Long) ofBizDelegator.createValue("WorkflowScheme", newSchemeValue).get("id");
    }

    private void associateWorkflowWithScheme(final String classicName, final long classicSchemeId)
    {
        ofBizDelegator.createValue("WorkflowSchemeEntity",
                ImmutableMap.<String, Object>of("scheme", classicSchemeId, "workflow", classicName,
                        "issuetype", ALL_UNASSIGNED_ISSUE_TYPES));
    }

    private void associateSchemeWithProjects(final long classicSchemeId) throws GenericEntityException
    {
        List<GenericValue> allProjects = entityEngine.run(Select.columns("id").from("Project")).asList();
        List<GenericValue> allProjectsWithSchemes = entityEngine.run(Select.columns("sourceNodeId")
                .from("NodeAssociation")
                .whereEqual("associationType", "ProjectScheme")
                .andEqual("sinkNodeEntity", "WorkflowScheme")).asList();
        Set<Long> withoutSchemes = Sets.newHashSet();

        for (GenericValue project : allProjects)
        {
            withoutSchemes.add(project.getLong("id"));
        }
        for (GenericValue project : allProjectsWithSchemes)
        {
            withoutSchemes.remove(project.getLong("sourceNodeId"));
        }

        GenericValue association;
        for (long projectId : withoutSchemes)
        {
            // NodeAssociation does not have an ID field, so we can't use ofBizDelegator.createValue()
            association = ofBizDelegator.makeValue("NodeAssociation");
            association.setFields(ImmutableMap.of(
                    "associationType", "ProjectScheme",
                    "sourceNodeId", projectId,
                    "sourceNodeEntity", "Project",
                    "sinkNodeId", classicSchemeId,
                    "sinkNodeEntity", "WorkflowScheme"));
            association.create();
        }
    }

    private String uniqueWorkflowName()
    {
        String name = i18n("admin.workflows.store.classic.name");
        List<String> workflowNames = entityEngine.run(Select.stringColumn("name").from("Workflow")).asList();
        return createUniqueName(workflowNames, name);
    }

    private String uniqueSchemeName()
    {
        String name = i18n("admin.workflowschemes.store.classic.name");
        List<String> schemeNames = entityEngine.run(Select.stringColumn("name").from("WorkflowScheme")).asList();
        return createUniqueName(schemeNames, name);
    }

    private String createUniqueName(Collection<String> names, String prefix)
    {
        int suffix = 0;
        String systemSuffix = " (" + i18n("admin.workflows.readonly.system") + ")";
        String newName = prefix;
        Set<String> potentialCollisions = new HashSet<String>();
        for (String schemeName : names)
        {
            if (schemeName.startsWith(prefix))
            {
                potentialCollisions.add(schemeName);
            }
        }
        if(potentialCollisions.contains(newName))
        {
            newName += systemSuffix;
            prefix = newName;
        }
        while (potentialCollisions.contains(newName))
        {
            suffix += 1;
            newName = prefix + " " + suffix;
        }
        return newName;
    }

    private String workflowDescription()
    {
        return i18n("admin.workflows.store.classic.description");
    }

    private String schemeDescription()
    {
        return i18n("admin.workflowschemes.store.classic.name");
    }

    private String i18n(String key)
    {
        return new I18nBean(Locale.getDefault()).getText(key);
    }

    private static String layoutKey (String workflowName)
    {
        return "jira.workflow.layout:" + DigestUtils.md5Hex(workflowName);
    }

    private String defaultClassicXml() throws IOException
    {
        return wholeFile("system-classic-workflow.xml");
    }

    private String defaultLayoutJson() throws IOException
    {
        return wholeFile("system-classic-workflow-layout.json");
    }

    private String wholeFile(String filename) throws IOException
    {
        InputStream stream = this.getClass().getClassLoader().getResourceAsStream(filename);
        try
        {
            return IOUtils.toString(stream, "utf-8");
        }
        finally
        {
            IOUtils.closeQuietly(stream);
        }
    }
}

