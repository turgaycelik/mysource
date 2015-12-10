package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.link.IssueLinkType;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.atlassian.jira.util.collect.MapBuilder;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.List;

/**
 * This upgrade task initializes link types to a set of predefined values.  Its defined as a setup=only and hence will
 * only run on new instances, not upgrades to existing instances
 *
 * @since v4.2
 */
public class UpgradeTask_Build572 extends AbstractUpgradeTask
{
    private final OfBizDelegator ofBizDelegator;
    private final ApplicationProperties applicationProperties;

    private static class LinkDef
    {
        private final String name;
        private final String outward;
        private final String inward;
        private final String style;

        private LinkDef(final String name, final String outward, final String inward, final String style)
        {
            this.name = name;
            this.outward = outward;
            this.inward = inward;
            this.style = style;
        }
    }


    public UpgradeTask_Build572(final OfBizDelegator ofBizDelegator, final ApplicationProperties applicationProperties)
    {
        super(false);
        this.ofBizDelegator = ofBizDelegator;
        this.applicationProperties = applicationProperties;
    }

    @Override
    public String getBuildNumber()
    {
        return "572";
    }

    @Override
    public String getShortDescription()
    {
        return "Initialize a default set of issue link types and turns issue linking on by default";
    }

    /**
     * This is a setup only task and hence can only run once!
     *
     * @throws Exception if something goes
     * @param setupMode
     */
    public void doUpgrade(boolean setupMode) throws Exception
    {
        if (setupMode)
        {
            String cloneName = getCloneLinkTypeName();

            List<LinkDef> linkDefs = new ArrayList<LinkDef>();
            linkDefs.add(new LinkDef("Blocks", "blocks", "is blocked by", null));
            linkDefs.add(new LinkDef(cloneName, "clones", "is cloned by", null));
            linkDefs.add(new LinkDef("Duplicate", "duplicates", "is duplicated by", null));
            linkDefs.add(new LinkDef("Relates", "relates to", "relates to", null));

            for (LinkDef linkDefiniton : linkDefs)
            {
                List<GenericValue> gv = ofBizDelegator.findByAnd(OfBizDelegator.ISSUE_LINK_TYPE, map()
                        .add(IssueLinkType.NAME_FIELD_NAME, linkDefiniton.name)
                        .toMap());

                //
                // we do this pre check just in case it got partially though this previous upgrade tasks and added data but didn't complete it all.
                //
                if (gv.isEmpty())
                {
                    ofBizDelegator.createValue(OfBizDelegator.ISSUE_LINK_TYPE, map()
                            .add(IssueLinkType.NAME_FIELD_NAME, linkDefiniton.name)
                            .add(IssueLinkType.OUTWARD_FIELD_NAME, linkDefiniton.outward)
                            .add(IssueLinkType.INWARD_FIELD_NAME, linkDefiniton.inward)
                            .add(IssueLinkType.STYLE_FIELD_NAME, linkDefiniton.style)
                            .toMap()
                    );
                }
            }
            applicationProperties.setOption(APKeys.JIRA_OPTION_ISSUELINKING, true);
        }
    }

    private String getCloneLinkTypeName()
    {
        String name = applicationProperties.getDefaultBackedString(APKeys.JIRA_CLONE_LINKTYPE_NAME);
        return name == null ? "Cloners" : name;
    }

    private MapBuilder<String, Object> map()
    {
        return MapBuilder.newBuilder();
    }
}
