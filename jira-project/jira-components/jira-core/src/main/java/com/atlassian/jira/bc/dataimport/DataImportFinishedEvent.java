package com.atlassian.jira.bc.dataimport;

/**
 * Sent when a data import completes, but before the plugins are restarted. Listeners may use this to purge unwanted
 * imported configuration, before lifecycle tasks re-create any config.
 */
public class DataImportFinishedEvent
{

}