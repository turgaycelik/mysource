package com.atlassian.jira.functest.framework;

/**
 * A series of constants that func tests can use
 *
 * I know I know it could be done via enums but I am trying to migrate old code in at least 2 places
 * with the least effort so suck it up!
 *
 */
public interface FunctTestConstants
{
    public static final String ISSUE_ALL = "0";
    public static final String ISSUE_BUG = "1";
    public static final String ISSUE_NEWFEATURE = "2";
    public static final String ISSUE_TASK = "3";
    public static final String ISSUE_IMPROVEMENT = "4";

    public static final String ISSUE_TYPE_ALL = "All Unassigned Issue Types";
    public static final String ISSUE_TYPE_BUG = "Bug";
    public static final String ISSUE_TYPE_NEWFEATURE = "New Feature";
    public static final String ISSUE_TYPE_TASK = "Task";
    public static final String ISSUE_TYPE_IMPROVEMENT = "Improvement";
    public static final String ISSUE_TYPE_SUB_TASK = "Sub-task";
    public static final String ISSUE_TYPE_ANY = "All Standard Issue Types";
    public static final String ISSUE_TYPE_ALL_SUB_TASK = "All Sub-Task Issue Types";

    public static final String ISSUE_IMAGE_BUG = "/images/icons/issuetypes/bug.png";
    public static final String ISSUE_IMAGE_NEWFEATURE = "/images/icons/issuetypes/newfeature.png";
    public static final String ISSUE_IMAGE_TASK = "/images/icons/issuetypes/task.png";
    public static final String ISSUE_IMAGE_IMPROVEMENT = "/images/icons/issuetypes/improvement.png";
    public static final String ISSUE_IMAGE_SUB_TASK = "/images/icons/issuetypes/subtask_alternate.png";
    public static final String ISSUE_IMAGE_GENERIC = "/images/icons/issuetypes/genericissue.png";

    public static final String PRIORITY_BLOCKER = "Blocker";
    public static final String PRIORITY_CRITICAL = "Critical";
    public static final String PRIORITY_MAJOR = "Major";
    public static final String PRIORITY_MINOR = "Minor";
    public static final String PRIORITY_TRIVIAL = "Trivial";

    public static final String PRIORITY_IMAGE_BLOCKER = "/images/icons/priorities/blocker.png";
    public static final String PRIORITY_IMAGE_CRITICAL = "/images/icons/priorities/critical.png";
    public static final String PRIORITY_IMAGE_MAJOR = "/images/icons/priorities/major.png";
    public static final String PRIORITY_IMAGE_MINOR = "/images/icons/priorities/minor.png";
    public static final String PRIORITY_IMAGE_TRIVIAL = "/images/icons/priorities/trivial.png";

    public static final String FS = System.getProperty("file.separator");
    public static final String HTM = ".htm";

    public static final String PROJECT_HOMOSAP = "homosapien";
    public static final String PROJECT_NEO = "neanderthal";
    public static final String PROJECT_MONKEY = "monkey";

    public static final String PROJECT_HOMOSAP_KEY = "HSP";
    public static final String PROJECT_NEO_KEY = "NDT";
    public static final String PROJECT_MONKEY_KEY = "MKY";

    public static final String ISSUE_TAB_ALL = "All";
    public static final String ISSUE_TAB_COMMENTS = "Comments";
    public static final String ISSUE_TAB_WORK_LOG = "Work Log";
    public static final String ISSUE_TAB_CHANGE_HISTORY = "History";

    public static final String PROJECT_TAB_OPEN_ISSUES = "Open Issues";
    public static final String PROJECT_TAB_ROAD_MAP = "Road Map";
    public static final String PROJECT_TAB_CHANGE_LOG = "Change Log";
    public static final String PROJECT_TAB_VERSIONS = "Versions";
    public static final String PROJECT_TAB_COMPONENTS = "Components";

