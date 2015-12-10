package com.atlassian.jira.util.system.patch;

import com.atlassian.jira.util.IOUtil;
import com.atlassian.jira.util.dbc.Assertions;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A class that can find patches in JIRA and return them as a set of {@link AppliedPatchInfo}s
 *
 * @since v4.0
 */
class AppliedPatchFinder
{
    /*
     The PRESENCES of this anchor file is the way that the patch finder can find patches.  if this anchor
     file is not shipped with JIRA then applied patches will not be shown
     */
    private static final String ANCHOR_FILENAME = "patching_jira.readme";
    private static final String PATCHES_CLASS_PATH = "/patches/";

    private static final Logger log = Logger.getLogger(AppliedPatchFinder.class);

    Set<AppliedPatchInfo> getAppliedPatches()
    {
        // finds a file called jira.patches in the /patches class loading directory
        final File patchAnchor = findPatchFileAnchor();
        if (patchAnchor == null)
        {
            return Collections.emptySet();
        }
        return findAppliedPatches(patchAnchor);
    }

    File findPatchFileAnchor()
    {
        final URL patchReadMe = findPatchFileURL();
        if (patchReadMe != null)
        {
            if (patchReadMe.getProtocol().equals("file"))
            {
                try
                {
                    File f = new File(patchReadMe.toURI());
                    if (f.exists())
                    {
                        return f;
                    }
                }
                catch (URISyntaxException e)
                {
                    log.error("This should never happen", e);
                }
            }
            else
            {
                log.warn("AppliedPatchFinder will be skipped because it can only work on simple files (not jars). patching_jira.readme URL = " + patchReadMe);
            }
        }
        return null;
    }

    URL findPatchFileURL()
    {
        return getClass().getResource(PATCHES_CLASS_PATH + ANCHOR_FILENAME);
    }

    private Set<AppliedPatchInfo> findAppliedPatches(final File patchAnchorFile)
    {
        Set<AppliedPatchInfo> patches = new HashSet<AppliedPatchInfo>();
        File patchesDir = patchAnchorFile.getParentFile();
        final File[] files = patchesDir.listFiles();
        for (File patchFile : files)
        {
            if (!validFile(patchFile))
            {
                continue;
            }
            final String issueKey = getKey(patchFile);
            final String issueDesc = getDesc(patchFile);
            patches.add(new AppliedPatchInfoImpl(issueKey, issueDesc));
        }
        return patches;
    }

    private boolean validFile(final File patchFile)
    {
        if (patchFile.isDirectory())
        {
            return false;
        }
        final String fileName = patchFile.getName();
        // is it our anchor file
        if (ANCHOR_FILENAME.equals(fileName))
        {
            return false;
        }
        // does it start with . as in .svn for example
        int lastIndex = fileName.lastIndexOf(".");
        if (lastIndex == 0)
        {
            return false;
        }
        return true;

    }

    private String getKey(final File patchFile)
    {
        String key = patchFile.getName();
        int lastIndex = key.lastIndexOf(".");
        if (lastIndex > 0)
        {
            key = key.substring(0, lastIndex);
        }
        return key;
    }

    private String getDesc(final File patchFile)
    {
        try
        {

            StringWriter sw = new StringWriter();
            FileReader fr = new FileReader(patchFile);
            IOUtil.copy(fr, sw);
            IOUtil.shutdownReader(fr);
            return sw.toString();
        }
        catch (FileNotFoundException e)
        {
            final String message = "Could not find patch file : " + patchFile;
            log.error(message, e);
            return message;
        }
        catch (IOException e)
        {
            final String message = "Could not read patch file : " + patchFile;
            log.error(message, e);
            return message;
        }
    }

    static class AppliedPatchInfoImpl implements AppliedPatchInfo, Comparable<AppliedPatchInfo>
    {
        private final String issueKey;
        private final String desc;

        public AppliedPatchInfoImpl(final String issueKey, final String desc)
        {
            this.issueKey = Assertions.notNull("issueKey", issueKey);
            this.desc = Assertions.notNull("issueDesc", desc);
        }

        public String getIssueKey()
        {
            return issueKey;
        }

        public String getDescription()
        {
            return desc;
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            final AppliedPatchInfoImpl that = (AppliedPatchInfoImpl) o;

            if (!desc.equals(that.desc))
            {
                return false;
            }
            if (!issueKey.equals(that.issueKey))
            {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = issueKey.hashCode();
            result = 31 * result + desc.hashCode();
            return result;
        }

        public int compareTo(final AppliedPatchInfo o)
        {
            if (this.equals(o))
            {
                return 0;
            }
            int rc = this.issueKey.compareTo(o.getIssueKey());
            if (rc == 0)
            {
                rc = this.desc.compareTo(o.getDescription());
            }
            return rc;
        }

        @Override
        public String toString()
        {
            return new StringBuilder().append(issueKey).append(" : ").append(desc).toString();
        }
    }
}
