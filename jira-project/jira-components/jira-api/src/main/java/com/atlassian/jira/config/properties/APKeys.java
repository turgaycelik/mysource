/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.config.properties;

import com.atlassian.annotations.PublicApi;

@PublicApi
public interface APKeys
{
    public static final String JIRA_TITLE = "jira.title";
    public static final String JIRA_BASEURL = "jira.baseurl";
    public static final String JIRA_SETUP = "jira.setup";
    public static final String JIRA_SETUP_CHOSEN_BUNDLE = "jira.setup.chosen.bundle";
    public static final String JIRA_SETUP_WEB_SUDO_TOKEN = "jira.setup.web.sudo.token";
    public static final String JIRA_MODE = "jira.mode";
    public static final String JIRA_EDITION = "jira.edition";
    public static final String JIRA_PATH_ATTACHMENTS = "jira.path.attachments";
    public static final String JIRA_ATTACHMENT_PATH_ALLOWED = "jira.attachment.set.allowed";
    public static final String JIRA_PATH_ATTACHMENTS_USE_DEFAULT_DIRECTORY = "jira.path.attachments.use.default.directory";
    public static final String JIRA_ATTACHMENT_SIZE = "webwork.multipart.maxSize";
    /** Backup path. @deprecated  Since 4.2.2 */
    public static final String JIRA_PATH_BACKUP = "jira.path.backup";
    public static final String JIRA_INTRODUCTION = "jira.introduction";
    public static final String JIRA_ALERT_HEADER = "jira.alertheader";
    public static final String JIRA_ALERT_HEADER_VISIBILITY = "jira.alertheader.visibility";
    public static final String JIRA_OPTION_USER_ASSIGNNEW = "jira.option.user.assignnew";
    public static final String JIRA_OPTION_USER_EXTERNALMGT = "jira.option.user.externalmanagement";
    public static final String JIRA_OPTION_USER_CROWD_ALLOW_RENAME = "jira.option.user.crowd.allow.rename";
    public static final String JIRA_OPTION_ALLOWUNASSIGNED = "jira.option.allowunassigned";
    public static final String JIRA_OPTION_VOTING = "jira.option.voting";
    public static final String JIRA_OPTION_WATCHING = "jira.option.watching";
    public static final String JIRA_OPTION_ALLOWATTACHMENTS = "jira.option.allowattachments";
    public static final String JIRA_OPTION_ALLOWSUBTASKS = "jira.option.allowsubtasks";
    /** @deprecated since 6.3.3 JIRA needs indexing and it cannot be disabled. See {@link com.atlassian.jira.util.index.IndexLifecycleManager#isIndexAvailable()}  */
    public static final String JIRA_OPTION_INDEXING = "jira.option.indexing";
    public static final String JIRA_OPTION_ISSUELINKING = "jira.option.issuelinking";
    /** @deprecated since 6.2 */
    @Deprecated
    public static final String JIRA_OPTION_AUDITING = "jira.option.auditing";
    public static final String JIRA_OPTION_AUDITING_LOG_RETENTION_PERIOD_IN_MONTHS = "jira.option.auditing.log.retention.period.in.months";
    public static final String JIRA_OPTION_AUDITING_LOG_RETENTION_PERIOD_LAST_CHANGE_TIMESTAMP = "jira.option.auditing.log.retention.period.last.change.timestamp";

    public static final String JIRA_OPTION_LOGOUT_CONFIRM = "jira.option.logoutconfirm";
    public static final String JIRA_OPTION_EMAIL_VISIBLE = "jira.option.emailvisible";
    public static final String JIRA_OPTION_RPC_ALLOW = "jira.option.rpc.allow";
    public static final String JIRA_OPTION_EXCLUDE_PRECEDENCE_EMAIL_HEADER = "jira.option.precedence.header.exclude";
    public static final String JIRA_OPTION_ALLOW_COOKIES = "jira.option.allowcookies";
    public static final String JIRA_OPTION_IGNORE_URL_WITH_KEY = "jira.option.ignore.url.with.key";
    public static final String JIRA_OPTION_KEY_DETECTION_BACKWARDS_COMPATIBLE = "jira.option.key.detection.backwards.compatible";
    public static final String JIRA_OPTION_CAPTCHA_ON_SIGNUP = "jira.option.captcha.on.signup";
    public static final String JIRA_OPTION_OPERATIONS_DISABLE = "jira.disable.operations.bar";
    public static final String JIRA_OPTION_ENABLED_DARK_FEATURES = "jira.enabled.dark.features";

    /**
     * @deprecated since JIRA 6.3. Use {@link com.atlassian.jira.avatar.GravatarSettings} instead.
     */
    @Deprecated
    public static final String JIRA_OPTION_USER_AVATAR_FROM_GRAVATAR = "jira.user.avatar.gravatar.enabled";

