package com.atlassian.jira;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import static org.junit.Assert.fail;

/**
 * This is an test which runs through startup database looking for certain errors.
 *
 * @since v6.1
 */
public class TestStartupDatabaseXml
{
    /**
     * Class path pattern to find translation files for checking
     */
    private static final String STARTUP_DATABASE_XML = "classpath*:/startupdatabase.xml";

    /**
     * Class path pattern to find translation files for checking, just for IDEA
     */
    private static final String STARTUP_DATABASE_XML_FOR_IDEA = "classpath*:*_*/startupdatabase.xml";

    /**
     * Tests the startup database XML
     *
     * @throws java.io.IOException if there is a problem opening/reading/closing properties files under the root dir.
     */
    @Test
    public void testRunStartupDataChecker() throws IOException
    {
        final PathMatchingResourcePatternResolver patternResolver = new PathMatchingResourcePatternResolver();

        // Find the resource.  For now we only expect one, but more may be added
        String patternUsed = STARTUP_DATABASE_XML;
        Resource[] resources = patternResolver.getResources(patternUsed);

        // There will be at least one resource, must be in IDEA
        if(resources.length == 0)
        {
            patternUsed = STARTUP_DATABASE_XML_FOR_IDEA;
            resources = patternResolver.getResources(patternUsed);
        }

        final StringBuilder problemDescription = new StringBuilder();
        boolean success = true;
        for (final Resource resource : resources)
        {
            final String filename = resource.getFilename();
            LicenseChecker tc = new LicenseChecker();
            tc.checkFile(resource.getInputStream(), filename);
            if (!tc.success())
            {
                problemDescription.append(tc.getProblemsDescription());
                problemDescription.append('\n');
                success = false;
            }
        }
        if(!success)
        {
            fail("The Startup database file is bad: \n" + problemDescription.toString());
        }
    }

    /**
     * Tests regex really works to find what we are looking for
     *
     * @throws java.io.IOException if there is a problem opening/reading/closing properties files under the root dir.
     */
    @Test
    public void testSelf() throws IOException
    {
        final PathMatchingResourcePatternResolver patternResolver = new PathMatchingResourcePatternResolver();

        // Find the resource.  For now we only expect one, but more may be added
        String badString = "\n\n  <OSPropertyEntry id=\"10021\" entityName=\"jira.properties\" entityId=\"1\" propertyKey=\"License20\" type=\"6\"/> \n\n";

        final String filename = "Self test";
        LicenseChecker tc = new LicenseChecker();
        tc.checkFile(new StringReader(badString), filename);
        if (tc.success())
        {
            fail("The Startup database checker is broken. \nIt should report the badString as being bad. Check the regex expression.");
        }
    }

    class LicenseChecker
    {
        private boolean ok = true;
        private Pattern regex = Pattern.compile(".*entityName=\"jira.properties\".*propertyKey=\"License20\".*");
        private final List<String> problems = new ArrayList<String>();

        public void checkFile(final InputStream inputStream, final String fileName) throws IOException
        {
            doLineChecks(new BufferedReader(new InputStreamReader(inputStream)));
        }

        public void checkFile(final Reader reader, final String fileName) throws IOException
        {
            doLineChecks(new BufferedReader(reader));
        }

        void doLineChecks(final BufferedReader br) throws IOException
        {
            String line;
            int lineNumber = 0;
            while ((line = br.readLine()) != null)
            {
                lineNumber++;
                if (regex.matcher(line).matches())
                {
                    ok = false;
                    problems.add("File contains a license entry at line " + lineNumber);
                }
            }
        }

        public boolean success()
        {
            return ok;
        }

        public String getProblemsDescription()
        {
            StringBuilder problemDescription = new StringBuilder();
            for (String problem : problems)
            {
                problemDescription.append(problem).append("\n");
            }
            return problemDescription.toString();
        }
    }

}
