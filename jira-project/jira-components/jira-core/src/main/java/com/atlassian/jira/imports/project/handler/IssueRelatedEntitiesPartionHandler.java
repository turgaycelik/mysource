package com.atlassian.jira.imports.project.handler;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.atlassian.jira.entity.property.EntityPropertyType;
import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalChangeGroup;
import com.atlassian.jira.external.beans.ExternalChangeItem;
import com.atlassian.jira.external.beans.ExternalComment;
import com.atlassian.jira.external.beans.ExternalEntityProperty;
import com.atlassian.jira.external.beans.ExternalLink;
import com.atlassian.jira.external.beans.ExternalNodeAssociation;
import com.atlassian.jira.external.beans.ExternalVoter;
import com.atlassian.jira.external.beans.ExternalWatcher;
import com.atlassian.jira.imports.project.core.BackupProject;
import com.atlassian.jira.imports.project.parser.ChangeGroupParser;
import com.atlassian.jira.imports.project.parser.ChangeGroupParserImpl;
import com.atlassian.jira.imports.project.parser.ChangeItemParser;
import com.atlassian.jira.imports.project.parser.ChangeItemParserImpl;
import com.atlassian.jira.imports.project.parser.CommentParser;
import com.atlassian.jira.imports.project.parser.CommentParserImpl;
import com.atlassian.jira.imports.project.parser.EntityPropertyParser;
import com.atlassian.jira.imports.project.parser.EntityPropertyParserImpl;
import com.atlassian.jira.imports.project.parser.IssueLinkParser;
import com.atlassian.jira.imports.project.parser.IssueLinkParserImpl;
import com.atlassian.jira.imports.project.parser.IssueParser;
import com.atlassian.jira.imports.project.parser.NodeAssociationParser;
import com.atlassian.jira.imports.project.parser.NodeAssociationParserImpl;
import com.atlassian.jira.imports.project.parser.UserAssociationParser;
import com.atlassian.jira.imports.project.parser.UserAssociationParserImpl;
import com.atlassian.jira.issue.IssueRelationConstants;

import org.ofbiz.core.entity.DelegatorInterface;
import org.ofbiz.core.entity.GenericEntity;
import org.ofbiz.core.entity.model.ModelEntity;

/**
 * Parses an XML import file and writes a smaller "partition" containing just the values for certain issue-related
 * entities that are valid for the project we are importing.
 *
 * @since v3.13
 */
public class IssueRelatedEntitiesPartionHandler extends AbstractImportPartitionHandler
{

    private final BackupProject backupProject;
    private final Map<String, ModelEntity> modelEntityMap;
    private final PrintWriter printWriter;
    private final PrintWriter secondDegreeEntityXmlWriter;
    private final Set<Long> changeGroupIds;
    private final Set<Long> commentIds;
    private ChangeGroupParser changeGroupParser;
    private ChangeItemParser changeItemParser;
    private UserAssociationParser userAssociationParser;
    private EntityPropertyParser entityPropertyParser;
    private NodeAssociationParser nodeAssocationParser;
    private IssueLinkParser issueLinkParser;
    private CommentParser commentParser;
    private int entityCount = 0;
    private int secondDegreeEntityCount = 0;

    /**
     * @param backupProject contains the issue id's that we are interested in partitioning.
     * @param printWriter the partitioned writer that should be written to if the entity being processed should be
     * written.
     * @param secondDegreeEntityXmlWriter the writer that will write
     * @param modelEntities a List of {@link org.ofbiz.core.entity.model.ModelEntity}'s that the partitioner should be
     * interested in.
     * @param encoding is the encoding that the partitioned files are going to writen in.
     * @param delegatorInterface required for persistence
     */
    public IssueRelatedEntitiesPartionHandler(final BackupProject backupProject, final PrintWriter printWriter, final PrintWriter secondDegreeEntityXmlWriter, final List /*<ModelEntity>*/modelEntities, final String encoding, final DelegatorInterface delegatorInterface)
    {
        super(printWriter, encoding, delegatorInterface);
        this.backupProject = backupProject;
        this.printWriter = printWriter;
        this.secondDegreeEntityXmlWriter = secondDegreeEntityXmlWriter;
        modelEntityMap = new HashMap<String, ModelEntity>();
        changeGroupIds = new HashSet<Long>();
        commentIds = new HashSet<Long>();
        buildModelEntityMap(modelEntities);
    }