    public static final String JIRA_OPTION_DISABLE_INLINE_EDIT = "jira.issue.inline.edit.disabled";

    public static final String JIRA_OPTION_WEB_USEGZIP = "jira.option.web.usegzip";
    public static final String JIRA_PATCHED_VERSION = "jira.version.patched";
    public static final String JIRA_VERSION = "jira.version";
    public static final String JIRA_DOWNGRADE_VERSION = "jira.downgrade.minimum.version";
    public static final String JIRA_PATH_INDEX_USE_DEFAULT_DIRECTORY = "jira.path.index.use.default.directory";
    public static final String JIRA_PATH_INDEX = "jira.path.index";

    //Note that this is a webwork property that will be used by the UI tag.
    //see ApplicationPropertiesConfiguration &  JiraConfiguration
    public static final String JIRA_WEBWORK_ENCODING = "webwork.i18n.encoding";

    // Mail encoding
    public static final String JIRA_MAIL_ENCODING = "jira.i18n.email.encoding";

    // Message handling
    public static final String JIRA_OPTION_IGNORE_EMAIL_MESSAGE_ATTACHMENTS = "jira.option.ignore.email.message.attachments";

    //Property that holds the maximum number of issues that should be sent in an email.
    public static final String JIRA_MAIL_MAX_ISSUES = "jira.subscription.email.max.issues";

    // internationalization (i18n)
    public static final String JIRA_I18N_LANGUAGE_INPUT = "jira.i18n.language.index";
    public static final String JIRA_I18N_DEFAULT_LOCALE = "jira.i18n.default.locale";
    public static final String JIRA_I18N_SQL_LOCALE = "jira.i18n.sql.locale";
    public static final String JIRA_I18N_SQL_COLLATOR_STRENGTH = "jira.i18n.sql.collator.strength";
    public static final String JIRA_DEFAULT_TIMEZONE = "jira.default.timezone";

    /**
     * Specifies whether JIRA should embed i18n key meta-data when rendering pages.
     */
    public static final String JIRA_I18N_INCLUDE_META_DATA = "jira.i18n.include.meta-data";

    // XSRF keys
    public static final String JIRA_XSRF_ENABLED = "jira.xsrf.enabled";

    // Database Transaction Support Is Disabled
    public static final String JIRA_DB_TXN_DISABLED = "jira.db.txns.disabled";

    //Look and Feel properties.
    // Note that you should not access these directly, but instead get an instance of
    // com.atlassian.jira.config.properties.LookAndFeelBean
    public static final String JIRA_LF_LOGO_URL = "jira.lf.logo.url";
    public static final String JIRA_LF_LOGO_WIDTH = "jira.lf.logo.width";
    public static final String JIRA_LF_LOGO_HEIGHT = "jira.lf.logo.height";

    public static final String JIRA_LF_FAVICON_URL = "jira.lf.favicon.url";
    public static final String JIRA_LF_FAVICON_HIRES_URL = "jira.lf.favicon.hires.url";

    public static final String JIRA_LF_TOP_BGCOLOUR = "jira.lf.top.bgcolour";
    public static final String JIRA_LF_TOP_TEXTCOLOUR = "jira.lf.top.textcolour";
    public static final String JIRA_LF_TOP_HIGHLIGHTCOLOR = "jira.lf.top.hilightcolour";
    public static final String JIRA_LF_TOP_TEXTHIGHLIGHTCOLOR = "jira.lf.top.texthilightcolour";
    public static final String JIRA_LF_TOP_SEPARATOR_BGCOLOR = "jira.lf.top.separator.bgcolor";


    public static final String JIRA_LF_MENU_BGCOLOUR = "jira.lf.menu.bgcolour";
    public static final String JIRA_LF_MENU_TEXTCOLOUR = "jira.lf.menu.textcolour";
    public static final String JIRA_LF_MENU_SEPARATOR = "jira.lf.menu.separator";

    public static final String JIRA_LF_HERO_BUTTON_TEXTCOLOUR = "jira.lf.hero.button.text.colour";
    public static final String JIRA_LF_HERO_BUTTON_BASEBGCOLOUR = "jira.lf.hero.button.base.bg.colour";

    public static final String JIRA_LF_TEXT_LINKCOLOUR = "jira.lf.text.linkcolour";
    public static final String JIRA_LF_TEXT_ACTIVE_LINKCOLOUR = "jira.lf.text.activelinkcolour";

    public static final String JIRA_LF_GADGET_COLOR_PREFIX = "jira.lf.gadget.";

    public static final String JIRA_LF_APPLICATION_ID = "jira.lf.application.id";


