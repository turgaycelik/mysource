package com.atlassian.jira.util.resourcebundle;

import com.atlassian.gzipfilter.org.apache.commons.lang.builder.ToStringBuilder;
import com.atlassian.jira.util.dbc.Assertions;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This {@link java.util.ResourceBundle} can dynamically read a properties file for keys.  This is reasonable efficient
 * in that it only re-reads the keys if the file modification date has changed.
 * <p/>
 * This is intended to be used ONLY in development so that changes the language files can be seen without restarting
 * JIRA
 *
 * @since v4.1
 */
public class DebuggingResourceBundle extends ResourceBundle
{
    public static final Logger log = Logger.getLogger(DebuggingResourceBundle.class);
    private static final int MAX_BUNDLES_SEARCHED = 3;
    private static final Locale LOCALE_EN_AU = new Locale("en", "AU");


    /**
     * See {@link java.util.ResourceBundle#getBundle(String, java.util.Locale)} for an example of the semantics
     * <p/>
     * * This should be re-implemented using Java 1.6 ResourceBundle.Control objects and hence allow the normal
     * ResourceBundle loading mechanism to be used.  But we arent on Java 6 yet!
     * <p/>
     * http://java.sun.com/javase/6/docs/api/java/util/ResourceBundle.Control.html
     *
     * @param baseName the base name of the reosurce
     * @param locale   the locale to use as a lookup
     *
     * @return a DebuggingResourceBundle or null
     */
    public static DebuggingResourceBundle getDebuggingResourceBundle(String baseName, Locale locale)
    {
        return getBundleImpl(baseName, locale, DebuggingResourceBundle.class.getClassLoader());
    }

    private static DebuggingResourceBundle getBundleImpl(String baseName, Locale locale, ClassLoader classLoader)
    {
        Assertions.notNull("baseName", baseName);
        Assertions.notNull("locale", locale);
        Assertions.notNull("classLoader", classLoader);

        DebuggingResourceBundle resourceBundle = null;
        try
        {
            final List<BundleInfo> bundleInfos = calculateBundleNames(baseName, locale);
            int index = 0;
            for (BundleInfo bundleInfo : bundleInfos)
            {
                resourceBundle = loadBundle(classLoader, bundleInfo);
                if (resourceBundle != null)
                {
                    fixUpParents(resourceBundle, bundleInfos, index + 1, classLoader);
                    break;
                }
                index++;
            }
        }
        catch (MissingResourceException  mre)
        {
            resourceBundle = null;
            log.error("Unable to load debugging resource bundle : " + mre.getMessage(),mre);
        }
        return resourceBundle;
    }

    private static void fixUpParents(final DebuggingResourceBundle resourceBundle, final List<BundleInfo> bundleInfos, final int index, ClassLoader classLoader)
    {
        DebuggingResourceBundle currentBundle = resourceBundle;
        int i = index;
        while (i < bundleInfos.size())
        {
            BundleInfo info = bundleInfos.get(i);
            DebuggingResourceBundle loadedBundledPlugin = loadBundle(classLoader, info);
            if (loadedBundledPlugin != null)
            {
                currentBundle.setParent(loadedBundledPlugin);
                currentBundle = loadedBundledPlugin;
            }
            i++;
        }
    }

    private static DebuggingResourceBundle loadBundle(ClassLoader classLoader, BundleInfo bundledInfo)
    {
        final String propertiesFileName = bundledInfo.bundleName.replace('.', '/') + ".properties";
        final URL resource = classLoader.getResource(propertiesFileName);
        if (resource != null)
        {
            // turn it into a File
            final URI resourceURI;
            try
            {
                resourceURI = resource.toURI();
            }
            catch (URISyntaxException e)
            {
                return null;
            }
            File propertiesFile;
            try
            {
                propertiesFile = new File(resourceURI);
            }
            catch (IllegalArgumentException iae)
            {
                // File will not accept all types of URIs.  For example if the resources
                // are in a JAR (and hence isOpaque() == true) then it will throw
                // IllegalArgumentException 
                return null;
            }
            return new DebuggingResourceBundle(propertiesFile, bundledInfo.locale);
        }
        return null;
    }

