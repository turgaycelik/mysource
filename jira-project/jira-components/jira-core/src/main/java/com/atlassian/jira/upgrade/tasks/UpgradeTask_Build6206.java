package com.atlassian.jira.upgrade.tasks;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.StatusCategoryManager;
import com.atlassian.jira.entity.Select;
import com.atlassian.jira.entity.Update;
import com.atlassian.jira.issue.status.category.StatusCategory;
import com.atlassian.jira.issue.status.category.StatusCategoryMapper;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;

import com.google.common.base.Function;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

/**
 * Sets status categories for all statuses in a system.
 * Explicitly sets Atlassian defined statuses, then makes a best guess at user-defined ones.
 *
 * @since v6.2
 */
public class UpgradeTask_Build6206 extends AbstractUpgradeTask
{
    private static final Logger log = Logger.getLogger(UpgradeTask_Build6206.class);
    private static final String ENTITY_STATUS = "Status";
    private static final String STATUS_CATEGORY_COLUMN = "statuscategory";

    private final StatusCategoryManager statusCategoryManager;
    private final WorkflowManager workflowManager;
    private final ConstantsManager constantsManager;
    private final StatusCategoryMapper mapper;

    public UpgradeTask_Build6206(final WorkflowManager workflowManager, StatusCategoryManager statusCategoryManager, final ConstantsManager constantsManager)
    {
        super(false);
        this.workflowManager = workflowManager;
        this.statusCategoryManager = statusCategoryManager;
        this.constantsManager = constantsManager;
        this.mapper = new StatusCategoryMapper(statusCategoryManager);
    }

    @Override
    public String getBuildNumber()
    {
        return "6206";
    }

    @Override
    public String getShortDescription()
    {
        return "Sets status categories for all statuses in a system."
                + " Explicitly sets Atlassian defined statuses, then makes a best guess at user-defined ones.";
    }

    @Override
    public void doUpgrade(final boolean setupMode) throws Exception
    {
        final StatusCategory defaultStatusCategory = statusCategoryManager.getDefaultStatusCategory();
        final StatusCategory newCategory = statusCategoryManager.getStatusCategoryByKey(StatusCategory.TO_DO);
        final StatusCategory completedCategory = statusCategoryManager.getStatusCategoryByKey(StatusCategory.COMPLETE);
        final StatusCategory inProgressCategory = statusCategoryManager.getStatusCategoryByKey(StatusCategory.IN_PROGRESS);

        log.info("Setting default status categories...");
        setStatusToCategory("1", newCategory.getId()); // Open
        setStatusToCategory("4", newCategory.getId()); // Reopened
        setStatusToCategory("3", inProgressCategory.getId()); // In Progress
        setStatusToCategory("5", completedCategory.getId()); // Resolved
        setStatusToCategory("6", completedCategory.getId()); // Closed

        log.info("Finding statuses with no assigned status category semantics...");
        List<GenericValue> statusesWithUndefinedSemantics = Lists.newArrayList();
        statusesWithUndefinedSemantics.addAll(getEntityEngine().run(Select.from(ENTITY_STATUS).whereNull(STATUS_CATEGORY_COLUMN)).asList());
        statusesWithUndefinedSemantics.addAll(getEntityEngine().run(Select.from(ENTITY_STATUS).whereEqual(STATUS_CATEGORY_COLUMN, defaultStatusCategory.getId())).asList());
        List<String> statusIdsWithUndefinedSemantics = Lists.transform(statusesWithUndefinedSemantics, new Function<GenericValue, String>()
        {
            @Override
            public String apply(@Nullable final GenericValue input)
            {
                return (null == input) ? null : input.getString("id");
            }
        });

        Multimap<String,Long> statusIdsToCategories = ArrayListMultimap.create();

        log.info("Parsing workflows...");
        for (JiraWorkflow workflow : workflowManager.getWorkflows())
        {
            Map<String,StatusCategory> results = mapper.mapCategoriesToStatuses(workflow);
            log.debug(String.format("Found %d statuses for workflow %s", results.size(), workflow.getName()));

            for (final String statusId : results.keySet())
            {
                if (statusIdsWithUndefinedSemantics.contains(statusId))
                {
                    StatusCategory category = results.get(statusId);
                    statusIdsToCategories.put(statusId, category.getId());
                    log.debug(String.format("Status '%s' could be assigned to the '%s' category", statusId, category.getKey()));
                }
                else
                {
                    log.debug(String.format("Status '%s' has predefined semantics, skipping", statusId));
                }
            }
        }

        log.debug(String.format("Found %d statuses with %d potential values", statusIdsToCategories.size(), statusIdsToCategories.values().size()));
        log.info(String.format("Assigning categories to %d statuses...", statusIdsToCategories.size()));

        for (String statusId : statusIdsToCategories.keySet())
        {
            // Apply in order of rarity.
            final Long categoryId;
            if (statusIdsToCategories.containsEntry(statusId, completedCategory.getId()))
            {
                categoryId = completedCategory.getId();
            }
            else if (statusIdsToCategories.containsEntry(statusId, newCategory.getId()))
            {
                categoryId = newCategory.getId();
            }
            else
            {
                categoryId = inProgressCategory.getId();
            }

            setStatusToCategory(statusId, categoryId);
        }

        // And now invalidate the cache.
        constantsManager.invalidateAll();
    }

    private void setStatusToCategory(final String statusId, final Long newCategoryId)
    {
        Update.into(ENTITY_STATUS).set(STATUS_CATEGORY_COLUMN, newCategoryId).whereEqual("id", statusId).execute(getEntityEngine());
    }
}