    public static final String JIRA_LF_TEXT_HEADINGCOLOUR = "jira.lf.text.headingcolour";

    public static final String JIRA_LF_FIELD_LABEL_WIDTH = "jira.lf.field.label.width";

    //Date time format fields
    public static final String JIRA_LF_DATE_TIME = "jira.lf.date.time";
    public static final String JIRA_LF_DATE_DAY = "jira.lf.date.day";
    public static final String JIRA_LF_DATE_COMPLETE = "jira.lf.date.complete";
    public static final String JIRA_LF_DATE_DMY = "jira.lf.date.dmy";
    public static final String JIRA_LF_DATE_RELATIVE = "jira.lf.date.relativize";



    //Descriptions for create issue fields
    public static final String JIRA_ISSUE_DESC_ISSUETYPE = "jira.issue.desc.issuetype";
    public static final String JIRA_ISSUE_DESC_SUMMARY = "jira.issue.desc.summary";
    public static final String JIRA_ISSUE_DESC_PRIORITY = "jira.issue.desc.priority";
    public static final String JIRA_ISSUE_DESC_COMPONENTS = "jira.issue.desc.components";
    public static final String JIRA_ISSUE_DESC_VERSIONS = "jira.issue.desc.versions";
    public static final String JIRA_ISSUE_DESC_FIXFOR = "jira.issue.desc.fixfor";
    public static final String JIRA_ISSUE_DESC_ASSIGNEE = "jira.issue.desc.assignee";
    public static final String JIRA_ISSUE_DESC_ENVIRONMENT = "jira.issue.desc.environment";
    public static final String JIRA_ISSUE_DESC_DESCRIPTION = "jira.issue.desc.description";
    public static final String JIRA_ISSUE_DESC_ORIGINAL_TIMETRACK = "jira.issue.desc.original.timetrack";
    public static final String JIRA_ISSUE_DESC_TIMETRACK = "jira.issue.desc.timetrack";
    public static final String JIRA_ISSUE_CACHE_CAPACITY = "jira.issue.cache.capacity";
    public static final String JIRA_ISSUE_EXPIRE_TIME = "jira.issue.expire.time";
    public static final String JIRA_ISSUE_FIELDS_CONFIG = "jira.issue.fields.config";

    //Default values for constants in the system
    public static final String JIRA_CONSTANT_DEFAULT_ISSUE_TYPE = "jira.constant.default.issue.type";
    public static final String JIRA_CONSTANT_DEFAULT_PRIORITY = "jira.constant.default.priority";
    public static final String JIRA_CONSTANT_DEFAULT_RESOLUTION = "jira.constant.default.resolution";
    public static final String JIRA_CONSTANT_DEFAULT_STATUS = "jira.constant.default.status";

    //Confirmed install of new version under Evaluation Terms with old license
    public static final String JIRA_LICENSE = "License20"; // since 4.0, for storing the license
    public static final String JIRA_LICENSE_V1_MESSAGE = "License Message Text"; // pre 4.0 for storing license message as text
    public static final String JIRA_LICENSE_V1_HASH = "License Hash 1 Text"; // pre 4.0 for storing license hash as text
    public static final String JIRA_OLD_LICENSE_V1_MESSAGE = "License Message"; // old for storing license message as string
    public static final String JIRA_OLD_LICENSE_V1_HASH = "License Hash 1"; // old for storing license hash as string

    public static final String JIRA_CONFIRMED_INSTALL_WITH_OLD_LICENSE = "jira.install.oldlicense.confirmed";
    public static final String JIRA_CONFIRMED_INSTALL_WITH_OLD_LICENSE_USER = "jira.install.oldlicense.confirmed.user";
    public static final String JIRA_CONFIRMED_INSTALL_WITH_OLD_LICENSE_TIMESTAMP = "jira.install.oldlicense.confirmed.timestamp";
    public static final String JIRA_DATE_PICKER_JAVA_FORMAT = "jira.date.picker.java.format";
    public static final String JIRA_DATE_PICKER_JAVASCRIPT_FORMAT = "jira.date.picker.javascript.format";
    public static final String JIRA_ISSUENAV_CRITERIA_AUTOUPDATE = "jira.issuenav.criteria.autoupdate";
    public static final String JIRA_DATE_TIME_PICKER_JAVA_FORMAT = "jira.date.time.picker.java.format";
    public static final String JIRA_DATE_TIME_PICKER_JAVASCRIPT_FORMAT = "jira.date.time.picker.javascript.format";

