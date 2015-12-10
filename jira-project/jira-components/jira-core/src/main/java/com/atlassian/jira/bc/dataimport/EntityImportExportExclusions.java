package com.atlassian.jira.bc.dataimport;

import java.util.Set;

import com.atlassian.jira.entity.Entity;

import com.google.common.collect.ImmutableSet;

public class EntityImportExportExclusions
{
    public static Set<String> ENTITIES_EXCLUDED_FROM_IMPORT_EXPORT = ImmutableSet.of(Entity.Name.ENTITY_PROPERTY_INDEX_DOCUMENT);
}
