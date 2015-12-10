package com.atlassian.jira.webtests;

import com.atlassian.jira.testkit.client.log.FuncTestOut;
import com.atlassian.jira.functest.framework.TestSuiteBuilder;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.FunctionalCategoryComparator;
import com.atlassian.jira.functest.framework.suite.SystemPropertyBasedSuite;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.util.TestClassUtils;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * Test suite that runs given set of tests based on provided test package name property and
 * included and excluded categories.
 *
 * <p>
 * This basically mimics behaviour of {@link com.atlassian.jira.functest.framework.suite.SystemPropertyBasedSuite}, but
 * works with the old JUnit3-based test infrastructure.
 *
 * @since v4.3
 */
public class CategorisingTestSuite implements Test
{
    public static final CategorisingTestSuite SUITE = new CategorisingTestSuite();

    public static Test suite()
    {
        return SUITE.createTest();
    }

    private static final String NUMBER_OF_BATCHES_PROPERTY_NAME = "atlassian.test.suite.numbatches";
    private static final String BATCH_NO_PROPERTY_NAME = "atlassian.test.suite.batch";
    private static final String PARALLEL_PROPERTY_NAME = "atlassian.test.suite.parallel";

    private static TestSuiteBuilder createFuncTestBuilder()
    {
        final String b = System.getProperty(NUMBER_OF_BATCHES_PROPERTY_NAME);
        final String n = System.getProperty(BATCH_NO_PROPERTY_NAME);
        final boolean parallel = Boolean.getBoolean(PARALLEL_PROPERTY_NAME);
        if (parallel && b != null)
        {
            int numBatches = Integer.parseInt(b);
            return new TestSuiteBuilder().maxBatch(numBatches).parallel(true).log(true);
        }
        if (b != null && n != null)
        {
            String batchInfo = "Batch " + n + " of " + b;
            try
            {
                int numBatches = Integer.parseInt(b);
                int batch = Integer.parseInt(n);
                if (batch > 0 && batch <= numBatches)
                {
                    FuncTestOut.out.println(batchInfo);
                    return new TestSuiteBuilder(batch, numBatches).log(true);
                }
                else
                {
                    FuncTestOut.out.println("Batch mode FAIL. Batch information looks wrong-arse: " + batchInfo);
                }
            }
            catch (NumberFormatException e)
            {
                FuncTestOut.err.println("Batch mode FAIL. Batch information cannot be properly interpreted: " + batchInfo);
                e.printStackTrace(FuncTestOut.err);
                // will fall back to unbatched mode
            }
        }
        return new TestSuiteBuilder().log(true);
    }

    private final SystemPropertyBasedSuite realSuite = new SystemPropertyBasedSuite();


    public int countTestCases()
    {
        return createTest().countTestCases();
    }

    public void run(final TestResult result)
    {
        createTest().run(result);
    }

    private Test createTest()
    {
        final Collection<Class<? extends TestCase>> tests = getTests();
        return createFuncTestBuilder().addTests(tests).build();
    }

    private Collection<Class<? extends TestCase>> getTests()
    {
        List<Class<? extends TestCase>> allTestsInPackage = TestClassUtils.getJUni3TestClasses(realSuite.webTestPackage(), true);
        // this should really be done in TestSuiteBuilder to allow for per-method filtering
        // but given this is a workaround and we will hopefully move to JUnit4 and WebTestSuite
        // it should be good enough
        List<Class<? extends TestCase>> filtered = Lists.newArrayList(Collections2.filter(allTestsInPackage,
                new CategoryMatchPredicate(realSuite.includes(), realSuite.excludes())));
        Collections.sort(filtered, FunctionalCategoryComparator.INSTANCE);
        return new LinkedHashSet<Class<? extends TestCase>>(filtered);
    }


    public static class CategoryMatchPredicate implements Predicate<Class<? extends TestCase>>
    {
        private final Set<Category> included;
        private final Set<Category> excluded;

            public CategoryMatchPredicate(Set<Category> includedCategories, Set<Category> excludedCategories)
        {
            included = includedCategories.size() > 0 ? EnumSet.copyOf(includedCategories) : EnumSet.noneOf(Category.class);
            excluded = excludedCategories.size() > 0 ? EnumSet.copyOf(excludedCategories) : EnumSet.noneOf(Category.class);
        }

        @Override
        public boolean apply(@Nullable Class<? extends TestCase> input)
        {
            return hasCorrectCategoryAnnotation(input);
        }

        private boolean hasCorrectCategoryAnnotation(Class<? extends TestCase> testClass)
        {
            Set<Category> categories = categories(testClass);
            if (categories.isEmpty())
            {
                return included == null;
            }
            for (Category each : categories)
            {
                if (excluded != null && excluded.contains(each))
                {
                    return false;
                }
            }
            for (Category each : categories)
            {
                if (included == null || included.contains(each))
                {
                    return true;
                }
            }
            return false;
        }

        private Set<Category> categories(Class<? extends TestCase> testClass)
        {
            WebTest annotation = testClass.getAnnotation(WebTest.class);
            if (annotation == null || annotation.value().length == 0)
            {
                return EnumSet.noneOf(Category.class);
            }
            return EnumSet.copyOf(Arrays.asList(annotation.value()));
        }
    }
}
