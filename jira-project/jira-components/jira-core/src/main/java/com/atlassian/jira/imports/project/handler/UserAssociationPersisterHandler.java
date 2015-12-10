package com.atlassian.jira.imports.project.handler;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalVoter;
import com.atlassian.jira.external.beans.ExternalWatcher;
import com.atlassian.jira.imports.project.ProjectImportPersister;
import com.atlassian.jira.imports.project.core.BackupSystemInformation;
import com.atlassian.jira.imports.project.core.ProjectImportResults;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.imports.project.parser.UserAssociationParser;
import com.atlassian.jira.imports.project.parser.UserAssociationParserImpl;
import com.atlassian.jira.imports.project.transformer.VoterTransformer;
import com.atlassian.jira.imports.project.transformer.VoterTransformerImpl;
import com.atlassian.jira.imports.project.transformer.WatcherTransformer;
import com.atlassian.jira.imports.project.transformer.WatcherTransformerImpl;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.concurrent.Executor;

/**
 * Reads, transforms, and stores all user association entities (voters and watchers) from a backup file.
 *
 * It is assumed that all version data that is processed by this handler is relevant and should be saved.
 *
 * @since v3.13
 */
public class UserAssociationPersisterHandler extends AbstractPersisterHandler implements ImportEntityHandler
{
    private static final Logger log = Logger.getLogger(UserAssociationPersisterHandler.class);

    private WatcherTransformer watcherTransformer;
    private VoterTransformer voterTransformer;
    private UserAssociationParser userAssociationParser;
    private final ProjectImportMapper projectImportMapper;
    private final ProjectImportPersister projectImportPersister;
    private final ProjectImportResults projectImportResults;
    private final BackupSystemInformation backupSystemInformation;

    public UserAssociationPersisterHandler(final ProjectImportPersister projectImportPersister, final ProjectImportMapper projectImportMapper, final ProjectImportResults projectImportResults, final BackupSystemInformation backupSystemInformation, final Executor executor)
    {
        super(executor, projectImportResults);

        this.projectImportMapper = projectImportMapper;
        this.projectImportPersister = projectImportPersister;
        this.projectImportResults = projectImportResults;
        this.backupSystemInformation = backupSystemInformation;
    }

    public void handleEntity(final String entityName, final Map<String, String> attributes) throws ParseException, AbortImportException
    {
        if (UserAssociationParser.USER_ASSOCIATION_ENTITY_NAME.equals(entityName))
        {
            // See if this UserAssociation can be resolved into a voter
            final ExternalVoter externalVoter = getUserAssociationParser().parseVoter(attributes);
            if (externalVoter != null)
            {
                saveVoter(externalVoter);
            }

            // See if this UserAssociation can be resolved into a watcher
            final ExternalWatcher externalWatcher = getUserAssociationParser().parseWatcher(attributes);
            if (externalWatcher != null)
            {
                saveWatcher(externalWatcher);
            }
        }
    }

    void saveWatcher(final ExternalWatcher externalWatcher) throws AbortImportException
    {
        final ExternalWatcher transformedWatcher = getWatcherTransformer().transform(projectImportMapper, externalWatcher);
        if (transformedWatcher.getIssueId() != null)
        {
            execute(new Runnable()
            {
                public void run()
                {
                    if (!projectImportPersister.createWatcher(transformedWatcher))
                    {
                        final String issueKey = backupSystemInformation.getIssueKeyForId(externalWatcher.getIssueId());
                        projectImportResults.addError(projectImportResults.getI18n().getText("admin.errors.project.import.watcher.error",
                            externalWatcher.getWatcher(), issueKey));
                    }
                }
            });
        }
        else
        {
            final String issueKey = backupSystemInformation.getIssueKeyForId(externalWatcher.getIssueId());
            log.warn("No watcher association for watcher '" + externalWatcher.getWatcher() + "' for issue '" + issueKey + "', the issue has not been mapped into the current instance of JIRA.");
        }
    }

    void saveVoter(final ExternalVoter externalVoter) throws AbortImportException
    {
        final ExternalVoter transformedVoter = getVoterTransformer().transform(projectImportMapper, externalVoter);
        if (transformedVoter.getIssueId() != null)
        {
            execute(new Runnable()
            {
                public void run()
                {
                    if (!projectImportPersister.createVoter(transformedVoter))
                    {
                        final String issueKey = backupSystemInformation.getIssueKeyForId(externalVoter.getIssueId());
                        projectImportResults.addError(projectImportResults.getI18n().getText("admin.errors.project.import.voter.error",
                            externalVoter.getVoter(), issueKey));
                    }
                }
            });
        }
        else
        {
            final String issueKey = backupSystemInformation.getIssueKeyForId(externalVoter.getIssueId());
            log.warn("No voter association for voter '" + externalVoter.getVoter() + "' for issue '" + issueKey + "', the issue has not been mapped into the current instance of JIRA.");
        }
    }

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

    ///CLOVER:OFF
    WatcherTransformer getWatcherTransformer()
    {
        if (watcherTransformer == null)
        {
            watcherTransformer = new WatcherTransformerImpl();
        }
        return watcherTransformer;
    }

    ///CLOVER:ON

    ///CLOVER:OFF
    VoterTransformer getVoterTransformer()
    {
        if (voterTransformer == null)
        {
            voterTransformer = new VoterTransformerImpl();
        }
        return voterTransformer;
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