    // The value jira.date.time.picker.use.iso8061 is incorrect (should be iso8601) but needs an upgrade task to change
    public static final String JIRA_DATE_TIME_PICKER_USE_ISO8601 = "jira.date.time.picker.use.iso8061";
    @Deprecated
    /*
     * @Deprecated Use APKeys#JIRA_DATE_TIME_PICKER_USE_ISO8601
     */
    public static final String JIRA_DATE_TIME_PICKER_USE_ISO8061 = JIRA_DATE_TIME_PICKER_USE_ISO8601;

    public static final String JIRA_THUMBNAIL_MAX_WIDTH = "jira.thumbnail.maxwidth";
    public static final String JIRA_THUMBNAIL_MAX_HEIGHT = "jira.thumbnail.maxheight";
    public static final String JIRA_OPTION_ALLOWTHUMBNAILS = "jira.option.allowthumbnails";

    public static final String JIRA_SCREENSHOTAPPLET_ENABLED = "jira.screenshotapplet.enabled";
    public static final String JIRA_SCREENSHOTAPPLET_LINUX_ENABLED = "jira.screenshotapplet.linux.enabled";
    public static final String JIRA_SEARCH_MAXCLAUSES = "jira.search.maxclauses";

    public static final String JIRA_SENDMAIL_RECIPENT_BATCH_SIZE = "jira.sendmail.recipient.batch.size";

    // Clone issue
    public static final String JIRA_CLONE_PREFIX = "jira.clone.prefix";
    public static final String JIRA_CLONE_LINKTYPE_NAME = "jira.clone.linktype.name";
    /**
     * @deprecated since JIRA 6.1.1
     */
    @Deprecated
    public static final String JIRA_CLONE_LINK_LEGACY_DIRECTION = "jira.clone.link.legacy.direction";

    //Maximum length of Project Names
    public static final String JIRA_PROJECTNAME_MAX_LENGTH = "jira.projectname.maxlength";
    //Maximum length of Project Keys
    public static final String JIRA_PROJECTKEY_MAX_LENGTH = "jira.projectkey.maxlength";

    // Project Key Regular Expressions
    public static final String JIRA_PROJECTKEY_PATTERN = "jira.projectkey.pattern";
    public static final String JIRA_PROJECTKEY_WARNING = "jira.projectkey.warning";
    public static final String JIRA_PROJECTKEY_DESCRIPTION = "jira.projectkey.description";

    /**
     * List of reserved words that cannot be used for Project Keys
     */
    public static final String JIRA_PROJECTKEY_RESERVEDWORDS_LIST = "jira.projectkey.reservedwords.list";

    // Import/Export keys
    public static final String JIRA_IMPORT_CLEAN_XML = "jira.exportimport.cleanxml";
    public static final String INCLUDE_USER_IN_MAIL_FROMADDRESS = "jira.option.include.user.in.mail.from.address";

    public static final String EMAIL_FROMHEADER_FORMAT = "jira.email.fromheader.format";

    // Plugins
    public static final String GLOBAL_PLUGIN_STATE_PREFIX = "jira.plugin.state-";
    public static final String JIRA_PATH_PLUGINS = "jira.plugins";
    public static final String JIRA_PATH_INSTALLED_PLUGINS = "jira.plugins.installed";
    public static final String JIRA_PATH_PENDING_PLUGINS = "jira.plugins.pending";
    public static final String JIRA_PATH_UNINSTALLED_PLUGINS = "jira.plugins.uninstalled";

    // Auto-Export
    public static final String JIRA_AUTO_EXPORT = "jira.autoexport";

    public static final String IMPORT_ID_PREFIX = "jira.importid.prefix";
    public static final String IMPORT_ID_PREFIX_UNCONFIGURED = "unconfigured";
    public static final String FULL_CONTENT_VIEW_PAGEBREAKS = "jira.search.fullcontentview.pagebreaks";

    // Database
    public static final String DATABASE_QUERY_BATCH_SIZE = "jira.databasequery.batch.size";
    public static final String ISSUE_INDEX_FETCH_SIZE = "jira.issueindex.fetch.size";
    public static final String DEFAULT_JNDI_NAME = "jira.default.jndi.name";

    // Bulk User Management
    public static final String USER_MANAGEMENT_MAX_DISPLAY_MEMBERS = "jira.usermanagement.maxdisplaymembers";

    // CVS
    public static final String VIEWCVS_ROOT_TYPE = "jira.viewcvs.root.type";

    // Default schemes
    public static final String DEFAULT_ISSUE_TYPE_SCHEME = "jira.scheme.default.issue.type";

    // Columns when viewing list of issues in the dashboard
    public static final String ISSUE_TABLE_COLS_DASHBOARD = "jira.table.cols.dashboard";

    // Columns when viewing subtasks
    public static final String ISSUE_TABLE_COLS_SUBTASK = "jira.table.cols.subtasks";

