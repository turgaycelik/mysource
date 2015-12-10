package com.atlassian.jira.imports.project.handler;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalAttachment;
import com.atlassian.jira.external.beans.ExternalProject;
import com.atlassian.jira.imports.project.core.BackupProject;
import com.atlassian.jira.imports.project.core.BackupSystemInformation;
import com.atlassian.jira.imports.project.core.ProjectImportOptions;
import com.atlassian.jira.imports.project.parser.AttachmentParser;
import com.atlassian.jira.imports.project.parser.AttachmentParserImpl;
import com.atlassian.jira.issue.attachment.AttachmentStore;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.Map;

/**
 * This handler inspects attachment entries and if the user is importing attachments will check to see that the
 * attachment file exists for the corresponding database entry.
 *
 * Any attachments that are not found will cause a warning to be generated and placed into the MessageSet.
 *
 * @since v3.13
 */
public class AttachmentFileValidatorHandler implements ImportEntityHandler
{
    private static final Logger log = Logger.getLogger(AttachmentFileValidatorHandler.class);
    private static final int MAX_WARNINGS = 20;

    private MessageSet messageSet;
    private final BackupProject backupProject;
    private final ProjectImportOptions projectImportOptions;
    private final BackupSystemInformation backupSystemInformation;
    private final I18nHelper i18nHelper;
    private final AttachmentStore attachmentStore;
    private String projectAttachmentsSubdir;
    private boolean projectAttachmentDirExists;
    private boolean maxWarningsExceeded;
    private int validAttachmentCount = 0;

    private final Supplier<AttachmentParser> attachmentParser;

    public AttachmentFileValidatorHandler(final BackupProject backupProject, final ProjectImportOptions projectImportOptions,
            final BackupSystemInformation backupSystemInformation, final I18nHelper i18nHelper,
            final AttachmentStore attachmentStore)
    {
        this.backupProject = backupProject;
        this.projectImportOptions = projectImportOptions;
        this.backupSystemInformation = backupSystemInformation;
        this.i18nHelper = i18nHelper;
        this.attachmentStore = attachmentStore;
        this.messageSet = new MessageSetImpl();
        this.maxWarningsExceeded = false;
        this.attachmentParser = Suppliers.memoize(new Supplier<AttachmentParser>()
        {
            @Override
            public AttachmentParser get()
            {
                return createAttachmentParser();
            }
        });
    }

    @VisibleForTesting
    protected AttachmentParser createAttachmentParser()
    {
        return new AttachmentParserImpl(attachmentStore, projectImportOptions.getAttachmentPath());
    }


    public void handleEntity(final String entityName, final Map<String, String> attributes) throws ParseException
    {
        // We only need to validate the attachment files if an attachment path has been provided
        // The service/manager should have short-circuited this handler anyway if there was no path, but lets be defensive.
        if (canProcessEntity(entityName, projectImportOptions))
        {
            Preconditions.checkNotNull(projectAttachmentsSubdir);

            final ExternalAttachment externalAttachment = attachmentParser.get().parse(attributes);
            if ((externalAttachment != null) && backupProject.containsIssue(externalAttachment.getIssueId()))
            {
                if (!projectAttachmentDirExists)
                {
                    getValidationResults().addWarningMessage(
                        i18nHelper.getText("admin.project.import.attachment.project.directory.does.not.exist",
                            projectAttachmentsSubdir));
                    getValidationResults().addWarningMessageInEnglish(
                        "The provided attachment path does not contain a sub-directory called '"
                                + projectAttachmentsSubdir + "'. If you proceed with the import attachments will not be included.");
                }
                else
                {
                    final File attachmentFile = attachmentParser.get().getAttachmentFile(externalAttachment,
                            backupProject.getProject(), backupSystemInformation.getIssueKeyForId(externalAttachment.getIssueId()));
                    if (attachmentFile.exists())
                    {
                        validAttachmentCount++;
                    }
                    else
                    {
                        log.warn("The attachment '" + externalAttachment.getFileName() + "' does not exist at '" + attachmentFile.getAbsolutePath() + "'. It will not be imported.");
                        // We only want to add 20 warnings so as not to clutter the UI
                        if (getValidationResults().getWarningMessages().size() >= MAX_WARNINGS)
                        {
                            maxWarningsExceeded = true;
                            messageSet = new MessageSetImpl();
                            getValidationResults().addWarningMessage(i18nHelper.getText("admin.project.import.attachment.too.many.warnings"));
                        }
                        if (!maxWarningsExceeded)
                        {
                            // JRA-15914 Missing filename in XML file.
                            if (externalAttachment.getFileName() == null || externalAttachment.getFileName().length() == 0)
                            {
                                getValidationResults().addWarningMessage(
                                    i18nHelper.getText("admin.project.import.attachment.missing.filename", externalAttachment.getId(),
                                        attachmentFile.getAbsolutePath()));
                            }
                            else
                            {
                                getValidationResults().addWarningMessage(
                                    i18nHelper.getText("admin.project.import.attachment.does.not.exist", externalAttachment.getFileName(),
                                        attachmentFile.getAbsolutePath()));
                            }
                        }
                    }
                }
            }
        }
    }