    public static final String STEP_PREFIX = "Bulk Operation: ";
    public static final String STEP_CHOOSE_ISSUES = "Choose Issues";
    public static final String STEP_CHOOSE_OPERATION = "Choose Operation";
    public static final String STEP_OPERATION_DETAILS = "Operation Details";
    public static final String STEP_CONFIRMATION = "Confirmation";

    public static final String LINK_BULK_CHANGE_ALL = "bulkedit_all";
    public static final String LINK_BULK_CHANGE_CURR_PG = "bulkedit_curr_pg";
    public static final String LINK_NEXT_PG = "Next >>";
    public static final String LINK_EDIT_ISSUE = "edit-issue";
    public static final String LINK_ASSIGN_ISSUE = "assign-issue";
    public static final String LINK_DELETE_ISSUE = "delete-issue";
    public static final String LINK_CLONE_ISSUE = "clone-issue";

    public static final String FIELD_OPERATION = "operation";
    public static final String FIELD_WORKFLOW = "wftransition";
    // Radio buttons for 'choose operation' step.
    public static final String RADIO_OPERATION_DELETE = "bulk.delete.operation.name";
    public static final String RADIO_OPERATION_EDIT = "bulk.edit.operation.name";
    public static final String RADIO_OPERATION_MOVE = "bulk.move.operation.name";
    public static final String RADIO_OPERATION_WORKFLOW = "bulk.workflowtransition.operation.name";

    public static final String FIELD_FIX_VERSIONS = "fixVersions";
    public static final String FIELD_VERSIONS = "versions";
    public static final String FIELD_COMPONENTS = "components";
    public static final String FIELD_ASSIGNEE = "assignee";
    public static final String FIELD_PRIORITY = "priority";
    public static final String FIELD_COMMENT = "comment";

    public static final String BUTTON_CANCEL = "Cancel";
    public static final String BUTTON_NEXT = "Next";
    public static final String BUTTON_CONFIRM = "Confirm";

    public static final String BUTTON_NAME_NEXT = "nextBtn";

    public static final String LABEL_ISSUE_NAVIGATOR = "Issue Navigator";

    public static final String ADMIN_USERNAME = "admin";
    public static final String ADMIN_PASSWORD = "admin";
    public static final String ADMIN_FULLNAME = "Administrator";
    public static final String ADMIN_EMAIL = "admin@stuff.com.com";

    public static final String SYS_ADMIN_USERNAME = "root";
    public static final String SYS_ADMIN_PASSWORD = "root";

    public static final String BOB_USERNAME = "bob";
    public static final String BOB_PASSWORD = "bob";
    public static final String BOB_FULLNAME = "Bob The Builder";
    public static final String BOB_EMAIL = "bob@stuff.com.com";

    public static final String FRED_USERNAME = "fred";
    public static final String FRED_PASSWORD = "fred";
    public static final String FRED_FULLNAME = "Fred Normal";
    public static final String FRED_EMAIL = "fred@example.com";

    public static final String ANYONE = "Anyone";

    public static final String CURRENT_USER = "issue_current_user";

    public static final String JIRA_USERS_GROUP = "jira-users";
    public static final String JIRA_DEV_GROUP = "jira-developers";
    public static final String JIRA_ADMIN_GROUP = "jira-administrators";

    public static final String JIRA_USERS_ROLE = "Users";
    public static final long JIRA_USERS_ROLE_ID = 10000;
    public static final String JIRA_DEV_ROLE = "Developers";
    public static final long JIRA_DEV_ROLE_ID = 10001;
    public static final String JIRA_ADMIN_ROLE = "Administrators";
    public static final long JIRA_ADMIN_ROLE_ID = 10002;

    public static final String COMPONENT_NAME_ONE = "New Component 1";
    public static final String COMPONENT_NAME_TWO = "New Component 2";
    public static final String COMPONENT_NAME_THREE = "New Component 3";
    public static final String COMPONENT_NAME_FOUR = "New Component 4";

    public static final String VERSION_NAME_ONE = "New Version 1";
    public static final String VERSION_NAME_TWO = "New Version 2";
    public static final String VERSION_NAME_THREE = "New Version 3";
    public static final String VERSION_NAME_FOUR = "New Version 4";
    public static final String VERSION_NAME_FIVE = "New Version 5";

