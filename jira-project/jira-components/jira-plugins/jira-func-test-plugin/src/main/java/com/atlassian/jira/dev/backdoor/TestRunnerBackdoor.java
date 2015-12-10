package com.atlassian.jira.dev.backdoor;

import com.atlassian.functest.junit.SpringAwareJUnit4ClassRunner;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.plugin.ComponentClassManager;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import org.apache.log4j.Logger;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * This is a backdoor for executing integration tests on the server remotely (via a Web Func Test).
 *
 * @since v5.0.1
 * @author mtokar
 */
@Path ("testRunner")
@Consumes ({ MediaType.APPLICATION_JSON })
@Produces ({ MediaType.APPLICATION_JSON })
public class TestRunnerBackdoor
{
    private static final Logger log = Logger.getLogger(TestRunnerBackdoor.class);

    @Autowired
    private ApplicationContext applicationContext;

    private ComponentClassManager componentClassManager;

    public TestRunnerBackdoor()
    {
        this.componentClassManager = ComponentManager.getComponentInstanceOfType(ComponentClassManager.class);
    }

    @GET
    @AnonymousAllowed
    @Path ("run")
    public Response runTests(@QueryParam ("testClasses") List<String> testClasses)
    {
        initialiseRunner();
        Class<?>[] classesToRun = getTestClasses(testClasses);

        JUnitCore core = new JUnitCore();
        Result run = core.run(classesToRun);
        return Response.ok(TestResultResponse.from(run)).build();
    }

    private void initialiseRunner()
    {
        SpringAwareJUnit4ClassRunner.setApplicationContext(applicationContext);
    }

    private Class<?>[] getTestClasses(List<String> testClasses)
    {
        List<Class<?>> clazzes = new ArrayList<Class<?>>();
        if (testClasses != null && !testClasses.isEmpty())
        {
            for (String testClass : testClasses)
            {
                try
                {
                    Class<Object> clazz = componentClassManager.loadClass(testClass);
                    clazzes.add(clazz);
                    log.info("Loaded class '" + testClass + "'");
                }
                catch (ClassNotFoundException e)
                {
                    log.error("Could not load class '" + testClass + "'... skipping");
                }
            }
        }
        else
        {
            log.error("No classes were specified -- nothing to run");
        }
        return clazzes.toArray(new Class<?>[clazzes.size()]);
    }

    public static class TestResultResponse
    {
        public boolean passed = false;
        public boolean failed = false;
        public String message = "";

        public static TestResultResponse from(Result result)
        {
            TestResultResponse testResultResponse = new TestResultResponse();
            testResultResponse.failed = !result.wasSuccessful();
            testResultResponse.passed = result.wasSuccessful();
            if (testResultResponse.failed)
            {
                testResultResponse.message = result.getFailures().toString();
            }
            return testResultResponse;
        }

    }
}
