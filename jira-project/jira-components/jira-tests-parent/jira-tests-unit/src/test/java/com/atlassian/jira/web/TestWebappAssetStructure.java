package com.atlassian.jira.web;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.jira.matchers.FileMatchers;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hamcrest.Matchers;
import org.junit.Test;

import static com.atlassian.jira.matchers.FileMatchers.named;
import static org.hamcrest.CoreMatchers.allOf;
import static org.junit.Assert.assertThat;
import static org.junit.internal.matchers.IsCollectionContaining.hasItem;
import static org.junit.internal.matchers.IsCollectionContaining.hasItems;

/**
 * Checks to ensure the asset structure is adhered to
 * @see {@literal JDEV-22790}
 * @see {@literal JDEV-22685}
 *
 * @since v6.2
 */
public class TestWebappAssetStructure
{
    private static Logger logger = Logger.getLogger(TestWebappAssetStructure.class);

    private static final String ASSET_ROOT = "static";
    private static final IOFileFilter WHITELISTED = FileFilterUtils.nameFileFilter(".gitkeep");
    private static final FileFilter DIR = FileFilterUtils.directoryFileFilter();
    private static final FileFilter FILE = FileFilterUtils.andFileFilter(FileFilterUtils.fileFileFilter(), FileFilterUtils.notFileFilter(WHITELISTED));

    private static final FileFilter withExtension(String... extension)
    {
        return extensionCheck(Arrays.asList(extension), true);
    }

    private static final FileFilter withoutExtension(String... extension)
    {
        return extensionCheck(Arrays.asList(extension), false);
    }

    private static final FileFilter extensionCheck(final Collection<String> extensions, final boolean inWhitelist)
    {
        return new FileFilter()
        {
            @Override
            public boolean accept(final File pathname)
            {
                final String name;
                return FILE.accept(pathname)
                        && (name = pathname.getName()) != null
                        && (extensions.contains(FilenameUtils.getExtension(name))) == inWhitelist;
            }
        };
    }

    private static final List<File> getFilesRecursive(final File basedir, final FileFilter filter)
    {
        List<File> files = new ArrayList<File>();

        if ( basedir != null && basedir.isDirectory() )
        {
            for (File subdir : basedir.listFiles(DIR))
            {
                files.addAll( getFilesRecursive(subdir, filter) );
            }

            files.addAll( Arrays.asList(basedir.listFiles(filter)) );
        }

        return files;
    }

    private static File getWebappRoot()
    {
        return getWebappRoot("");
    }
    private static File getWebappRoot(String... loc)
    {
        String path = "jira-components/jira-webapp/src/main/webapp";

        final String wd = new File(System.getProperty("user.dir")).getAbsolutePath();
        if (wd.contains("jira-components"))
        {
            int end = wd.indexOf("jira-components");
            path = FilenameUtils.concat(wd.substring(0,end), path);
        }

        final String relPath = FilenameUtils.normalize(StringUtils.join(Lists.newArrayList(loc), "/"));
        return new File(path, relPath).getAbsoluteFile();
    }

    @Test
    public void testRootExists()
    {
        List<File> folders = Lists.newArrayList(getWebappRoot().listFiles());
        assertThat(folders, hasItem(allOf(
                named(ASSET_ROOT),
                FileMatchers.isDirectory()
        )));
    }

    @Test
    /**
     * Ensure the structure is adhered to.
     * See <a href="https://extranet.atlassian.com/pages/viewpage.action?pageId=2055909767">Stash Front-End Architecture & Directory Structure</a>
     */
    public void testStructureDefined()
    {
        List<File> folders = Lists.newArrayList(getWebappRoot(ASSET_ROOT).listFiles(DIR));

        assertThat(folders, hasItems(
                named("feature"),    // Sets of LESS, Soy, and JS components that will form the main content and be the focus of a page.
                named("layout"),     // LESS, Soy, and JS that make up the "chrome" of a page.
                named("lib"),        // External dependencies.
                named("model"),      // JS files containing Backbone Models. Domain objects.
                named("page"),       // One-off components of LESS, Soy, and JS.
                named("util"),       // JS files that act like libraries you can call out to. These are generally "behind-the-scenes" and don't affect UI.
                named("widget")      // A content-agnostic and reusable component, intended to be rendered and visible in the UI.
        ));
    }