    /**
     * Sorting order of the linked issues listed in view issue screen
     */
    public static final String JIRA_VIEW_ISSUE_LINKS_SORT_ORDER = "jira.view.issue.links.sort.order";

    /**
     * Wait time for a Lucene index file lock
     */
    public static final String JIRA_INDEX_LOCK_WAITTIME = "jira.index.lock.waittime";

    /**
     * Time Tracking related property keys
     */
    public static final String JIRA_OPTION_TIMETRACKING = "jira.option.timetracking";
    public static final String JIRA_OPTION_TIMETRACKING_ESTIMATES_LEGACY_BEHAVIOUR = "jira.timetracking.estimates.legacy.behaviour";
    public static final String JIRA_TIMETRACKING_COPY_COMMENT_TO_WORK_DESC_ON_TRANSITION = "jira.timetracking.copy.comment.to.work.desc.on.transition";
    public static final String JIRA_TIMETRACKING_FORMAT = "jira.timetracking.format";
    public static final String JIRA_TIMETRACKING_DEFAULT_UNIT = "jira.timetracking.default.unit";
    public static final String JIRA_TIMETRACKING_HOURS_PER_DAY = "jira.timetracking.hours.per.day";
    public static final String JIRA_TIMETRACKING_DAYS_PER_WEEK = "jira.timetracking.days.per.week";

    /**
     *  Unsupported browser warnings
     */
    public static final String JIRA_BROWSER_UNSUPPORTED_WARNINGS_DISABLED = "jira.browser.unsupported.warnings.disabled";

    /**
     * Number of issue indexes updates before automatic index optimization is triggered
     */
    public static final String JIRA_MAX_REINDEXES = "jira.index.max.reindexes";

    /**
     * Number of issue indexes performed in bulk that will trigger index optimization
     */
    public static final String JIRA_BULK_INDEX_UPDATE_OPTIMIZATION = "jira.index.update.bulk.optimization";

    /**
     * Whether AJAX enhancements for the edit issue screens should be enabled
     */
    public static final String JIRA_ISSUE_OPERATIONS_AJAX_ENABLED = "jira.issue.operations.ajax.enabled";

    /**
     * Number of results to display in the AJAX autocomplete pickers
     */
    public static final String JIRA_AJAX_AUTOCOMPLETE_LIMIT = "jira.ajax.autocomplete.limit";

    /**
     * Number of results to display in the label suggestions.  May be 0 for all suggestions.
     */
    public static final String JIRA_AJAX_LABEL_SUGGESTION_LIMIT = "jira.ajax.autocomplete.labelsuggestion.limit";

    /**
     * Whether or not the issue picker is ajaxified or not
     */
    public static final String JIRA_AJAX_ISSUE_PICKER_ENABLED = "jira.ajax.autocomplete.issuepicker.enabled";

    /**
     * The limit to the number of issue keys to store in the prev/next cache
     */
    public static final String JIRA_PREVIOUS_NEXT_CACHE_SIZE = "jira.previous.next.cache.size";

    /**
     * The maximum number of results the issue navigator search views will request
     */
    public static final String JIRA_SEARCH_VIEWS_DEFAULT_MAX = "jira.search.views.default.max";

    /**
     * The maximum number of results the issue navigator search views will return
     */
    public static final String JIRA_SEARCH_VIEWS_MAX_LIMIT = "jira.search.views.max.limit";

    /**
     * The maxium number of history records to keep for a user
     */
    public static final String JIRA_MAX_HISTORY_ITEMS = "jira.max.history.items";

    /**
     * The max number of entries to show for the history drop down
     */
    public static final String JIRA_MAX_ISSUE_HISTORY_DROPDOWN_ITEMS = "jira.max.issue.history.dropdown.items";

    /**
     * The max number of entries to show for the history drop down
     */
    public static final String JIRA_MAX_ADMIN_HISTORY_DROPDOWN_ITEMS = "jira.max.AdminPage.history.items";

    /**
     * The max number of entries to show for the filters drop down
     */
    public static final String JIRA_MAX_FILTER_DROPDOWN_ITEMS = "jira.max.issue.filter.dropdown.items";

    /**
     * regardless of the above, users in this group will be able to request very large search requests
     */
    public static final String JIRA_SEARCH_VIEWS_MAX_UNLIMITED_GROUP = "jira.search.views.max.unlimited.group";

    /**
     * limits the number of issues a user may select and edit in one go. *
     */
    public static final String JIRA_BULK_EDIT_LIMIT_ISSUE_COUNT = "jira.bulk.edit.limit.issue.count";

