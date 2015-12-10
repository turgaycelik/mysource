package com.atlassian.jira.imports.project.handler;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalIssue;
import com.atlassian.jira.imports.project.ProjectImportPersister;
import com.atlassian.jira.imports.project.core.ProjectImportResults;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.imports.project.parser.IssueParser;
import com.atlassian.jira.imports.project.parser.IssueParserImpl;
import com.atlassian.jira.imports.project.transformer.IssueTransformer;
import com.atlassian.jira.imports.project.transformer.IssueTransformerImpl;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.util.ImportUtils;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * Used to inspect issue entries in a backup file, transform the entities and persist them to the database.
 *
 * @since v3.13
 */
public class IssuePersisterHandler extends AbstractPersisterHandler implements ImportEntityHandler
{
    private static final Logger log = Logger.getLogger(IssuePersisterHandler.class);

    private final ProjectImportPersister projectImportPersister;
    private final ProjectImportMapper projectImportMapper;
    private final User importAuthor;
    private final ProjectImportResults projectImportResults;
    private final Date importDate;
    private IssueParser issueParser;
    private IssueTransformer issueTransformer;
    private long largestIssueKeyNumber = 0;

    public IssuePersisterHandler(final ProjectImportPersister projectImportPersister, final ProjectImportMapper projectImportMapper, final User importAuthor, final ProjectImportResults projectImportResults, final Executor executor)
    {
        super(executor, projectImportResults);

        this.projectImportPersister = projectImportPersister;
        this.projectImportMapper = projectImportMapper;
        this.importAuthor = importAuthor;
        this.projectImportResults = projectImportResults;
        // We will create change items that mark the issues we have created and we want them all to have a value of
        // the same date.
        importDate = getImportDate();
    }

    /**
     * Will read the issue, transform it, save it, and keep the new mapped value. It also keeps track of the highest
     * seen issue key number.
     *
     * @param entityName the entity that is being processed
     * @param attributes the map containing all the attributes
     * @throws ParseException if something goes wrong interpreting the attributes into an objec
     */
    public void handleEntity(final String entityName, final Map<String, String> attributes) throws ParseException, AbortImportException
    {
        if (IssueParser.ISSUE_ENTITY_NAME.equals(entityName))
        {
            final ExternalIssue oldIssue = getIssueParser().parse(attributes);
            final ExternalIssue transformedIssue = getIssueTransformer().transform(projectImportMapper, oldIssue);

            execute(new Runnable()
            {
                public void run()
                {
                    try
                    {
                        ImportUtils.setSubvertSecurityScheme(true);
                        ImportUtils.setIndexIssues(false);
                        ImportUtils.setEnableNotifications(false);

                        final Issue createdIssue = projectImportPersister.createIssue(transformedIssue, importDate, importAuthor);
                        if (createdIssue != null)
                        {
                            projectImportMapper.getIssueMapper().mapValue(oldIssue.getId(), createdIssue.getId().toString());
                            updateLargestIssueKeyNumber(createdIssue.getKey());
                            // Update the Count of Added Issues.
                            projectImportResults.incrementIssuesCreatedCount();
                        }
                        else
                        {
                            // There was an error creating this issue.
                            projectImportResults.addError(projectImportResults.getI18n().getText(
                                "admin.errors.project.import.could.not.create.issue", oldIssue.getKey()));
                        }
                    }
                    finally
                    {
                        ImportUtils.setSubvertSecurityScheme(false);
                        ImportUtils.setIndexIssues(true);
                        ImportUtils.setEnableNotifications(true);

                    }
                }
            });
        }
    }

    /**
     * While running through all the issues this handler keeps track of what the largest issue key number it has
     * encountered. This will return the largest seen number at the time it is called or 0 if it imported nothing.
     * <p/>
     * The issue key number is the numeric part of the key after the '-' of an issue key (e.g. TST-7, 7 is the
     * issue key number).
     *
     * @return largest seen issue key number
     */
    public long getLargestIssueKeyNumber()
    {
        return largestIssueKeyNumber;
    }

    ///CLOVER:OFF
    IssueTransformer getIssueTransformer()
    {
        if (issueTransformer == null)
        {
            issueTransformer = new IssueTransformerImpl();
        }
        return issueTransformer;
    }

    ///CLOVER:ON

    ///CLOVER:OFF
    IssueParser getIssueParser()
    {
        if (issueParser == null)
        {
            issueParser = new IssueParserImpl();
        }
        return issueParser;
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

    ///CLOVER:OFF
    Date getImportDate()
    {
        return new Date();
    }

    ///CLOVER:ON

    private void updateLargestIssueKeyNumber(final String key)
    {
        // parse the numeric part of the key:
        final int index = key.lastIndexOf('-');
        if (index == -1)
        {
            log.warn("Created an issue with a key with no '-' in it ('" + key + "').");
            return;
        }

        try
        {
            final long keyNumber = Long.parseLong(key.substring(index + 1));
            // This is not really thread safe and should be done atomically, because there are multiple threads calling
            // this in project import,  but if the highest number is missed, it is a self correcting problem.
            if (keyNumber > largestIssueKeyNumber)
            {
                largestIssueKeyNumber = keyNumber;
            }
        }
        catch (final NumberFormatException e)
        {
            log.warn("Unable to extract the numeric part of the Issue key '" + key + "'.");
        }
    }
}
