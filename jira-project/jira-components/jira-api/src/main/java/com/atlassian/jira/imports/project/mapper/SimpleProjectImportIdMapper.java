package com.atlassian.jira.imports.project.mapper;

import com.atlassian.annotations.PublicApi;

/**
 * Defines a value mapper used for project import. This allows us to map an old value with a new value in the target system.
 * Each instance of ProjectImportIdMapper represents a particular type of object in the system; eg IssueType, Issue, etc.
 *
 * @since v3.13
 */
@PublicApi
public interface SimpleProjectImportIdMapper extends ProjectImportIdMapper, MapperEntityRegister
{

    /**
     * Flags an old value, which should eventually be registered via {@link #registerOldValue(String, String)}, such
     * that this mapper will indicate that the value MUST be mapped to a new value. 
     *
     * @param oldId the string representation for the id of the backup value.
     */
    void flagValueAsRequired(String oldId);

}