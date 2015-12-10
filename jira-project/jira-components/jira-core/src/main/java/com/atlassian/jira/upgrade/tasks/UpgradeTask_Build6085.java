package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.entity.EntityEngine;
import com.atlassian.jira.entity.Select;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.util.UtilMisc;

import java.util.List;

/**
 * This upgrade task checks whether the clone link descriptions have been swapped (specifically the check introduced
 * in 5.2.6 to resolve JRA-24563 is used), and sets the "jira.clone.link.legacy.direction" flag to true if they have. The
 * intent of this upgrade task is to preserve a change in clone link behaviour introduced in JRA-24563 for users that
 * might have already compensated for it.
 * <p/>
 * JRADEV-18245: Workflow permissions are probably wrong for renamed users
 * JRA-24563: Clone operation creates links in the wrong direction
 *
 * @since v6.0
 */
public class UpgradeTask_Build6085 extends AbstractUpgradeTask
{
    public static final int BOOLEAN = 1;
    public static final int JIRA_PROPERTIES = 1;
    private EntityEngine entityEngine;
    private OfBizDelegator ofBizDelegator;

    public UpgradeTask_Build6085(EntityEngine entityEngine, OfBizDelegator ofBizDelegator)
    {
        super(false);
        this.entityEngine = entityEngine;
        this.ofBizDelegator = ofBizDelegator;
    }

    @Override
    public String getBuildNumber()
    {
        return "6085";
    }

    @Override
    public String getShortDescription()
    {
        return "This upgrade task checks whether the clone link descriptions have been swapped and sets the jira.clone.link.legacy.direction flag to true if they have.";
    }

    @Override
    public void doUpgrade(final boolean setupMode) throws Exception
    {
        if (legacyCloneDirectionPropertySet())
        {
            return;
        }
        if (cloneLinkDirectionsReversed())
        {
            setLegacyCloneDirectionProperty(true);
        }
        else
        {
            setLegacyCloneDirectionProperty(false);
        }
    }

    private boolean legacyCloneDirectionPropertySet()
    {
        final Select.WhereContext idQuery =
                Select.from("OSPropertyEntry").whereEqual("propertyKey", UpgradeTask_Build849.JIRA_CLONE_LINK_LEGACY_DIRECTION);
        return !entityEngine.run(idQuery).asList().isEmpty();
    }

    private boolean cloneLinkDirectionsReversed()
    {
        final Select.WhereContext query = Select.from("IssueLinkType")
                .whereEqual("linkname", "Cloners");

        List clonersList = entityEngine.run(query).asList();
        if (clonersList.isEmpty())
        {
            return false;
        }
        final GenericValue clonersEntry = (GenericValue) clonersList.iterator().next();
        return clonersEntry.getString("outward").trim().toLowerCase().equals("is cloned by")
                || clonersEntry.getString("inward").trim().toLowerCase().equals("clones");
    }

    private void setLegacyCloneDirectionProperty(boolean on)
    {
        final GenericValue propertyEntry = ofBizDelegator.createValue("OSPropertyEntry",
                UtilMisc.toMap("entityId", JIRA_PROPERTIES,
                        "entityName", "jira.properties",
                        "type", BOOLEAN,
                        "propertyKey", UpgradeTask_Build849.JIRA_CLONE_LINK_LEGACY_DIRECTION));
        ofBizDelegator.createValue("OSPropertyNumber", UtilMisc.toMap("id", propertyEntry.getLong("id"), "value", on ? 1 : 0));
    }
}