    @Test
    public void testAllFileAndFolderNamesAreAllLowerCase()
    {
        Collection<File> nonConforming = getFilesRecursive(getWebappRoot(ASSET_ROOT), new FileFilter()
        {
            @Override
            public boolean accept(final File pathname)
            {
                final String name;
                return pathname != null
                        && (name = pathname.getName()) != null
                        && (name.equals(name.toLowerCase())) == false;
            }
        });
        assertThat("All file and folder names should be lowercase", nonConforming, Matchers.<File>empty());
    }

    @Test
    public void testAllFileAndFolderNamesShouldBeAlphaNumericWithHyphens()
    {
        final Pattern nonConformingPattern = Pattern.compile("[^\\w\\-]");
        Collection<File> all = getOurFiles(null);
        Collection<File> nonConforming = Collections2.filter(all, new Predicate<File>()
        {
            @Override
            public boolean apply(final File pathname)
            {
                final String name;
                return pathname != null
                    && (name = FilenameUtils.removeExtension(pathname.getName())) != null
                    && nonConformingPattern.matcher(name).find() == true;
            }
        });
        assertThat("All file and folder names should be alpha-numeric with hyphens", nonConforming, Matchers.<File>empty());
    }

//    @Test
//    public void testOnlyAllowedFileTypesArePresent()
//    {
//        Collection<File> nonConforming = getFilesRecursive(getWebappRoot(ASSET_ROOT), withoutExtension("js", "soy", "less"));
//        assertThat("Only front-end asset file types should live in this directory", nonConforming, Matchers.<File>empty());
//    }

    @Test
    public void testModelsAreOnlyJavaScript()
    {
        Collection<File> nonConforming = getFilesRecursive(getWebappRoot(ASSET_ROOT, "model"), withoutExtension("js"));
        assertThat("Models should only be JS", nonConforming, Matchers.<File>empty());
    }

    @Test
    public void testUtilsAreOnlyJavaScript()
    {
        Collection<File> nonConforming = getFilesRecursive(getWebappRoot(ASSET_ROOT, "util"), withoutExtension("js"));
        assertThat("Utils should only be JS", nonConforming, Matchers.<File>empty());
    }

    @Test
    public void testDecoratorsAreOnlySoy()
    {
        Collection<File> nonConforming = getFilesRecursive(getWebappRoot(ASSET_ROOT, "decorators"), withoutExtension("soy"));
        assertThat("Decorators should only be Soy", nonConforming, Matchers.<File>empty());
    }

    @Test
    public void testComponentFileNamesMustStartWithConsistentPrefix()
    {
        Collection<File> all = new HashSet<File>();
        all.addAll(getFilesRecursive(getWebappRoot(ASSET_ROOT, "feature"), FILE));
        all.addAll(getFilesRecursive(getWebappRoot(ASSET_ROOT, "widget"), FILE));

        Collection<File> nonConforming = Collections2.filter(all, new Predicate<File>()
        {
            @Override
            public boolean apply(@Nullable final File input)
            {
                return input != null && FilenameUtils.getBaseName(input.getName()).startsWith(input.getParentFile().getName()) == false;
            }
        });
        assertThat("Files should start with the same name as their parent folder", nonConforming, Matchers.<File>empty());
    }

    @Test
    public void testTemplateFileNamesMustStartWithConsistentPrefix()
    {
        Collection<File> all = new HashSet<File>();
        all.addAll(getFilesRecursive(getWebappRoot(ASSET_ROOT, "layout"), FILE));
        all.addAll(getFilesRecursive(getWebappRoot(ASSET_ROOT, "page"), FILE));

        Collection<File> nonConforming = Collections2.filter(all, new Predicate<File>()
        {
            @Override
            public boolean apply(@Nullable final File input)
            {
                return input != null && FilenameUtils.getBaseName(input.getName()).startsWith(input.getParentFile().getName()) == false;
            }
        });
        assertThat("Files should start with the same name as their parent folder", nonConforming, Matchers.<File>empty());
    }

