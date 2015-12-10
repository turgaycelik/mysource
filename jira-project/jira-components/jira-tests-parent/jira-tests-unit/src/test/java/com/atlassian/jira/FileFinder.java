package com.atlassian.jira;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import com.atlassian.jira.util.dbc.Assertions;

/**
 * This Class walkes through a directory structure and based on a FileNameFilter provided by the FileChecker and if a
 * the file matches the FileNameFilter the checkFile() method is called for each file on the FileChecker (Visitor
 * pattern).
 *
 * @since v4.0
 */
public class FileFinder
{
    static final FileFilter IS_A_NON_DOT_DIR = new FileFilter()
    {
        public boolean accept(File pathname)
        {
            return pathname.isDirectory() && pathname.getName().charAt(0) != '.';
        }
    };

    private final FileChecker fileChecker;

    public FileFinder(final FileChecker fileChecker)
    {
        this.fileChecker = Assertions.notNull("fileChecker", fileChecker);
    }

    public Result checkDir(File rootDir)
    {
        Result result = new Result();
        Assertions.stateTrue(rootDir.getAbsolutePath(), rootDir.exists() && rootDir.isDirectory());
        for (File file : rootDir.listFiles(fileChecker.getFilenameFilter()))
        {
            result.add(1, fileChecker.checkFile(file));
        }
        for (File dir : rootDir.listFiles(IS_A_NON_DOT_DIR))
        {
            result.add(checkDir(dir));
        }
        return result;
    }

    /**
     * Accumulator for recursive check info
     */
    public static class Result
    {
        private int filesChecked;
        private List<String> fails = new ArrayList<String>();

        public boolean success() {
            return fails.isEmpty();
        }

        void add(int checkedCount, List<String> fails)
        {
            this.filesChecked += checkedCount;
            this.fails.addAll(fails);
        }

        void add(Result other)
        {
            add(other.filesChecked, other.fails);
        }

        public List<String> getFails()
        {
            return fails;
        }

        public int getFilesChecked()
        {
            return filesChecked;
        }
    }
}
