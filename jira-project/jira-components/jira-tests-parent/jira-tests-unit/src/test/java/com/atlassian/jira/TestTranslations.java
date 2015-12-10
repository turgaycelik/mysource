package com.atlassian.jira;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import static org.junit.Assert.fail;

/**
 * This is an odd test which runs through the message bundles translations properties files checking if they meet certain criteria that translations
 * should.
 *
 * @since v3.13
 */
public class TestTranslations
{
    /**
     * Class path pattern to find translation files for checking
     */
    private static final String DEFAULT_TRANSLATION_RESOURCES_PATH = "classpath*:com/atlassian/jira/web/action/*.properties";

    /**
     * Class path pattern to find translation files for checking, just for IDEA
     */
    private static final String DEFAULT_TRANSLATION_RESOURCES_PATH_FOR_IDEA = "classpath*:*_*/com/atlassian/jira/web/action/*.properties";

    /**
     * Property key for setting the root directory to find translations files in.
     */
    private static final String ROOT_DIR_SYSTEM_PROPERTY_KEY = "jira.translations.checker.root";
    private static final Logger log = Logger.getLogger(TestTranslations.class);

    /**
     * Tests that the Translations are all Hunky Dory.
     *
     * @throws IOException if there is a problem opening/reading/closing properties files under the root dir.
     */
    @Test
    public void testRunTranslationsChecker() throws IOException
    {

        String rootDirname = System.getProperty(ROOT_DIR_SYSTEM_PROPERTY_KEY);
        if (rootDirname == null) {

            final PathMatchingResourcePatternResolver patternResolver = new PathMatchingResourcePatternResolver();

            String patternUsed = DEFAULT_TRANSLATION_RESOURCES_PATH;
            Resource[] resources = patternResolver.getResources(patternUsed);

            // There will be at least one resource, must be in IDEA
            if(resources.length == 0)
            {
                patternUsed = DEFAULT_TRANSLATION_RESOURCES_PATH_FOR_IDEA;
                resources = patternResolver.getResources(patternUsed);
            }

            log.info("Defaulting to class path resources matching '" + patternUsed
                     + "' choose the root (relative or absolute) with System Property "
                     + ROOT_DIR_SYSTEM_PROPERTY_KEY);

            final StringBuilder problemDescription = new StringBuilder();
            boolean success = true;
            for (final Resource resource : resources)
            {
                final String filename = resource.getFilename();

                // Special exclusion for antartica since it has escape sequences
                if(filename.contains("en_AQ"))
                {
                    continue;
                }
                TranslationsChecker tc = new TranslationsChecker();
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
                fail("There's trouble brewing in the translations files: \n" + problemDescription.toString());
            }

        }
        else
        {
            TranslationsChecker tc = new TranslationsChecker();
            FileFinder finder = new FileFinder(tc);

            finder.checkDir(new File(rootDirname));
            if (!tc.success())
            {
                fail("There's trouble brewing in the translations files: \n" + tc.getProblemsDescription());
            }
        }
    }

}