    public static final String PERM_SCHEME_NAME = "New Permission Scheme";
    public static final String PERM_SCHEME_DESC = "permission scheme for testing";
    public static final String DEFAULT_PERM_SCHEME = "Default Permission Scheme";
    public static final int DEFAULT_PERM_SCHEME_ID = 0;


    public static final String FIELD_SCHEME_NAME = "New Field Layout Scheme";
    public static final String FIELD_SCHEME_DESC = "field layout scheme for testing";

    public static final String SECURITY_SCHEME_NAME = "New Security Scheme";
    public static final String SECURITY_SCHEME_DESC = "security scheme for testing";

    public static final String SECURITY_LEVEL_ONE_NAME = "Red";
    public static final String SECURITY_LEVEL_TWO_NAME = "Orange";
    public static final String SECURITY_LEVEL_THREE_NAME = "Green";

    public static final String SECURITY_LEVEL_ONE_DESC = "Highest Level";
    public static final String SECURITY_LEVEL_TWO_DESC = "Middle Level";
    public static final String SECURITY_LEVEL_THREE_DESC = "Lowest Level";

    public final String SUB_TASK_DEFAULT_TYPE = "Sub-task";
    public final String SUB_TASK_SUMMARY = "Sub Task of Test 5";

    public final String CUSTOM_SUB_TASK_SUMMARY = "Custom Sub Task of Test 5";
    public final String CUSTOM_SUB_TASK_TYPE_NAME = "Custom Sub Task";
    public final String CUSTOM_SUB_TASK_TYPE_DESCRIPTION = "Custom Sub Task";

    public final String Summary = "Summary of a sub task";
    public final String minorPriority = "Minor";

    public final String WORKFLOW_SCHEME = "New Workflow Scheme";
    public final String WORKFLOW_ADDED = "New Workflow For Testing";
    public final String WORKFLOW_COPIED = "Copied Workflow";
    public final String STEP_NAME = "Approved";
    public final String STATUS_NAME = "Approved";
    public final String TRANSIION_NAME_APPROVE = "Approve Issue";
    public final String TRANSIION_NAME_REOPEN = "Reopen Issue";
    public final String TRANSIION_NAME_CLOSE = "Close Issue";
    public final String TRANSIION_NAME_RESOLVE = "Resolve Issue";
    public final String TRANSIION_NAME_START_PROGRESS = "Start Progress";
    public final String TRANSIION_NAME_STOP_PROGRESS = "Stop Progress";

    public String ASSIGN_FIELD_SCREEN_NAME = "Workflow Screen";
    public String DEFAULT_FIELD_SCREEN_NAME = "Default Screen";
    public String RESOLVE_FIELD_SCREEN_NAME = "Resolve Issue Screen";

    public static final String ASSIGN_FIELD_SCREEN = "Workflow Screen";
    public static final String TEST_FIELD_SCREEN = "Test Workflow Screen";

    public String DEFAULT_OPERATION_SCREEN = "Default";
    public String VIEW_ISSUE_OPERATION_SCREEN = "View Issue";
    public String EDIT_ISSUE_OPERATION_SCREEN = "Edit Issue";
    public String CREATE_ISSUE_OPERATION_SCREEN = "Create Issue";

    public String DEFAULT_SCREEN_SCHEME = "Default Screen Scheme";

    public String DEFAULT_ISSUE_TYPE_SCREEN_SCHEME = "Default Issue Type Screen Scheme";