    public void handleEntity(final String entityName, final Map<String, String> attributes) throws ParseException
    {
        final ModelEntity modelEntity = modelEntityMap.get(entityName);
        if (modelEntity == null)
        {
            return;
        }
        boolean saveEntity;
        // A couple of entity types store the issueID in a non-standard way
        if (NodeAssociationParser.NODE_ASSOCIATION_ENTITY_NAME.equals(entityName))
        {
            saveEntity = handleNodeAssociation(attributes);
        }
        // for Issue Links
        else if (IssueLinkParser.ISSUE_LINK_ENTITY_NAME.equals(entityName))
        {
            saveEntity = handleIssueLink(attributes);
        }
        // for change items
        else if (ChangeItemParser.CHANGE_ITEM_ENTITY_NAME.equals(entityName))
        {
            // This is a special case since we write it to a different XML file
            saveEntity = false;
            if (handleChangeItem(attributes))
            {
                saveSecondDegreeEntity(modelEntity, attributes);
            }
        }
        else if (UserAssociationParser.USER_ASSOCIATION_ENTITY_NAME.equals(entityName))
        {
            saveEntity = handleVotersAndWatchers(attributes);
        }
        else if (EntityPropertyParser.ENTITY_PROPERTY_ENTITY_NAME.equals(entityName))
        {
            saveEntity = handleEntityProperty(modelEntity, attributes);
        }
        else
        {
            final String issueId = attributes.get("issue");
            saveEntity = backupProject.containsIssue(issueId);
        }

        if (saveEntity)
        {
            // If we are inspecting a changeGroup then save its id
            handleChangeGroup(entityName, attributes);
            handleComment(entityName, attributes);
            // Create a GenericEntity
            final GenericEntity genericEntity = new GenericEntity(delegator, modelEntity, attributes);
            genericEntity.writeXmlText(printWriter, null);
            // Count the entities as we go, we use the count later for our task progress bar.
            entityCount++;
        }
    }

    private void saveSecondDegreeEntity(final ModelEntity modelEntity, final Map<String, String> attributes)
    {
        // Create a GenericEntity
        final GenericEntity genericEntity = new GenericEntity(delegator, modelEntity, attributes);
        genericEntity.writeXmlText(secondDegreeEntityXmlWriter, null);
        // Count the entities as we go, we use the count later for our task progress bar.
        secondDegreeEntityCount++;
    }

    private boolean handleEntityProperty(final ModelEntity modelEntity, final Map<String, String> attributes) throws ParseException
    {
        final ExternalEntityProperty entityProperty = getEntityPropertyParser().parse(attributes);
        if (EntityPropertyType.ISSUE_PROPERTY.getDbEntityName().equals(entityProperty.getEntityName()))
        {
            return backupProject.containsIssue(String.valueOf(entityProperty.getEntityId()));
        }
        else if (EntityPropertyType.COMMENT_PROPERTY.getDbEntityName().equals(entityProperty.getEntityName()) && commentIds.contains(entityProperty.getEntityId()))
        {
            saveSecondDegreeEntity(modelEntity, attributes);
            return false;
        }
        else if (EntityPropertyType.CHANGE_HISTORY_PROPERTY.getDbEntityName().equals(entityProperty.getEntityName()) && changeGroupIds.contains(entityProperty.getEntityId()))
        {
            saveSecondDegreeEntity(modelEntity, attributes);
            return false;
        }
        return false;

    }

    public int getEntityCount()
    {
        return entityCount;
    }

    public int getSecondDegreeEntityCount()
    {
        return secondDegreeEntityCount;
    }

    private boolean handleVotersAndWatchers(final Map<String, String> attributes) throws ParseException
    {
        final ExternalVoter externalVoter = getUserAssociationParser().parseVoter(attributes);
        if (externalVoter != null)
        {
            return backupProject.containsIssue(externalVoter.getIssueId());
        }
        final ExternalWatcher externalWatcher = getUserAssociationParser().parseWatcher(attributes);
        if (externalWatcher != null)
        {
            return backupProject.containsIssue(externalWatcher.getIssueId());
        }
        return false;
    }

    private boolean handleChangeItem(final Map<String, String> attributes) throws ParseException
    {
        final ExternalChangeItem externalChangeItem = getChangeItemParser().parse(attributes);
        try
        {
            final Long groupId = new Long(externalChangeItem.getChangeGroupId());
            return changeGroupIds.contains(groupId);
        }
        catch (final NumberFormatException e)
        {
            throw new ParseException("Unable to parse the changeGroup id'" + externalChangeItem.getChangeGroupId() + "' for change item.");
        }
    }

    public void startDocument()
    {
        super.startDocument();
        if (secondDegreeEntityXmlWriter != null)
        {
            secondDegreeEntityXmlWriter.println("<?xml version=\"1.0\" encoding=\"" + getEncoding() + "\"?>");
            secondDegreeEntityXmlWriter.println("<entity-engine-xml>");
        }
    }

    public void endDocument()
    {
        super.endDocument();
        if (secondDegreeEntityXmlWriter != null)
        {
            secondDegreeEntityXmlWriter.print("</entity-engine-xml>");
        }
    }

    ///CLOVER:OFF - NOTE: this is mainly here for testing purposes
    public Map<String, ModelEntity> getRegisteredHandlers()
    {
        return Collections.unmodifiableMap(modelEntityMap);
    }