    /**
     * limits the number of bulk transition errors shown on UI.
     */
    public static final String JIRA_BULK_EDIT_LIMIT_TRANSITION_ERRORS = "jira.bulk.edit.limit.transition.errors";

    /**
     * Defines if a multipart get request should be handle *
     */
    public static final String JIRA_DISABLE_MULTIPART_GET_HTTP_REQUEST = "jira.disable.multipart.get.http.request";

    /**
     * Defines policy for attachment downloads and ie mime sniffing.
     */
    public static final String JIRA_OPTION_IE_MIME_SNIFFING = "jira.attachment.download.mime.sniffing.workaround";

    /**
     * Option to allow downloading attachments as a ZIP file
     */
    public static final String JIRA_OPTION_ALLOW_ZIP_SUPPORT = "jira.attachment.allow.zip.support";

    /**
     * Defines the number of zip entries to show on the view issue screen
     */
    public static final String JIRA_ATTACHMENT_NUMBER_OF_ZIP_ENTRIES_TO_SHOW = "jira.attachment.number.of.zip.entries";

    /**
     * Defines a comma-separated list of the file extensions that JIRA won't expand as a ZIP in the view issue screen.
     */
    public static final String JIRA_ATTACHMENT_DO_NOT_EXPAND_AS_ZIP_EXTENSIONS_LIST = "jira.attachment.do.not.expand.as.zip.extensions.list";

    /**
     * mime sniffing policy allowing inlining of all attachments.
     */
    public static final String MIME_SNIFFING_OWNED = "insecure";

    /**
     * mime sniffing policy allowing inlining of attachments except in ie when it would detect html and run scripts.
     */
    public static final String MIME_SNIFFING_WORKAROUND = "workaround";

    /**
     * mime sniffing policy allowing no inlining of attachments forcing download with Content-Disposition header.
     */
    public static final String MIME_SNIFFING_PARANOID = "secure";

    /**
     * maximum number of issues to display in a fragment in the Browse Project Summary tab panel
     */
    public static final String JIRA_PROJECT_SUMMARY_MAX_ISSUES = "jira.project.summary.max.issues";

    /**
     * introduced as part of http://jira.atlassian.com/browse/JRA-6344
     */
    public static final String JIRA_ASSIGNEE_CHANGE_IS_SENT_TO_BOTH_PARTIES = "jira.assignee.change.is.sent.to.both.parties";

    /**
     * Prefix for days previous limits for charts
     */
    public static final String JIRA_CHART_DAYS_PREVIOUS_LIMIT_PREFIX = "jira.chart.days.previous.limit.";

    /**
     * Database id of the default avatar.
     */
    public static final String JIRA_DEFAULT_AVATAR_ID = "jira.avatar.default.id";

    /**
     * Database id of the default user avatar.
     */
    public static final String JIRA_DEFAULT_USER_AVATAR_ID = "jira.avatar.user.default.id";

    /**
     * Database id of the avatar for anonymous users
     */
    public static final String JIRA_ANONYMOUS_USER_AVATAR_ID = "jira.avatar.user.anonymous.id";

    /**
     * Flag for whether to enable or disable autocomplete for JQL.
     */
    public static final String JIRA_JQL_AUTOCOMPLETE_DISABLED = "jira.jql.autocomplete.disabled";

    /**
     * Used to limit the number of gadgets per dashboard. 20 by default
     */
    public static final String JIRA_DASHBOARD_MAX_GADGETS = "jira.dashboard.max.gadgets";

    /**
     * If this is set to true, the login gadget wont get added to the system dashboard for logged out users
     */
    public static final String JIRA_DISABLE_LOGIN_GADGET = "jira.disable.login.gadget";

    /**
     * If this is set to true, the admin gadget will show the "Getting Started" task list
     */
    public static final String JIRA_ADMIN_GADGET_TASK_LIST_ENABLED = "jira.admin.gadget.task.list.enabled";


    /**
     * This is the maximum failed authentication attempts allowed before things get serious
     */
    public static final String JIRA_MAXIMUM_AUTHENTICATION_ATTEMPTS_ALLOWED = "jira.maximum.authentication.attempts.allowed";

    /**
     * Determines if the gadget upgrade message for applinks should still be displayed for administrators
     * @deprecated no longer used as of JIRA 6.1. Will be removed in JIRA 7.0.
     */
    @Deprecated
    public static final String JIRA_GADGET_APPLINK_UPGRADE_FINISHED = "jira.gadget.applink.upgrade.finished";

    /**
     * Returns a list of newline separated strings representing the http whitelist for JIRA
     * @deprecated no longer used as of JIRA 6.1. Will be removed in JIRA 7.0.
     */
    @Deprecated
    public static final String JIRA_WHITELIST_RULES = "jira.whitelist.rules";

