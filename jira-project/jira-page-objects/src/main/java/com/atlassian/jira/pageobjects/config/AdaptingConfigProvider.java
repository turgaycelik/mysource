package com.atlassian.jira.pageobjects.config;

import com.atlassian.jira.functest.framework.backdoor.Backdoor;
import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.jira.pageobjects.pages.admin.RestoreDataPage;

import javax.inject.Inject;
import java.io.File;

/**
 * Config provider that uses jira-func-test-plugin if possible, or falls back to parsing JIRA UI.
 *
 * @since v4.4
 */
public class AdaptingConfigProvider implements JiraConfigProvider
{
    private static final String EXPECTED_IMPORT_PATH_SUFFIX = File.separator + "import";

    @Inject
    private JiraTestedProduct jiraProduct;

    @Inject
    private TestkitPluginDetector testkitPluginDetector;

    @Inject
    private Backdoor backdoor;

    private RestConfigProvider restProvider;

    private volatile String jiraHomePath; // this shouldn't change evar!
    private volatile boolean isSetUp; // this also won't change once it turns true

    @Override
    public String jiraHomePath()
    {
        if (jiraHomePath == null)
        {
            jiraHomePath = initJiraHomePath();
        }
        return jiraHomePath;
    }

    private String initJiraHomePath()
    {
        if (testkitPluginDetector.isInstalled())
        {
            return rest().jiraHomePath();
        }
        else
        {
            // simple assumption is that we're AMPS:) so JIRA is setup
            final RestoreDataPage page = jiraProduct.quickLoginAsSysadmin(RestoreDataPage.class);
            final String importPath = page.getDefaultImportPath();
            if (importPath.endsWith(EXPECTED_IMPORT_PATH_SUFFIX))
            {
                return importPath.substring(0, importPath.lastIndexOf(EXPECTED_IMPORT_PATH_SUFFIX));
            }
            else
            {
                throw new RuntimeException("Unrecognized import path '" + importPath + "', expected path ending with '"
                 + EXPECTED_IMPORT_PATH_SUFFIX + "'");
            }
        }
    }

    @Override
    public boolean isSetUp()
    {
        if (isSetUp)
        {
            return true;
        }
        else if (backdoor.dataImport().isSetUp())
        {
            isSetUp = true;
            return true;
        }
        else
        {
            return false;
        }
    }

    private JiraConfigProvider rest()
    {
        if (restProvider == null)
        {
            restProvider = new RestConfigProvider(jiraProduct.getProductInstance());
        }
        return restProvider;
    }
}