    private static List<BundleInfo> calculateBundleNames(String baseName, Locale locale)
    {
        final List<BundleInfo> bundleInfos = new ArrayList<BundleInfo>(MAX_BUNDLES_SEARCHED);
        final String language = locale.getLanguage();
        final int languageLength = language.length();
        final String country = locale.getCountry();
        final int countryLength = country.length();
        final String variant = locale.getVariant();
        final int variantLength = variant.length();

        BundleInfo bundleInfoLang = null;
        BundleInfo bundleInfoLang_Country = null;
        BundleInfo bundleInfoLang_Country_Variant = null;

        if (languageLength + countryLength + variantLength != 0)
        {
            final StringBuilder temp = new StringBuilder(baseName);
            temp.append('_');
            temp.append(language);
            if (languageLength > 0)
            {
                bundleInfoLang = new BundleInfo(temp.toString(), new Locale(language));
            }

            if (countryLength + variantLength != 0)
            {
                temp.append('_');
                temp.append(country);
                if (countryLength > 0)
                {
                    bundleInfoLang_Country = new BundleInfo(temp.toString(), new Locale(language, country));
                }

                if (variantLength != 0)
                {
                    temp.append('_');
                    temp.append(variant);
                    bundleInfoLang_Country_Variant = new BundleInfo(temp.toString(), new Locale(language, country, variant));
                }
            }
        }
        if (bundleInfoLang_Country_Variant != null)
        {
            bundleInfos.add(bundleInfoLang_Country_Variant);
        }
        if (bundleInfoLang_Country != null)
        {
            bundleInfos.add(bundleInfoLang_Country);
        }
        if (bundleInfoLang != null)
        {
            bundleInfos.add(bundleInfoLang);
        }

        // add the default locale as the parent of all other locales
        if (Locale.ROOT.equals(locale) || LOCALE_EN_AU.equals(locale))
        {
            bundleInfos.add(new BundleInfo(baseName, Locale.ROOT));
        }

        return bundleInfos;
    }

    private static class BundleInfo
    {
        private final String bundleName;
        private final Locale locale;

        private BundleInfo(final String bundleName, final Locale locale)
        {
            this.bundleName = bundleName;
            this.locale = locale;
        }
    }


    private AtomicLong lastModified = new AtomicLong(0);
    private AtomicReference<PropertyResourceBundle> propertyResourceBundle = new AtomicReference<PropertyResourceBundle>();
    private final File propertiesFile;
    private final Locale locale;

    public DebuggingResourceBundle(File propertiesFile, final Locale locale)
    {
        Assertions.notNull("propertiesFile", propertiesFile);
        Assertions.notNull("locale", locale);

        this.propertiesFile = propertiesFile;
        this.locale = locale;
        propertyResourceBundle.set(refreshFromDisk());
    }

    private PropertyResourceBundle refreshFromDisk()
    {
        try
        {
            return new PropertyResourceBundle(new FileInputStream(propertiesFile));
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public boolean isStale()
    {
        File checkModified = new File(propertiesFile.getAbsolutePath());
        final long lastModification = checkModified.lastModified();
        final long previousValue = lastModified.getAndSet(lastModification);
        if (previousValue != lastModification)
        {
            propertyResourceBundle.set(refreshFromDisk());
            return true;
        }
        return false;
    }

    protected Object handleGetObject(final String key)
    {
        return propertyResourceBundle.get().handleGetObject(key);
    }

    public Enumeration<String> getKeys()
    {
        return propertyResourceBundle.get().getKeys();
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this)
                .append("locale", locale)
                .append("file", propertiesFile)
                .toString();
    }
}
