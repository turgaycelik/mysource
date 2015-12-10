package com.atlassian.jira.webtests.cargo;

import com.atlassian.jira.webtests.CategorisingTestSuite;
import junit.framework.Test;
import junit.framework.TestSuite;

import java.io.IOException;

/**
 * {@link com.atlassian.jira.webtests.CategorisingTestSuite} with support for cargo. Ouch :P
 *
 * @since v4.4
 */
public class CargoCategorisingTestSuite extends TestSuite
{
    public static Test suite() throws IOException
    {
        return CargoTestHarness.suite(CategorisingTestSuite.class);
    }

}