    /**
     * Returns true if the http whitelist should be disabled.
     * @deprecated no longer used as of JIRA 6.1. Will be removed in JIRA 7.0.
     */
    @Deprecated
    public static final String JIRA_WHITELIST_DISABLED = "jira.whitelist.disabled";

    /**
     * Either asc or desc to define the default comments order.
     */
    public static final String JIRA_ISSUE_ACTIONS_ORDER = "jira.issue.actions.order";

    /**
     * Returns true if we show the email form on the contact administrators form.
     */
    public static final String JIRA_SHOW_CONTACT_ADMINISTRATORS_FORM = "jira.show.contact.administrators.form";

    /**
     * Returns message to show on the contact administrators form.
     */
    public static final String JIRA_CONTACT_ADMINISTRATORS_MESSSAGE = "jira.contact.administrators.message";

    /**
     * The maximum number of characters to be entered for a single field.
     */
    public static final String JIRA_TEXT_FIELD_CHARACTER_LIMIT = "jira.text.field.character.limit";

    /**
     * The limit of IDs to retrieve when doing a stable search.
     */
    public static final String JIRA_STABLE_SEARCH_MAX_RESULTS = "jira.search.stable.max.results";

    /**
     * The number of Issues to keep in the cache
     */
    public static final String JIRA_SEARCH_CACHE_MAX_SIZE = "jira.search.cache.max.size";

    /**
     * Returns true if advertisements in JIRA are disabled
     */
    public static final String JIRA_OPTION_ADS_DISABLED = "jira.ads.disabled";

    /**
     * Returns mode of project description, either html or wiki
     */
    public static final String JIRA_OPTION_PROJECT_DESCRIPTION_HTML_ENABLED = "jira.project.description.html.enabled";

    /**
     * The number of entries to keep in the security level to permissions cache
     */
    public static final String JIRA_SECURITY_LEVEL_PERMISSIONS_CACHE_MAX_SIZE = "jira.security.level.permission.cache.max.size";

    /** @deprecated since 6.2 */
    @Deprecated
    public static final String JIRA_OPTION_BTF_ANALYTICS_ENABLED = "jira.btf.analytics.enabled";

    public static class WebSudo
    {
        public static final String IS_DISABLED = "jira.websudo.is.disabled";
        public static final String TIMEOUT = "jira.websudo.timeout";

        private WebSudo() {}
    }

    public static class TrustedApplications
    {
        public static final String USER_NAME_TRANSFORMER_CLASS = "jira.trustedapps.user.name.transformation.policy.class";

        private TrustedApplications() {}
    }

    public static final class Export
    {
        private static final String PREFIX = "jira.export.";

        public static final String FETCH_SIZE = PREFIX + "fetchsize";
    }

    public static final class Import
    {
        private static final String PREFIX = "jira.import.";

        public static final String MAX_QUEUE_SIZE = PREFIX + "maxqueuesize";
        public static final String THREADS = PREFIX + "threads";
    }

    /**
     * Lucene IndexWriter configuration
     */
    public static final class JiraIndexConfiguration
    {
        private static final String PREFIX = "jira.index.";

        public static final String MAX_FIELD_LENGTH = PREFIX + "maxfieldlength";

        /**
         * Batch mode config
         */
        public static final class Batch
        {
            private static final String BATCH_PREFIX = PREFIX + "batch.";

            public static final String MERGE_FACTOR = BATCH_PREFIX + "mergefactor";
            public static final String MAX_MERGE_DOCS = BATCH_PREFIX + "maxmergedocs";
            public static final String MAX_BUFFERED_DOCS = BATCH_PREFIX + "maxbuffereddocs";
        }

        /**
         * Interactive mode config
         */
        public static final class Interactive
        {
            private static final String INTERACTIVE_PREFIX = PREFIX + "interactive.";

            public static final String MERGE_FACTOR = INTERACTIVE_PREFIX + "mergefactor";
            public static final String MAX_MERGE_DOCS = INTERACTIVE_PREFIX + "maxmergedocs";
            public static final String MAX_BUFFERED_DOCS = INTERACTIVE_PREFIX + "maxbuffereddocs";
        }

        /**
         * Merge policy config
         */
        public static final class MergePolicy
        {
            private static final String MERGE_POLICY_PREFIX = PREFIX + "mergepolicy.";

