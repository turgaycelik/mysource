package com.atlassian.jira.upgrade.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.MapBuilder;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

/**
 * Update the file paths of default icons for Statuses, Issue Types, Priorities and Sub Tasks.
 * The default locations can be found in {@literal /jira-webapp/src/main/webapp/WEB-INF/iconimages.properties}.
 */
public class UpgradeTask_Build843 extends AbstractUpgradeTask
{
    private static final Logger log = Logger.getLogger(UpgradeTask_Build843.class);
    private OfBizDelegator ofBizDelegator;
    private ConstantsManager constantsManager;

    public UpgradeTask_Build843(OfBizDelegator ofBizDelegator, ConstantsManager constantsManager)
    {
        super(false);
        this.ofBizDelegator = ofBizDelegator;
        this.constantsManager = constantsManager;
    }

    @Override
    public String getBuildNumber()
    {
        return "843";
    }

    @Override
    public String getShortDescription()
    {
        return "Updating file paths of default icons for statuses, issue types, priorities and sub-tasks.";
    }

    @Override
    public void doUpgrade(boolean setupMode) throws Exception
    {
        upgradeIconPathsForEntity("IssueType", issueTypeIconPaths);
        upgradeIconPathsForEntity("IssueType", subtaskIconPaths);
        upgradeIconPathsForEntity("Status", statusIconPaths);
        upgradeIconPathsForEntity("Priority", priorityIconPaths);

        constantsManager.refresh();
    }

    private void upgradeIconPathsForEntity(String entityName, Map<String,String> iconPaths) throws GenericEntityException
    {
        final List<String> oldIconPaths = new ArrayList<String>(iconPaths.keySet());
        final EntityCondition entityCondition = new EntityExpr(iconFieldName, EntityOperator.IN, oldIconPaths);
        final List<GenericValue> iconsToUpdate = ofBizDelegator.findByCondition(entityName, entityCondition, CollectionBuilder.list("id", iconFieldName), null);

        log.debug("Found " + iconsToUpdate.size() + " '" + entityName + "' icons with a default icon url.");
        for (GenericValue gv : iconsToUpdate)
        {
            changeIconPath(gv, iconPaths);
        }
    }

    private static void changeIconPath(GenericValue gv, Map<String,String> iconPaths) throws GenericEntityException
    {
        if (gv == null)
        {
            log.warn("Cannot update null generic value.");
        }
        else
        {
            final String currentPath = gv.getString(iconFieldName);
            if (StringUtils.isBlank(currentPath))
            {
                log.warn("Current icon url for '" + gv.getString("id") + "' was blank. Skipping.");
                return;
            }
            final String newPath = iconPaths.get(currentPath);

            if (StringUtils.isBlank(newPath))
            {
                log.warn("Current icon url '" + currentPath + "' has no corresponding new url. Skipping.");
                return;
            }

            log.debug("Updating icon url of '" + gv.getString("id") + "' from '" + currentPath + "' to '" + newPath + "'.");
            gv.setString(iconFieldName, newPath);
            gv.store();
        }
    }

    private static final String iconFieldName = "iconurl";

    private static final Map<String, String> statusIconPaths = MapBuilder.<String, String>newBuilder()
        .add("/images/icons/status_assigned.gif","/images/icons/statuses/assigned.png")
        .add("/images/icons/status_closed.gif","/images/icons/statuses/closed.png")
        .add("/images/icons/status_document.gif","/images/icons/statuses/document.png")
        .add("/images/icons/status_down.gif","/images/icons/statuses/down.png")
        .add("/images/icons/status_email.gif","/images/icons/statuses/email.png")
        .add("/images/icons/status_generic.gif","/images/icons/statuses/generic.png")
        .add("/images/icons/status_information.gif","/images/icons/statuses/information.png")
        .add("/images/icons/status_inprogress.gif","/images/icons/statuses/inprogress.png")
        .add("/images/icons/status_invisible.gif","/images/icons/statuses/invisible.png")
        .add("/images/icons/status_needinfo.gif","/images/icons/statuses/needinfo.png")
        .add("/images/icons/status_open.gif","/images/icons/statuses/open.png")
        .add("/images/icons/status_reopened.gif","/images/icons/statuses/reopened.png")
        .add("/images/icons/status_resolved.gif","/images/icons/statuses/resolved.png")
        .add("/images/icons/status_trash.gif","/images/icons/statuses/trash.png")
        .add("/images/icons/status_unassigned.gif","/images/icons/statuses/unassigned.png")
        .add("/images/icons/status_up.gif","/images/icons/statuses/up.png")
        .add("/images/icons/status_visible.gif","/images/icons/statuses/visible.png")
        .toHashMap();

    private static final Map<String, String> issueTypeIconPaths = MapBuilder.<String, String>newBuilder()
        .add("/images/icons/bug.gif","/images/icons/issuetypes/bug.png")
        .add("/images/icons/improvement.gif","/images/icons/issuetypes/improvement.png")
        .add("/images/icons/newfeature.gif","/images/icons/issuetypes/newfeature.png")
        .add("/images/icons/task.gif","/images/icons/issuetypes/task.png")
        .add("/images/icons/genericissue.gif","/images/icons/issuetypes/genericissue.png")
        .add("/images/icons/blank.gif","/images/icons/issuetypes/blank.png")
        .add("/images/icons/delete.gif","/images/icons/issuetypes/delete.png")
        .add("/images/icons/documentation.gif","/images/icons/issuetypes/documentation.png")
        .add("/images/icons/exclamation.gif","/images/icons/issuetypes/exclamation.png")
        .add("/images/icons/health.gif","/images/icons/issuetypes/health.png")
        .add("/images/icons/removefeature.gif","/images/icons/issuetypes/remove_feature.png")
        .add("/images/icons/requirement.gif","/images/icons/issuetypes/requirement.png")
        .add("/images/icons/sales.gif","/images/icons/issuetypes/sales.png")
        .add("/images/icons/subtask.gif","/images/icons/issuetypes/subtask.png")
        .add("/images/icons/undefined.gif","/images/icons/issuetypes/undefined.png")
        .add("/images/icons/ico_defect.png","/images/icons/issuetypes/defect.png")
        .add("/images/icons/ico_epic.png","/images/icons/issuetypes/epic.png")
        .add("/images/icons/ico_story.png","/images/icons/issuetypes/story.png")
        .add("/images/icons/ico_task.png","/images/icons/issuetypes/task_agile.png")
        .toHashMap();

    private static final Map<String, String> priorityIconPaths = MapBuilder.<String, String>newBuilder()
        .add("/images/icons/priority_blocker.gif","/images/icons/priorities/blocker.png")
        .add("/images/icons/priority_critical.gif","/images/icons/priorities/critical.png")
        .add("/images/icons/priority_major.gif","/images/icons/priorities/major.png")
        .add("/images/icons/priority_minor.gif","/images/icons/priorities/minor.png")
        .add("/images/icons/priority_trivial.gif","/images/icons/priorities/trivial.png")
        .toHashMap();

    private static final Map<String, String> subtaskIconPaths = MapBuilder.<String, String>newBuilder()
        .add("/images/icons/issue_subtask.gif","/images/icons/issuetypes/subtask_alternate.png")
        .toHashMap();
}