    @Test
    public void testAllOurJavascriptUsesAMD()
    {
        final Collection<File> ourJsFiles = getOurFiles("js");
        final Collection<File> nonConforming = Collections2.filter(ourJsFiles, new Predicate<File>()
        {
            @Override
            public boolean apply(@Nullable final File input)
            {
                try
                {
                    List<String> lines = (List<String>) FileUtils.readLines(input);
                    final String firstLine = lines.get(0);
                    return StringUtils.isEmpty(firstLine) || firstLine.startsWith("define(") == false;
                }
                catch (Exception e)
                {
                    logger.error("A file got weird", e);
                    return true;
                }
            }
        });
        assertThat("All our javascript files need to use the AMD pattern", nonConforming, Matchers.<File>empty());
    }

    @Test
    public void testAllOurJavascriptModulesAreNamedByConvention()
    {
        final Collection<File> ourJsFiles = getOurFiles("js");
        final List<String> errors = Lists.newArrayList();
        for(final File input : ourJsFiles)
        {
            if (input == null) continue;

            boolean conforms = true;
            final String moduleName = getModuleNameFromFile(input);

            String absPath, filePath, fileName;
            absPath = input.getAbsolutePath();
            absPath = relativeFromAssetRoot(absPath);
            fileName = FilenameUtils.getBaseName(absPath);
            filePath = FilenameUtils.getFullPathNoEndSeparator(absPath);
            filePath = FilenameUtils.separatorsToUnix(filePath);
            filePath = FilenameUtils.removeExtension(filePath);

            final String validModuleName1 = FilenameUtils.normalize("jira/".concat(filePath));
            final String validModuleName2 = FilenameUtils.normalize(validModuleName1.concat("/"+fileName));

            conforms = conforms && StringUtils.isNotEmpty(moduleName);
            conforms = conforms && (
                       validModuleName1.equals(moduleName)
                    || validModuleName2.equals(moduleName)
            );

            if (!conforms)
            {
                errors.add(String.format(
                        "Expected file '%s' to define module name <%s> or <%s>, but was <%s>",
                        absPath,
                        validModuleName1,
                        validModuleName2,
                        moduleName
                ));
            }
        }
        assertThat("JS module names should begin with the root folder name, followed by the module's name, separated with a forward slash, surrounded by single quotes",
                errors,
                Matchers.<String>empty()
        );
    }

    private String relativeFromAssetRoot(final String filePath)
    {
        return filePath.split(getWebappRoot(ASSET_ROOT).getAbsolutePath())[1];
    }

    private String getModuleNameFromFile(@Nonnull final File input)
    {
        final Pattern moduleNamePattern = Pattern.compile("define\\('(.*?)',");
        String moduleName = "";
        try
        {
            List<String> lines = (List<String>)FileUtils.readLines(input);
            Matcher m = moduleNamePattern.matcher(lines.get(0));
            m.find();
            moduleName = m.group(1);
        }
        catch (Exception e)
        {
            logger.error(String.format("File '%s' got weird", input.getAbsolutePath()), e);
        }
        return moduleName;
    }

    /**
     * Convenience method to return all files written by us.
     * Basically, every folder in our structure except for 'lib'.
     * @param extension only return files of this particular file extension.
     * @return a list of matching files that were written by us.
     */
    private Collection<File> getOurFiles(final String extension)
    {
        Collection<File> files = new HashSet<File>();
        final FileFilter filter = StringUtils.isEmpty(extension) ? FILE : withExtension(extension);

        files.addAll(getFilesRecursive(getWebappRoot(ASSET_ROOT, "feature"), filter));
        files.addAll(getFilesRecursive(getWebappRoot(ASSET_ROOT, "layout"), filter));
        files.addAll(getFilesRecursive(getWebappRoot(ASSET_ROOT, "model"), filter));
        files.addAll(getFilesRecursive(getWebappRoot(ASSET_ROOT, "page"), filter));
        files.addAll(getFilesRecursive(getWebappRoot(ASSET_ROOT, "util"), filter));
        files.addAll(getFilesRecursive(getWebappRoot(ASSET_ROOT, "widget"), filter));
        return files;
    }
}
