package com.atlassian.jira.webtests.zsuites;

import com.atlassian.jira.functest.framework.FuncTestSuite;
import junit.framework.Test;

/**
 * TODO - A TEMPLATE func test suite.  Use this as a starting point for creating FuncTestSuite's.
 * <p/>
 * TODO - NOTE the name of the suite is FuncTestSuiteXXXX.  This makes it easier to find in IDEA via Ctrl-N
 *
 * @since v4.0
 */
public class FuncTestSuiteXXXX extends FuncTestSuite
{
    /**
     * A static declaration of this particular FuncTestSuite
     */
    public static final FuncTestSuite SUITE = new FuncTestSuiteXXXX(); // TODO <--- replace your class name here

    /**
     * The pattern in JUnit/IDEA JUnit runner is that if a class has a static suite() method that returns a Test, then
     * this is the entry point for running your tests.  So make sure you declare one of these in the FuncTestSuite
     * implementation.
     *
     * @return a Test that can be run by as JUnit TestRunner
     */
    public static Test suite()
    {
        return SUITE.createTest();
    }

    public FuncTestSuiteXXXX()
    {
        /*

        These are your test declaration options : 

        - A xxxxOnly will only run on that edition of JIRA

        - A xxxUpwards will run on that edition of JIRA and upwards
        

        addStandardOnly(TestSomething.class);
        addStandardUpwards(TestSomething.class);

        addProfessionalOnly(TestSomething.class)
        addProfessionalUpwards(TestSomething.class.class);

        addEnterpriseOnly(TestSomething.class)
        
        */
        // TODO <--- insert your Test classes here
    }
}