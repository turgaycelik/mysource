package com.atlassian.jira.util.resourcebundle;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;

import com.atlassian.jira.web.action.JiraWebActionSupport;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TestAmericanTranslation
{
    private static final Logger log = Logger.getLogger(TestAmericanTranslation.class);

    public static final String BUNDLE_PATH = "/com/atlassian/jira/web/action/JiraWebActionSupport_en_US.properties";
    public static final String BUNDLE_PATH_FOR_IDEA = "/en_US/com/atlassian/jira/web/action/JiraWebActionSupport_en_US.properties";
    private static final String DELIM = " \t\n\r\f,.:;()[]{}'\"";
    private ResourceBundle defaultBundle;
    private Properties americanBundle;
    private Map<String, String> usToBritish;
    private Map<String, String> britishToUs;

    @Before
    public void setUp() throws Exception
    {
        defaultBundle = ResourceBundle.getBundle(JiraWebActionSupport.class.getName(), new Locale(""));
        americanBundle = new Properties();

        InputStream resourceAsStream = TestAmericanTranslation.class.getResourceAsStream(BUNDLE_PATH);
        if(resourceAsStream == null)
        {
            resourceAsStream = TestAmericanTranslation.class.getResourceAsStream(BUNDLE_PATH_FOR_IDEA);
        }
        americanBundle.load(resourceAsStream);

        @SuppressWarnings ("unchecked")
        final List<String> dict = FileUtils.readLines(new File(TestAmericanTranslation.class.getResource("/ustobritish.txt").getFile()));
        usToBritish = new HashMap<String, String>(dict.size());
        britishToUs = new HashMap<String, String>(dict.size());
        for (String s : dict)
        {
            final String[] strings = s.split(",");
            usToBritish.put(strings[0].trim(), strings[1].trim());
            britishToUs.put(strings[1].trim(), strings[0].trim());
        }
    }

    /**
     * Checks that the american translation only contains entries that need to be translated according to the
     * ustobritish dictionary.  It also compares the US entry to the default entry to ensure that the text hasn't
     * changed in the default entry!
     * <p/>
     * This test will also fail if there's any unused keys in the american translation, or if the american translation
     * contains keys without any translated words!
     *
     * @throws IOException If there's an error reading files.
     */
    @Test
    public void testAmericanTranslations() throws IOException
    {
        final Set<String> keysUnused = new LinkedHashSet<String>();
        final Set<String> keysToTranslate = new LinkedHashSet<String>();
        for (Object keyObj : americanBundle.keySet())
        {
            final String key = (String) keyObj;
            try
            {
                final String defaultTranslation = defaultBundle.getString(key);
                final String americanTranslation = americanBundle.getProperty(key);
                final StringTokenizer usSt = new StringTokenizer(americanTranslation.toLowerCase(), DELIM);
                final StringTokenizer defaultSt = new StringTokenizer(defaultTranslation.toLowerCase(), DELIM);
                final StringBuilder usBuilder = new StringBuilder();
                final StringBuilder defaultBuilder = new StringBuilder();
                boolean replaced = false;
                //break the US translation down into individual words and see if there's any british translations.
                //if there is swap the US words out with their british equivalent and compare to the
                //british version of the translation.
                while (usSt.hasMoreTokens())
                {
                    final String usword = usSt.nextToken();
                    if (usToBritish.containsKey(usword))
                    {
                        usBuilder.append(usToBritish.get(usword));
                        replaced = true;
                    }
                    else
                    {
                        usBuilder.append(usword);
                    }
                }
                while (defaultSt.hasMoreTokens())
                {
                    defaultBuilder.append(defaultSt.nextToken());
                }

                if (replaced)
                {
                    assertEquals("American translation for key '" + key + "' seems incorrect '" + americanTranslation + "'",
                            defaultBuilder.toString(), usBuilder.toString());
                }
                else
                {
                    //looks like there was a US translation but no words could be swapped.  Either the dictionary
                    //we got is wrong or this translation doesn't need to exist in the US language pack.
                    keysToTranslate.add(key);
                }
            }
            catch (MissingResourceException e)
            {
                keysUnused.add(key);
            }
            log.info("Key '" + key + " is ok!");
        }

        if (!keysUnused.isEmpty())
        {
            //looks like we found some US translations that no longer exist in the default language pack.  should
            //probably be removed here.
            fail("American translation contains " + keysUnused.size() + " unused keys :" + keysUnused);
        }
        if (!keysToTranslate.isEmpty())
        {
            // The american translation contained some keys that don't have anything to swap out.  Probably need to
            //be translated or removed still!
            fail("American translation contains " + keysToTranslate.size() + " translations without any american words.  Either translate these entries or remove them (if not needed):" + keysToTranslate);
        }
    }

    /**
     * Checks if there's anything in the default language pack that looks like it needs to be americanized.
     */
    @Test
    public void testDefaultTranslationsMissingInAmerican()
    {
        final List<SuggestedTranslation> missingTranslations = new ArrayList<SuggestedTranslation>();
        for (Enumeration<String> keysIt = defaultBundle.getKeys(); keysIt.hasMoreElements();)
        {
            String key = keysIt.nextElement();

            final String translation = defaultBundle.getString(key);
            final StringTokenizer defaultSt = new StringTokenizer(translation.toLowerCase(), DELIM);
            while (defaultSt.hasMoreTokens())
            {
                final String word = defaultSt.nextToken();
                if (britishToUs.containsKey(word) && !americanBundle.containsKey(key))
                {

                    missingTranslations.add(new SuggestedTranslation(key, translation, britishToUs.get(word)));
                }
            }
        }
        if (!missingTranslations.isEmpty())
        {
            fail("There appear to be " + missingTranslations.size() + " translations in the default translation that should be americanized: " + missingTranslations);
        }
    }

    static class SuggestedTranslation
    {
        private String key;
        private String oldTranslation;
        private String newTranslation;

        SuggestedTranslation(final String key, final String oldTranslation, final String newTranslation)
        {
            this.key = key;
            this.oldTranslation = oldTranslation;
            this.newTranslation = newTranslation;
        }

        @Override
        public String toString()
        {
            return "\n" + key + "=" + oldTranslation + " [" + newTranslation + "]";
        }
    }
}