    public static boolean canProcessEntity(final String entityName, final ProjectImportOptions projectImportOptions)
    {
        return AttachmentParser.ATTACHMENT_ENTITY_NAME.equals(entityName) && StringUtils.isNotEmpty(projectImportOptions.getAttachmentPath());
    }

    public MessageSet getValidationResults()
    {
        return messageSet;
    }

    public int getValidAttachmentCount()
    {
        return validAttachmentCount;
    }

    public void startDocument()
    {
        // Do a check to see if the project directory exists in the attachment directory
        final String attachmentPath = projectImportOptions.getAttachmentPath();
        if (StringUtils.isNotEmpty(attachmentPath))
        {
            final ExternalProject project = backupProject.getProject();
            if (attachmentParser.get().isUsingOriginalKeyPath(project))
            {
                projectAttachmentsSubdir = project.getOriginalKey();
                projectAttachmentDirExists = new File(attachmentPath, project.getOriginalKey()).exists();
            }
            else
            {
                projectAttachmentsSubdir = project.getKey();
                projectAttachmentDirExists = new File(attachmentPath, project.getKey()).exists();
            }
        }
    }

    ///CLOVER:OFF
    public void endDocument()
    {}

    ///CLOVER:ON

    ///CLOVER:OFF
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass()))
        {
            return false;
        }

        final AttachmentFileValidatorHandler that = (AttachmentFileValidatorHandler) o;

        if (attachmentParser != null ? !attachmentParser.equals(that.attachmentParser) : that.attachmentParser != null)
        {
            return false;
        }
        if (backupProject != null ? !backupProject.equals(that.backupProject) : that.backupProject != null)
        {
            return false;
        }
        if (backupSystemInformation != null ? !backupSystemInformation.equals(that.backupSystemInformation) : that.backupSystemInformation != null)
        {
            return false;
        }
        if (i18nHelper != null ? !i18nHelper.equals(that.i18nHelper) : that.i18nHelper != null)
        {
            return false;
        }
        if (messageSet != null ? !messageSet.equals(that.messageSet) : that.messageSet != null)
        {
            return false;
        }
        if (projectImportOptions != null ? !projectImportOptions.equals(that.projectImportOptions) : that.projectImportOptions != null)
        {
            return false;
        }

        return true;
    }

    ///CLOVER:ON

    ///CLOVER:OFF
    public int hashCode()
    {
        int result;
        result = (messageSet != null ? messageSet.hashCode() : 0);
        result = 31 * result + (backupProject != null ? backupProject.hashCode() : 0);
        result = 31 * result + (projectImportOptions != null ? projectImportOptions.hashCode() : 0);
        result = 31 * result + (backupSystemInformation != null ? backupSystemInformation.hashCode() : 0);
        result = 31 * result + (i18nHelper != null ? i18nHelper.hashCode() : 0);
        result = 31 * result + (attachmentParser != null ? attachmentParser.hashCode() : 0);
        return result;
    }
    ///CLOVER:ON
}