    // Permission Numbers
    public static final int GLOBAL_ADMIN = 0;
    public static final int USE = 1;
    public static final int BROWSE = 10;
    public static final int CREATE_ISSUE = 11;
    public static final int EDIT_ISSUE = 12;
    public static final int ASSIGN_ISSUE = 13;
    public static final int RESOLVE_ISSUE = 14;
    public static final int COMMENT_ISSUE = 15;
    public static final int COMMENT_EDIT_ALL = 34;
    public static final int COMMENT_EDIT_OWN = 35;
    public static final int COMMENT_DELETE_ALL = 36;
    public static final int COMMENT_DELETE_OWN = 37;
    public static final int DELETE_ISSUE = 16;
    public static final int ASSIGNABLE_USER = 17;
    public static final int CLOSE_ISSUE = 18;
    public static final int CREATE_ATTACHMENT = 19;
    public static final int WORK_ISSUE = 20;
    public static final int LINK_ISSUE = 21;
    public static final int CREATE_SHARED_OBJECTS = 22;
    public static final int PROJECT_ADMIN = 23;
    public static final int MANAGE_GROUP_FILTER_SUBSCRIPTIONS = 24;
    public static final int MOVE_ISSUE = 25;
    public static final int SET_ISSUE_SECURITY = 26;
    public static final int USER_PICKER = 27;
    public static final int SCHEDULE_ISSUE = 28;
    public static final int VIEW_VERSION_CONTROL = 29;
    public static final int MODIFY_REPORTER = 30;
    public static final int VIEW_VOTERS_AND_WATCHERS = 31;
    public static final int MANAGE_WATCHER_LIST = 32;
    public static final int BULK_CHANGE = 33;
    public static final int ADMINISTER = 0;
    public static final int SYSTEM_ADMINISTER = 44;


    public static final String FIELD_TABLE_ID = "field_table";
    public static final String JIRA_FORM_NAME = "jiraform";
    public static final int SCREEN_TABLE_NAME_COLUMN_INDEX = 1;

    public static final String AFFECTS_VERSIONS_FIELD_ID = "Affects Version/s";
    public static final String FIX_VERSIONS_FIELD_ID = "Fix Version/s";
    public static final String VERSIONS_FIELD_ID = "Version/s";
    public static final String COMPONENTS_FIELD_ID = "Component/s";
    public static final String REPORTER_FIELD_ID = "Reporter";
    public static final String SECURITY_LEVEL_FIELD_ID = "Security Level";
    public static final String DUE_DATE_FIELD_ID = "Due Date";
    public static final String PRIORITY_FIELD_ID = "Priority";
    public static final String RESOLUTION_FIELD_ID = "Resolution";
    public static final String ASSIGNEE_FIELD_ID = "Assignee";
    public static final String ATTACHMENT_FIELD_ID = "Attachment";

    public static final String BUILT_IN_CUSTOM_FIELD_KEY = "com.atlassian.jira.plugin.system.customfieldtypes";
    public static final String CUSTOM_FIELD_PREFIX = "customfield_";
    public static final String CUSTOM_FIELD_TYPE_SELECT = "select";
    public static final String CUSTOM_FIELD_TYPE_RADIO = "radiobuttons";
    public static final String CUSTOM_FIELD_TYPE_CHECKBOX = "multicheckboxes";
    public static final String CUSTOM_FIELD_TYPE_MULTICHECKBOXES = "multicheckboxes";
    public static final String CUSTOM_FIELD_TYPE_TEXTFIELD = "textfield";
    public static final String CUSTOM_FIELD_TYPE_MULTISELECT = "multiselect";
    public static final String CUSTOM_FIELD_TYPE_USERPICKER = "userpicker";
    public static final String CUSTOM_FIELD_TYPE_MULTIUSERPICKER = "multiuserpicker";
    public static final String CUSTOM_FIELD_TYPE_DATEPICKER = "datepicker";
    public static final String CUSTOM_FIELD_TYPE_DATETIME = "datetime";
    public static final String CUSTOM_FIELD_TYPE_GROUPPICKER = "grouppicker";
    public static final String CUSTOM_FIELD_TYPE_MULTIGROUPPICKER = "multigrouppicker";
    public static final String CUSTOM_FIELD_TYPE_URL = "url";
    public static final String CUSTOM_FIELD_TYPE_FLOAT = "float";
    public static final String CUSTOM_FIELD_TYPE_CASCADINGSELECT = "cascadingselect";
    public static final String CUSTOM_FIELD_TYPE_PROJECT = "project";
    public static final String CUSTOM_FIELD_TYPE_LABELS = "labels";
    public static final String CUSTOM_FIELD_TYPE_VERSION = "version";

