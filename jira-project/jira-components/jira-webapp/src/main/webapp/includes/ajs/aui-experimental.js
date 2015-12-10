AJS.namespace('AJS.asBooleanOrString', null, require('jira/util/strings').asBooleanOrString);

/**
 * A backing map to use if the user sets data.
 * @deprecated
 */
AJS.namespace('AJS.overrides', null, require('jira/util/data/meta/store'));

/**
 * AJS.Meta will be the namespace for accessing dynamic metadata passed from the
 * server to JavaScript via the page HTML.
 *
 * @since 5.0
 */
AJS.namespace('AJS.Meta', null, require('jira/util/data/meta'));
