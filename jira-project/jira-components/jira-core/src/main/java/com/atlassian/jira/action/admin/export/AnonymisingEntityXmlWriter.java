package com.atlassian.jira.action.admin.export;

import org.ofbiz.core.entity.GenericValue;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import com.atlassian.jira.issue.attachment.AttachmentConstants;

/**
 * A writer that replaces many characters with 'x' characters, for the purposes of anonymising data. The set of
 * characters that will be replaced is defined in {@link com.atlassian.jira.action.admin.export.AnonymousGenericValue}
 * <p/>
 * This would be used to anonymise data before sending it to Atlassian for instance.  It should <em>never</em> be used
 * to <em>back up</em> data, as the data would be unable to be restored.
 */
public class AnonymisingEntityXmlWriter implements EntityXmlWriter
{
    private final Collection anonymousEntities;

    // Note that we use a collection rather than a hashSet because AnonymousEntity does not
    // implement hashCode in any meaninful fashion, and therefore there is no performance gain
    // from using a set
    public static final Collection<AnonymousEntity> DEFAULT_ANONYMOUS_ENTITIES;

    static
    {
        Collection<AnonymousEntity> defaultAnonymousEntities = new ArrayList<AnonymousEntity>();
        defaultAnonymousEntities.add(new AnonymousEntity("Action", "body"));
        defaultAnonymousEntities.add(new AnonymousEntity("Issue", "environment"));
        defaultAnonymousEntities.add(new AnonymousEntity("Issue", "summary"));
        defaultAnonymousEntities.add(new AnonymousEntity("NotificationInstance", "email"));
        defaultAnonymousEntities.add(new AnonymousEntity("ChangeItem", "newstring"));
        defaultAnonymousEntities.add(new AnonymousEntity("ChangeItem", "oldstring"));
        defaultAnonymousEntities.add(new AnonymousEntity(AttachmentConstants.ATTACHMENT_ENTITY_NAME, "filename"));
        defaultAnonymousEntities.add(new AnonymousEntity("NotificationScheme", "name"));
        defaultAnonymousEntities.add(new AnonymousEntity("PermissionScheme", "name"));
        defaultAnonymousEntities.add(new AnonymousEntity("Resolution", "name"));
        defaultAnonymousEntities.add(new AnonymousEntity(null, "description"));
        defaultAnonymousEntities.add(new AnonymousEntity("CustomFieldValue", "textvalue"));
        defaultAnonymousEntities.add(new AnonymousEntity("MailServer", "name"));
        defaultAnonymousEntities.add(new AnonymousEntity("MailServer", "servername"));
        defaultAnonymousEntities.add(new AnonymousEntity("MailServer", "username"));
        defaultAnonymousEntities.add(new AnonymousEntity("MailServer", "password"));
        defaultAnonymousEntities.add(new AnonymousEntity("Worklog", "body"));
        DEFAULT_ANONYMOUS_ENTITIES = Collections.unmodifiableCollection(defaultAnonymousEntities);
    }

    /**
     * @param anonymousEntities A collection of {@link AnonymousEntity} objects, representing the entities to escape
     */
    public AnonymisingEntityXmlWriter(Collection anonymousEntities)
    {
        this.anonymousEntities = anonymousEntities;
    }

    /**
     * An Anonymising writer, that is initalised using the default JIRA entities from {@link
     * #DEFAULT_ANONYMOUS_ENTITIES}
     */
    public AnonymisingEntityXmlWriter()
    {
        this(DEFAULT_ANONYMOUS_ENTITIES);
    }

    public void writeXmlText(GenericValue entity, PrintWriter writer)
    {
        GenericValue anonymisingGV = new AnonymousGenericValue(entity, anonymousEntities);
        anonymisingGV.writeXmlText(writer, "");
    }

}
