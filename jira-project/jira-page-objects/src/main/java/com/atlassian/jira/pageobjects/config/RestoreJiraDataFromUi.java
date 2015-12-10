package com.atlassian.jira.pageobjects.config;

import com.atlassian.jira.functest.framework.backdoor.Backdoor;
import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.jira.pageobjects.pages.admin.RestoreDataPage;
import com.atlassian.jira.testkit.client.RestoreDataResources;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.hamcrest.StringDescription;

import javax.inject.Inject;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * <p>
 * Implementation of {@link RestoreJiraData} that uses UI.
 *
 * <p>
 * Web-sudo needs to be disabled for this to work.
 *
 * @since v4.4
 */
public class RestoreJiraDataFromUi extends AbstractRestoreJiraData implements RestoreJiraData
{
    private final JiraTestedProduct product;
    private final JiraConfigProvider configProvider;
    private static final Logger logger = Logger.getLogger(RestoreJiraDataFromUi.class);


    @Inject public RestoreJiraDataFromUi(final JiraTestedProduct product, final JiraConfigProvider configProvider)
    {
        this.product = notNull(product);
        this.configProvider = configProvider;
    }

    public void restore(final String resourcePath)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug(String.format("Restoring '%s' via JIRA's UI", resourcePath));
        }
        final String name = prepareImportFile(resourcePath);
        final RestoreDataPage restoreDataPage = product.quickLoginAsSysadmin(RestoreDataPage.class);
        restoreDataPage
                .setFileName(name)
                .setQuickImport(true)
                .submitRestore()
                .waitForRestoreCompleted();
    }

    private String prepareImportFile(final String resourcePath)
    {
        final String importFileName = importFileNameFor(notNull(resourcePath));
        final InputStream resourceStream = RestoreDataResources.getResourceAsStream(resourcePath);
        final String targetPath = jiraImportPath() + "/" + importFileName;
        OutputStream targetStream = null;
        try
        {
            targetStream = new FileOutputStream(targetPath);
            IOUtils.copy(resourceStream, targetStream);
        }
        catch (IOException ioe)
        {
            throw new RuntimeException(new StringDescription()
                    .appendText("Error while trying to restore JIRA data from resource ").appendValue(resourcePath)
                    .toString(), ioe);
        }
        finally
        {
            IOUtils.closeQuietly(resourceStream);
            IOUtils.closeQuietly(targetStream);
        }
        return importFileName;
    }

    private String importFileNameFor(final String resourcePath)
    {
        final String extension = FilenameUtils.getExtension(resourcePath);
        return extension != null ? resourcePath.hashCode() + "." + extension : resourcePath.hashCode() + ".xml";
    }

    private String jiraImportPath()
    {
        return configProvider.jiraHomePath() + "/import";
    }
}
