package com.atlassian.jira.imports.project.taskprogress;

/**
 * An interface that allows for processing the task progress in different ways.
 * The first time we process the import XML file, we don't know exactly how big it is.
 * But we know how many entity types there are.
 * So that time we use the "EntityType" processor and it just tells us the percentage completion of entity types.
 * On the other hand, The second time we process it, and when we process the partitioned files, we can have counted
 * the total number of entities,and use these numbers for a more accurate "percent done".
 */
public interface TaskProgressProcessor
{
    /**
     * Update the task progress in the TaskProgressSink.
     *
     * @param entityTypeName Name of the entity type that we are currently processing. eg "IssueType"
     * @param entityTypeCount Count of the Entity Type that we are currently processing.
     * @param entityCount Count of ALL the entities we have seen so far in this file.
     * @param currentEntityCount Count of entities just within this entity type.
     */
    void processTaskProgress(String entityTypeName, int entityTypeCount, long entityCount, long currentEntityCount);
}
