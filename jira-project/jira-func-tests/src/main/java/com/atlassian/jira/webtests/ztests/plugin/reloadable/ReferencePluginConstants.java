package com.atlassian.jira.webtests.ztests.plugin.reloadable;

import static com.atlassian.jira.util.lang.JiraStringUtils.asString;

/**
 * Constants relating to modules of the reference development plugin
 *
 * @since v4.3
 */
public final class ReferencePluginConstants
{
    private ReferencePluginConstants()
    {
        throw new AssertionError("Don't instantiate me");
    }

    private static String completeKey(String moduleKey)
    {
        return REFERENCE_PLUGIN_KEY + ":" + moduleKey;
    }

    private static String completeDependentKey(String moduleKey)
    {
        return REFERENCE_DEPENDENT_PLUGIN_KEY + ":" + moduleKey;
    }

    public static final String REFERENCE_PLUGIN_KEY = asString("com.atlassian.jira.dev.reference-plugin");
    public static final String REFERENCE_DEPENDENT_PLUGIN_KEY = asString("com.atlassian.jira.dev.reference-dependent-plugin");
    public static final String REFERENCE_LANGUAGE_PACK_KEY = asString("com.atlassian.jira.jira-reference-language-pack");

    public static final String SELECT_CUSTOM_FIELD_TYPE_KEY = completeKey("reference-select");
    public static final String SELECT_CUSTOM_FIELD_TYPE_NAME = asString("Reference Custom Field Type");

    public static final String REFERENCE_ACTIONS_KEY = completeKey("reference-actions");
    public static final String REFERENCE_ACTIONS_NAME = asString("Reference WebWork Action");

    public static final String REFERENCE_SERVLET_KEY = completeKey("reference-servlet");
    public static final String REFERENCE_SERVLET_NAME = asString("Reference Servlet");

    public static final String REFERENCE_ECHO_NAME = asString("Reference Echo Function");
    public static final String REFERENCE_ECHO_KEY = completeKey("reference-echo-jql-function");

    public static final String REFERENCE_REPORT_KEY = completeKey("reference-report");
    public static final String REFERENCE_REPORT_NAME = completeKey("Reference Report");

    public static final String REFERENCE_WORKFLOW_VALIDATOR_ERROR_MESSAGE = asString("Somebody configured me to fail:)");

    public static final String REFERENCE_PORTLET_KEY = completeKey("reference-portlet");
    public static final String REFERENCE_PORTLET_NAME = asString("Reference Portlet");

    public static final String REFERENCE_RESOURCE_KEY = asString("reference.resource");
    public static final String REFERENCE_DEPENDENT_RESOURCE_KEY = asString("reference.dependent.resource");

      // Custom type and Custom type searcher module keys
    public static final String CFTYPE_SELECT_MODULE_KEY =  "reference-select";
    public static final String CFTYPE_LABELS_MODULE_KEY =  "reference-labels";
    public static final String CFTYPE_USERPICKER_MODULE_KEY =  "reference-userpicker";
    public static final String CFTYPE_DATETIME_MODULE_KEY =  "reference-datetime";
    public static final String CFTYPE_TEXTAREA_MODULE_KEY =  "reference-textarea";
    public static final String CFTYPE_MULTIGROUPPICKER_KEY =  "reference-multigrouppicker";
    public static final String CFTYPE_CASCADINGSELECT_MODULE_KEY =  "reference-cascadingselect";
    public static final String CFTYPE_USERPICKERSEARCHER_MODULE_KEY =  "reference-userpickersearcher";
    public static final String CFTYPE_DATEIMESEARCHER_MODULE_KEY =  "reference-datetimesearcher";
    public static final String CFTYPE_TEXTAREASEARCHER_MODULE_KEY =  "reference-textsearcher";
    public static final String CFTYPE_LABELSSEARCHER_MODULE_KEY = "reference-labelsearcher";
    public static final String CFTYPE_STDLABELSSEARCHER_MODULE_KEY = "labelsearcher";
}
