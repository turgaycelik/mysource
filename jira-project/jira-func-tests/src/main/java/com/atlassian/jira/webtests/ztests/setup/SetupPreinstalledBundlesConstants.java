package com.atlassian.jira.webtests.ztests.setup;

import java.util.EnumSet;

import com.atlassian.jira.functest.framework.setup.JiraSetupInstanceHelper;

import static com.atlassian.jira.functest.framework.setup.JiraSetupInstanceHelper.SetupStage;

public class SetupPreinstalledBundlesConstants
{
    public static final String PLUGINID_SERVICEDESK = "com.atlassian.servicedesk";
    public static final String PLUGINID_AGILE = "com.pyxis.greenhopper.jira";
    public static final String BUNDLE_AGILE = "DEVELOPMENT";
    public static final String BUNDLE_SERVICEDESK = "SERVICEDESK";
    public static final String BUNDLE_NONE = "TRACKING";
    public static final String SELECTED_BUNDLE_FORM_ELEMENT = "selectedBundle";

    public static final EnumSet<SetupStage> STEPS_UNTIL_BUNDLE = EnumSet.range(SetupStage.START, SetupStage.BUNDLE.prev());
    public static final EnumSet<SetupStage> STEPS_AFTER_BUNDLE = EnumSet.range(SetupStage.BUNDLE.next(), SetupStage.END);
}
