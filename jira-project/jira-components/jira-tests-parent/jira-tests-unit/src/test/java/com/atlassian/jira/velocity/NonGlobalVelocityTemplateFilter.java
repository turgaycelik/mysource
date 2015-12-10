package com.atlassian.jira.velocity;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import com.atlassian.core.util.ClassLoaderUtils;

import org.apache.velocity.runtime.RuntimeConstants;

/**
 * A FilenameFilter that identifies velocity templates that are NOT configured as global velocity files like macros.vm.
 *
 * Note that to improve performance, this contains a static cache of velocity template Files. This is OK because the JVM
 * only lasts for the unit test run.
 *
 * @since v5.1
 */
class NonGlobalVelocityTemplateFilter implements FilenameFilter
{

    static final NonGlobalVelocityTemplateFilter INSTANCE = new NonGlobalVelocityTemplateFilter();

    private List<String> velocityFiles;

    public boolean accept(File dir, String name)
    {
        final File velocityFile = new File(dir, name);
        String path = dir.getPath();
        return name.endsWith(".vm")
                && !path.contains("target")
                && !path.contains("classes")
                && !isGlobalVelocityFile(velocityFile)
                && !name.equals("velocity_implicit.vm"); // this is just for IDEA linking
    }

    private List<String> getGlobalVelocityFiles()
    {
        if (velocityFiles == null)
        {
            velocityFiles = new ArrayList<String>();
            Properties props = new Properties();
            try
            {
                props.load(ClassLoaderUtils.getResourceAsStream("velocity.properties", getClass()));
            }
            catch (IOException e)
            {
                throw new RuntimeException("Failed to find velocity.properties file.", e);
            }

            final Object globalMacros = props.get(RuntimeConstants.VM_LIBRARY);

            StringTokenizer tokenizer = new StringTokenizer((String) globalMacros, ",");

            while (tokenizer.hasMoreElements())
            {
                final String velocityFile = tokenizer.nextToken().trim();
                velocityFiles.add(velocityFile);

            }
        }
        return velocityFiles;
    }

    private boolean isGlobalVelocityFile(File velocityFile)
    {
        List<String> globalVelocityFiles = getGlobalVelocityFiles();
        for (String globalVelocityFile : globalVelocityFiles)
        {
            if (velocityFile.getPath().endsWith(globalVelocityFile))
            {
                // we assume that the path fragment (classpath relative) we get from the velocity config is
                // a sufficiently unique path suffix.
                return true;
            }
        }
        return false;
    }
}
