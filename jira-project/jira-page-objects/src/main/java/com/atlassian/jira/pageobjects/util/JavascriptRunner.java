package com.atlassian.jira.pageobjects.util;

import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.io.IOUtils;
import org.openqa.selenium.JavascriptExecutor;

/**
 * Class that can be used to execute JS on the browser while running WebDriver tests. It also contains methods to run
 * useful common JS on the browser.
 *
 * @since v6.2
 */
@Singleton
public class JavascriptRunner
{
    private static final String RESET_WEBSUDO = readResource("js/websudo.js");
    private static final String RESET_XSRF = readResource("js/xsrf.js");
    private static final String REMOVE_WORKFLOW_DESIGNER_UNLOAD_HOOKS = readResource("js/remove-workflow-designer-unload-hooks.js");

    @Inject
    private JavascriptExecutor driver;

    /**
     * Wipe out all the XSRF tokens on the page.
     */
    public void destroyAllXsrfTokens()
    {
        execute(RESET_XSRF);
    }

    /**
     * Drop the user's Websudo authentication session.
     */
    public void clearWebSudo()
    {
        execute(RESET_WEBSUDO);
    }

    /**
     * The workflow designer adds unload hooks that can stop people leaving the page while it is doing something.
     * This causes problems in the tests so we just remove these hooks in the tests. Ideally we should be able to wait
     * for the WD to tell us that it is not busy.
     *
     * REF: CAS-580
     */
    public void removeWorkflowDesignerUnloadHooks()
    {
        execute(REMOVE_WORKFLOW_DESIGNER_UNLOAD_HOOKS);
    }

    public void execute(String script)
    {
        driver.executeScript(script);
    }

    private static String readResource(String name)
    {
        final ClassLoader loader = JavascriptRunner.class.getClassLoader();
        final InputStream resourceAsStream = loader.getResourceAsStream(name);
        try
        {
            return IOUtils.toString(resourceAsStream, "utf-8");
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            IOUtils.closeQuietly(resourceAsStream);
        }
    }
}
