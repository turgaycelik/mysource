package com.atlassian.jira.index.ha;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import com.atlassian.jira.config.util.IndexPathManager;
import com.atlassian.jira.issue.index.IndexException;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.service.JiraServiceContainer;
import com.atlassian.jira.service.ServiceManager;
import com.atlassian.jira.task.TaskProgressSink;
import com.atlassian.jira.task.context.Context;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.FileFactory;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.web.action.admin.index.IndexCommandResult;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * Service for recovering an Index.
 *
 * @since v6.1
 */
public class DefaultIndexRecoveryService implements IndexRecoveryService
{
    private static final Logger LOG = Logger.getLogger(DefaultIndexRecoveryService.class);

    private final IndexRecoveryManager indexRecoveryManager;
    private final PermissionManager permissionManager;
    private final IndexPathManager indexPathManager;
    private final ServiceManager serviceManager;
    private final FileFactory fileFactory;


    public DefaultIndexRecoveryService(final IndexRecoveryManager indexRecoveryManager, final PermissionManager permissionManager, final IndexPathManager indexPathManager, final ServiceManager serviceManager, final FileFactory fileFactory)
    {
        this.indexRecoveryManager = indexRecoveryManager;
        this.permissionManager = permissionManager;
        this.indexPathManager = indexPathManager;
        this.serviceManager = serviceManager;
        this.fileFactory = fileFactory;
    }

    @Override
    public IndexCommandResult recoverIndexFromBackup(final ApplicationUser user, final Context context, final I18nHelper i18n, final String recoveryFilename, final TaskProgressSink taskProgressSink)
            throws IndexException
    {
        ErrorCollection errorCollection = new SimpleErrorCollection();
        if (!permissionManager.hasPermission(Permissions.SYSTEM_ADMIN, user))
        {
            errorCollection.addErrorMessage(i18n.getText("admin.errors.index.recovery.unauthorised"));
        }
        File recoveryFile = validateRecoveryFile(user, errorCollection, i18n, recoveryFilename);
        if (errorCollection.hasAnyErrors())
        {
            return new IndexCommandResult(errorCollection);
        }

        return indexRecoveryManager.recoverIndexFromBackup(recoveryFile, taskProgressSink);
    }

    private File validateRecoveryFile(final ApplicationUser user, final ErrorCollection errorCollection, final I18nHelper i18n, final String path)
    {
        if (StringUtils.isBlank(path))
        {
            errorCollection.addError("recoveryFilename", i18n.getText("admin.index.recovery.file.error.not.specified"));
            return null;
        }

        final File actualFile = fileFactory.getFile(path);

        if (!actualFile.exists())
        {
            errorCollection.addError("recoveryFilename", i18n.getText("admin.index.recovery.file.error.not.found"));
        }
        else if (actualFile.isDirectory())
        {
            errorCollection.addError("recoveryFilename", i18n.getText("admin.index.recovery.file.error.not.zip"));
        }
        else if (!actualFile.canRead())
        {
            errorCollection.addError("indexPath", i18n.getText("admin.errors.path.entered.is.not.readable"));
        }
        else if (!validIndexZipFile(user, actualFile))
        {
            errorCollection.addError("recoveryFilename", i18n.getText("admin.index.recovery.file.error.not.zip"));
        }

        return actualFile;
    }

    @Override
    public boolean validIndexZipFile(final ApplicationUser user, final File zipFile)
    {
        // We want a zip file with an index for issues, comments and change history.
        final String segmentFileName = "segments.gen";
        final Map<String, Boolean> requiredEntries = new HashMap<String, Boolean>();
        int basePathLength = indexPathManager.getIndexRootPath().length();
        requiredEntries.put(indexPathManager.getIssueIndexPath().substring(basePathLength + 1) + "/" + segmentFileName, Boolean.FALSE);
        requiredEntries.put(indexPathManager.getCommentIndexPath().substring(basePathLength + 1) + "/" + segmentFileName, Boolean.FALSE);
        requiredEntries.put(indexPathManager.getChangeHistoryIndexPath().substring(basePathLength + 1) + "/" + segmentFileName, Boolean.FALSE);

        try
        {
            final ZipFile file = new ZipFile(zipFile.getAbsolutePath());
            try
            {
                Enumeration<ZipArchiveEntry> entries = file.getEntries();
                while (entries.hasMoreElements())
                {
                    ZipArchiveEntry entry = entries.nextElement();
                    if (requiredEntries.containsKey(entry.getName()))
                    {
                        requiredEntries.put(entry.getName(), Boolean.TRUE);
                    }
                }
                for (Boolean found : requiredEntries.values())
                {
                    if (!found)
                    {
                        return false;
                    }
                }
                return true;
            }
            finally
            {
                ZipFile.closeQuietly(file);
            }
        }
        catch (IOException e)
        {
            LOG.debug("Can't access zip file '" + zipFile.getPath() + "'");
            return false;
        }
    }

    @Override
    public int size()
    {
        return indexRecoveryManager.size();
    }

    @Override
    public boolean isEmpty()
    {
        return indexRecoveryManager.isEmpty();
    }

    @Override
    public void updateRecoverySettings(final ApplicationUser user, final boolean recoveryEnabled, final long snapshotInterval)
            throws Exception
    {
        JiraServiceContainer service = serviceManager.getServiceWithName(IndexSnapshotService.getServiceName());
        if (permissionManager.hasPermission(Permissions.SYSTEM_ADMIN, user))
        {
            if (recoveryEnabled)
            {
                if (service == null)
                {
                    serviceManager.addService(IndexSnapshotService.getServiceName(), IndexSnapshotService.class, snapshotInterval);
                }
                else
                {
                    serviceManager.editService(service.getId(), snapshotInterval, Collections.<String, String[]>emptyMap());
                }
            }
            else
            {
                if (service != null)
                {
                    serviceManager.removeService(service.getId());
                }
            }
        }
    }

    @Override
    public boolean isRecoveryEnabled(final ApplicationUser user)
    {
        try
        {
            return serviceManager.getServiceWithName(IndexSnapshotService.getServiceName()) != null;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    @Override
    public Long getSnapshotInterval(final ApplicationUser user)
    {
        try
        {
            final JiraServiceContainer service = serviceManager.getServiceWithName(IndexSnapshotService.getServiceName());
            if (service != null)
            {
                return service.getDelay();
            }
            return null;
        }
        catch (Exception e)
        {
            return null;
        }
    }
}
