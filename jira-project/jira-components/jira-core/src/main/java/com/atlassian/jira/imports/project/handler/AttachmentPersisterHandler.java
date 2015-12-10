package com.atlassian.jira.imports.project.handler;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalAttachment;
import com.atlassian.jira.imports.project.ProjectImportPersister;
import com.atlassian.jira.imports.project.core.BackupProject;
import com.atlassian.jira.imports.project.core.BackupSystemInformation;
import com.atlassian.jira.imports.project.core.ProjectImportOptions;
import com.atlassian.jira.imports.project.core.ProjectImportResults;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.imports.project.parser.AttachmentParser;
import com.atlassian.jira.imports.project.parser.AttachmentParserImpl;
import com.atlassian.jira.imports.project.transformer.AttachmentTransformer;
import com.atlassian.jira.imports.project.transformer.AttachmentTransformerImpl;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.attachment.AttachmentStore;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.Map;
import java.util.concurrent.Executor;

import static com.atlassian.jira.imports.project.handler.AttachmentFileValidatorHandler.canProcessEntity;

/**
 * Reads, transforms, and stores all attachment entities from a backup file and copies the actual attachment,
 * as specified in the attachment directory into the JIRA attachment format.
 *
 * It is assumed that all attachment data that is processed by this handler is relevant and should be saved.
 *
 * NOTE: This handler will not process attachment data if {@link com.atlassian.jira.imports.project.core.ProjectImportOptions#getAttachmentPath()}
 * is null.
 *
 * @since v3.13
 */
public class AttachmentPersisterHandler extends AbstractPersisterHandler implements ImportEntityHandler
{
    private static final Logger log = Logger.getLogger(AttachmentPersisterHandler.class);

    private final ProjectImportPersister projectImportPersister;
    private final ProjectImportOptions projectImportOptions;
    private final ProjectImportMapper projectImportMapper;
    private final BackupProject backupProject;
    private final BackupSystemInformation backupSystemInformation;
    private final ProjectImportResults projectImportResults;
    private final AttachmentStore attachmentStore;

    protected Supplier<AttachmentParser> attachmentParser = Suppliers.memoize(new Supplier<AttachmentParser>()
    {
        @Override
        public AttachmentParser get()
        {
            return new AttachmentParserImpl(attachmentStore, projectImportOptions.getAttachmentPath());
        }
    });

    private AttachmentTransformer attachmentTransformer;

    public AttachmentPersisterHandler(final ProjectImportPersister projectImportPersister,
            final ProjectImportOptions projectImportOptions, final ProjectImportMapper projectImportMapper,
            final BackupProject backupProject, final BackupSystemInformation backupSystemInformation,
            final ProjectImportResults projectImportResults, final Executor executor,
            final AttachmentStore attachmentStore)
    {
        super(executor, projectImportResults);

        this.projectImportPersister = projectImportPersister;
        this.projectImportOptions = projectImportOptions;
        this.projectImportMapper = projectImportMapper;
        this.backupProject = backupProject;
        this.backupSystemInformation = backupSystemInformation;
        this.projectImportResults = projectImportResults;
        this.attachmentStore = attachmentStore;
    }

    public void handleEntity(final String entityName, final Map<String, String> attributes) throws ParseException, AbortImportException
    {
        if (canProcessEntity(entityName, projectImportOptions))
        {
            final ExternalAttachment externalAttachment = attachmentParser.get().parse(attributes);
            if (externalAttachment.getIssueId() != null)
            {
                // Now find out where the actual attachment file is on disk and set this on the externalAttachment
                final String issueKey = backupSystemInformation.getIssueKeyForId(externalAttachment.getIssueId());
                final File attachedFile = attachmentParser.get().getAttachmentFile(externalAttachment, backupProject.getProject(),
                        issueKey);
                externalAttachment.setAttachedFile(attachedFile);

                if (fileExists(attachedFile))
                {
                    // Transform the attachment (i.e. set the mapped issue id)
                    final ExternalAttachment transformedAttachment = getAttachmentTransformer().transform(projectImportMapper, externalAttachment);

                    execute(new Runnable()
                    {
                        public void run()
                        {
                            final Attachment createdAttachment = projectImportPersister.createAttachment(transformedAttachment);
                            // if this doesn't create the attachment for some reason, then this is already logged in DefaultAttachmentManager.
                            if (createdAttachment == null)
                            {
                                projectImportResults.addError(projectImportResults.getI18n().getText("admin.errors.project.import.attachment.error",
                                    externalAttachment.getFileName(), issueKey));
                            }
                            else
                            {
                                projectImportResults.incrementAttachmentsCreatedCount();
                            }
                        }
                    });
                }
                else
                {
                    // The user already knows about this we warned them on the pre-import summary page, it is therefore
                    // not an error.
                    log.warn("Not saving attachment '" + externalAttachment.getFileName() + "' for issue '" + issueKey + "', the file does not exist in the provided attachment directory.");
                }
            }
            else
            {
                log.warn("Not saving attachment '" + externalAttachment.getFileName() + "' it appears that the issue was not created as part of the import.");
            }
        }
    }

    ///CLOVER:OFF
    boolean fileExists(final File attachedFile)
    {
        return attachedFile.exists();
    }

    ///CLOVER:ON

    ///CLOVER:OFF
    AttachmentTransformer getAttachmentTransformer()
    {
        if (attachmentTransformer == null)
        {
            attachmentTransformer = new AttachmentTransformerImpl();
        }
        return attachmentTransformer;
    }

    ///CLOVER:ON

    ///CLOVER:OFF
    public void startDocument()
    {
    // No-op
    }

    ///CLOVER:ON

    ///CLOVER:OFF
    public void endDocument()
    {
    // No-op
    }
    ///CLOVER:ON
}