    public static final String CUSTOM_FIELD_TEXT_SEARCHER = "textsearcher";
    public static final String CUSTOM_FIELD_EXACT_TEXT_SEARCHER = "exacttextsearcher";
    public static final String CUSTOM_FIELD_DATE_RANGE = "daterange";
    public static final String CUSTOM_FIELD_EXACT_NUMBER = "exactnumber";
    public static final String CUSTOM_FIELD_NUMBER_RANGE = "numberrange";
    public static final String CUSTOM_FIELD_PROJECT_SEARCHER = "projectsearcher";
    public static final String CUSTOM_FIELD_USER_PICKER_SEARCHER = "userpickersearcher";
    public static final String CUSTOM_FIELD_GROUP_PICKER_SEARCHER = "grouppickersearcher";
    public static final String CUSTOM_FIELD_SELECT_SEARCHER = "selectsearcher";
    public static final String CUSTOM_FIELD_RADIO_SEARCHER = "radiosearcher";
    public static final String CUSTOM_FIELD_CASCADING_SELECT_SEARCHER = "cascadingselectsearcher";
    public static final String CUSTOM_FIELD_MULTI_SELECT_SEARCHER = "multiselectsearcher";
    public static final String CUSTOM_FIELD_CHECKBOX_SEARCHER = "checkboxsearcher";
    public static final String CUSTOM_FIELD_LABEL_SEARCHER = "labelsearcher";
    public static final String CUSTOM_FIELD_VERSION_SEARCHER = "versionsearcher";
    public static final String CUSTOM_FIELD_USER_PICKER_GROUP_SEARCHER = "userpickergroupsearcher";

    public static final String MOVE_TO_LAST = "moveToLast_";
    public static final String MOVE_TO_FIRST = "moveToFirst_";
    public static final String MOVE_DOWN = "moveDown_";
    public static final String MOVE_UP = "moveUp_";

    public static final String CHANGE_HISTORY = "History";


    public static final String STATUS_OPEN = "Open";
    public static final String STATUS_IN_PROGRESS = "In Progress";
    public static final String STATUS_RESOLVED = "Resolved";

    public static final String DEFAULT_FIELD_CONFIGURATION = "Default Field Configuration";
    public static final String CUSTOM_FIELD_CONFIGURATION = "Renderer Custom Field Configuration";
    public static final String WIKI_STYLE_RENDERER = "Wiki Style Renderer";
    public static final String DEFAULT_TEXT_RENDERER = "Default Text Renderer";

    public final String EVENT_TYPE_ACTIVE_STATUS = "Active";
    public final String EVENT_TYPE_INACTIVE_STATUS = "Inactive";
    public static final String DEFAULT_ASSIGNEE_ERROR_MESSAGE = "The default assignee does NOT have ASSIGNABLE permission OR Unassigned issues are turned off.";
    public static final String CLONERS_LINK_TYPE_NAME = "Cloners";
    public static final String CLONERS_OUTWARD_LINK_NAME = "clones";
    public static final String CLONERS_INWARD_LINK_NAME = "is cloned by";
    public static final String ISSUETABLE_ID = "issuetable";
    public static final int ISSUETABLE_HEADER_ROW = 0;
    public static final int ISSUETABLE_EDIT_ROW = 1;

    public static final String UNKNOWN = "Unknown";
    public static final String UNKNOWN_ID = "-1";

    public static final String FORMAT_PRETTY = "pretty";
    public static final String FORMAT_HOURS = "hours";
    public static final String FORMAT_DAYS = "days";

    /**
     * The REST path under which the func-test-plugin's *Backdoor classes are available.
     */
    public static final String FUNC_TEST_PLUGIN_REST_PATH = "func-test";
}