    ///CLOVER:ON

    private void handleComment(final String entityName, final Map<String, String> attributes) throws ParseException
    {
        if (CommentParser.COMMENT_ENTITY_NAME.equals(entityName))
        {
            final ExternalComment externalComment = getCommentParser().parse(attributes);
            final String externalCommentId = externalComment.getId();
            if (externalCommentId != null)
            {
                try
                {
                    final Long id = new Long(externalCommentId);
                    commentIds.add(id);
                }
                catch (final NumberFormatException e)
                {
                    throw new ParseException("Unable to parse the id for changeGroup '" + externalCommentId + "'");
                }
            }
            else
            {
                throw new ParseException("Encountered a Comment entry without an id, this should not happen");
            }
        }
    }

    private void handleChangeGroup(final String entityName, final Map<String, String> attributes) throws ParseException
    {
        // keep the change group id's so we can correctly store the changeItems
        if (ChangeGroupParser.CHANGE_GROUP_ENTITY_NAME.equals(entityName))
        {
            final ExternalChangeGroup externalChangeGroup = getChangeGroupParser().parse(attributes);
            final String idStr = externalChangeGroup.getId();
            if (idStr != null)
            {
                try
                {
                    final Long id = new Long(idStr);
                    changeGroupIds.add(id);
                }
                catch (final NumberFormatException e)
                {
                    throw new ParseException("Unable to parse the id for changeGroup '" + idStr + "'");
                }
            }
            else
            {
                throw new ParseException("Encountered a ChangeGroup entry without an id, this should not happen.");
            }
        }
    }

    private boolean handleIssueLink(final Map<String, String> attributes) throws ParseException
    {
        // Let the IssueLink Parser parse the xml:
        final ExternalLink externalLink = getIssueLinkParser().parse(attributes);

        return backupProject.containsIssue(externalLink.getSourceId()) || backupProject.containsIssue(externalLink.getDestinationId());
    }

    private boolean handleNodeAssociation(final Map<String, String> attributes) throws ParseException
    {
        final ExternalNodeAssociation externalNodeAssociation = getNodeAssociationParser().parse(attributes);
        if (IssueParser.ISSUE_ENTITY_NAME.equals(externalNodeAssociation.getSourceNodeEntity()) && (IssueRelationConstants.VERSION.equals(externalNodeAssociation.getAssociationType()) || IssueRelationConstants.FIX_VERSION.equals(externalNodeAssociation.getAssociationType()) || IssueRelationConstants.COMPONENT.equals(externalNodeAssociation.getAssociationType())))
        {
            return backupProject.containsIssue(externalNodeAssociation.getSourceNodeId());
        }
        return false;
    }

    private void buildModelEntityMap(final List modelEntities)
    {
        final Iterator iter = modelEntities.iterator();
        while (iter.hasNext())
        {
            final ModelEntity modelEntity = (ModelEntity) iter.next();
            modelEntityMap.put(modelEntity.getEntityName(), modelEntity);
        }
    }

    ///CLOVER:OFF
    private NodeAssociationParser getNodeAssociationParser()
    {
        if (nodeAssocationParser == null)
        {
            nodeAssocationParser = new NodeAssociationParserImpl();
        }
        return nodeAssocationParser;
    }

    ///CLOVER:ON

    ///CLOVER:OFF
    private IssueLinkParser getIssueLinkParser()
    {
        if (issueLinkParser == null)
        {
            issueLinkParser = new IssueLinkParserImpl();
        }
        return issueLinkParser;
    }

    ///CLOVER:ON

    ///CLOVER:OFF
    private ChangeGroupParser getChangeGroupParser()
    {
        if (changeGroupParser == null)
        {
            changeGroupParser = new ChangeGroupParserImpl();
        }
        return changeGroupParser;
    }

    ///CLOVER:ON

    ///CLOVER:OFF
    private ChangeItemParser getChangeItemParser()
    {
        if (changeItemParser == null)
        {
            changeItemParser = new ChangeItemParserImpl();
        }
        return changeItemParser;
    }

    ///CLOVER:ON

    ///CLOVER:OFF
    UserAssociationParser getUserAssociationParser()
    {
        if (userAssociationParser == null)
        {
            userAssociationParser = new UserAssociationParserImpl();
        }
        return userAssociationParser;
    }
    ///CLOVER:ON

    //CLOVER:OFF
    EntityPropertyParser getEntityPropertyParser()
    {
        if (entityPropertyParser == null)
        {
            entityPropertyParser = new EntityPropertyParserImpl();
        }
        return entityPropertyParser;
    }
    ///CLOVER:ON

    CommentParser getCommentParser()
    {
        if (commentParser == null)
        {
            commentParser = new CommentParserImpl();
        }
        return commentParser;
    }
}