            public static final String EXPUNGE_DELETES_PCT_ALLOWED = MERGE_POLICY_PREFIX + "expungedeletespctallowed";
            public static final String FLOOR_SEGMENT_MB = MERGE_POLICY_PREFIX + "floorsegmentmb";
            public static final String MAX_MERGE_AT_ONCE = MERGE_POLICY_PREFIX + "maxmergeatonce";
            public static final String MAX_MERGE_AT_ONCE_EXPLICIT = MERGE_POLICY_PREFIX + "maxmergeatonceexplicit";
            public static final String MAX_MERGED_SEGMENT_MB = MERGE_POLICY_PREFIX + "maxmergedsegmentmb";
            public static final String NO_CFS_PCT = MERGE_POLICY_PREFIX + "nocfspct";
            public static final String SEGMENTS_PER_TIER = MERGE_POLICY_PREFIX + "segmentspertier";
            public static final String USE_COMPOUND_FILE = MERGE_POLICY_PREFIX + "usecompoundfile";
        }

        /**
         * Issue indexing
         */
        public static final class Issue
        {
            private static final String ISSUE_PREFIX = PREFIX + "issue.";

            public static final String MIN_BATCH_SIZE = ISSUE_PREFIX + "minbatchsize";
            public static final String MAX_QUEUE_SIZE = ISSUE_PREFIX + "maxqueuesize";
            public static final String THREADS = ISSUE_PREFIX + "threads";
        }

        /**
         * SharedEntity indexing
         */
        public static final class SharedEntity
        {
            private static final String SHARED_ENTITY_PREFIX = PREFIX + "sharedentity.";

            public static final String MIN_BATCH_SIZE = SHARED_ENTITY_PREFIX + "minbatchsize";
            public static final String MAX_QUEUE_SIZE = SHARED_ENTITY_PREFIX + "maxqueuesize";
            public static final String THREADS = SHARED_ENTITY_PREFIX + "threads";
        }

    }

    /**
     * Max number of schemes that can be compared in the scheme comparison tool
     */
    public static final String JIRA_MAX_SCHEMES_FOR_COMPARISON = "jira.schemes.comparison.max";

    /**
     * Flag that enables/disables either Project Roles & Groups or Project Roles.
     */
    public static final String COMMENT_LEVEL_VISIBILITY_GROUPS = "jira.comment.level.visibility.groups";

    /**
     * The minimum number of comments that can be hidden by comment collapsing. 0 means no comment can be hidden.
     */
    public static final String COMMENT_COLLAPSING_MINIMUM_HIDDEN = "jira.comment.collapsing.minimum.hidden";

    /**
     * Key for JIRA's SID
     */
    public static final String JIRA_SID = "jira.sid.key";

    /**
     * The last date at which all web resources should be flushed.
     */
    public static final String WEB_RESOURCE_FLUSH_COUNTER = "jira.webresource.flushcounter";

     /**
     * Counter that can be used to flush super batched web-resources.
     */
    public static final String WEB_RESOURCE_SUPER_BATCH_FLUSH_COUNTER = "jira.webresource.superbatch.flushcounter";

    /**
     * Show marketing links.  These is a cross product property.
     */
    public static final String JIRA_SHOW_MARKETING_LINKS = "show.plugin.marketing.hints";

    /**
     * Database id of the default issueType avatar.
     */
    public static final String JIRA_DEFAULT_ISSUETYPE_AVATAR_ID = "jira.avatar.issuetype.default.id";
    /**
     * Database id of the default issueType subtask avatar.
     */
    public static final String JIRA_DEFAULT_ISSUETYPE_SUBTASK_AVATAR_ID = "jira.avatar.issuetype.subtask.default.id";


    /**
     * These are the Lucene indexing Languages, not the languages for displaying user messages.
     */
    public class Languages
    {
        public static final String ARMENIAN = "armenian";
        public static final String BASQUE = "basque";
        public static final String BRAZILIAN = "brazilian";
        public static final String BULGARIAN = "bulgarian";
        public static final String CATALAN = "catalan";
        public static final String CHINESE = "chinese";
        public static final String CJK = "cjk";
        public static final String CZECH = "czech";
        public static final String DANISH = "danish";
        public static final String DUTCH = "dutch";
        public static final String ENGLISH = "english";
        public static final String ENGLISH_MODERATE_STEMMING="english-moderate-stemming";
        public static final String ENGLISH_MINIMAL_STEMMING="english-minimal-stemming";
        public static final String FINNISH = "finnish";
        public static final String FRENCH = "french";
        public static final String GERMAN = "german";
        public static final String GREEK = "greek";
        public static final String HUNGARIAN = "hungarian";
        public static final String ITALIAN = "italian";
        public static final String NORWEGIAN = "norwegian";
        public static final String PORTUGUESE = "portuguese";
        public static final String ROMANIAN = "romanian";
        public static final String RUSSIAN = "russian";
        public static final String SPANISH = "spanish";
        public static final String SWEDISH = "swedish";
        public static final String THAI = "thai";
        public static final String OTHER = "other";
    }
}
