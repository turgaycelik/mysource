package com.atlassian.jira.issue.attachment;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import com.atlassian.jira.util.cache.WeakInterner;

import com.google.common.collect.Lists;

import org.apache.commons.lang3.StringUtils;

import static com.atlassian.jira.util.cache.WeakInterner.newWeakInterner;

/**
 * Copied from FECRU
 * <p/>
 * ./java/com/cenqua/fisheye/Path.java r54145
 */
public class Path implements Serializable, Comparable<Path>
{
    private static final long serialVersionUID = -8932171926020964811L;

    private static final String[] EMPTY_STRING_ARRAY = {};
    private static final String ELLIPSIS = "...";

    private static final Pattern SPLIT_PATTERN = Pattern.compile("/");
    private static final Pattern SLOSH_SPLIT_PATTERN = Pattern.compile("[/|\\\\]");

    private static final WeakInterner<String> INTERNER = newWeakInterner();

    private final String[] path;

    // Thread-safety note: the value is an immutable object that is deterministically generated, so
    // the lack of proper publishing is ok (provided ref publishing is atomic, which it generally
    // should be).  If somebody gets scared, they can mark this volatile.
    private String stringRep;


    public Path()
    {
        this.path = EMPTY_STRING_ARRAY;
    }

    public Path(CharSequence path)
    {
        this.path = canonicalize(split(path));
    }

    public Path(CharSequence path, boolean allowSloshes)
    {
        this.path = canonicalize(split(path, allowSloshes));
    }

    public Path(Path path)
    {
        this.path = path.path;
    }

    public Path(Path aParent, String aPath)
    {
        this.path = canonicalize(join(aParent.path, split(aPath)));
    }

    public Path(Path aParent, Path aPath)
    {
        this.path = canonicalize(join(aParent.path, aPath.path));
    }

    public Path(Path aParent, String aPath, boolean allowSloshes)
    {
        this.path = canonicalize(join(aParent.path, split(aPath, allowSloshes)));
    }

    public Path(String aParent, Path aPath)
    {
        this.path = canonicalize(join(split(aParent), aPath.path));
    }



    private Path(List<String> start)
    {
        this.path = start.toArray(new String[start.size()]);
    }

    private Path(String[] comp)
    {
        this.path = canonicalize(comp);
    }



    public Path getParent()
    {
        if (path.length == 0)
        {
            return this;
        }
        return new Path(Arrays.copyOf(path, path.length - 1));
    }

    /**
     * @return the string representation of the path
     */
    public String getPath()
    {
        if (stringRep == null)
        {
            stringRep = StringUtils.join(path, '/');
        }
        return stringRep;
    }


    public String[] getComponents()
    {
        return path;
    }

    public String getName()
    {
        if (path.length == 0)
        {
            return ""; // is this right?
        }
        return path[path.length - 1];
    }

    public String toString()
    {
        return getPath();
    }

    public boolean equals(Object o)
    {
        return this == o ||
                (o instanceof Path && Arrays.equals(this.path, ((Path)o).path));
    }

    public int hashCode()
    {
        int result = 0;
        for (String s : path)
        {
            result += s.hashCode();
        }
        return result;
    }

    private static String[] join(String[] aLeft, String[] aRight)
    {
        String[] result = new String[aLeft.length + aRight.length];
        System.arraycopy(aLeft, 0, result, 0, aLeft.length);
        System.arraycopy(aRight, 0, result, aLeft.length, aRight.length);
        return result;
    }

    /**
     * Intelligently Put a "/" between two strings.
     */
    public static String join(String str1, String str2)
    {
        // Special cases for null or blank inputs
        if (str1 == null || str1.isEmpty())
        {
            return (str2 == null) ? "" : str2;
        }
        if (str2 == null || str2.isEmpty())
        {
            return str1;
        }

        final StringBuilder sb = new StringBuilder(str1.length() + str2.length() + 1);

        // If str1 lacks a trailing slash, then add one
        sb.append(str1);
        if (sb.charAt(sb.length() - 1) != '/')
        {
            sb.append('/');
        }

        // If str2 has a leading slash, then skip over it
        final int startAt = (str2.charAt(0) == '/') ? 1 : 0;
        return sb.append(str2, startAt, str2.length()).toString();
    }

    private static String[] split(CharSequence aPath)
    {
        return split(aPath, false);
    }

    private static String[] split(CharSequence aPath, boolean allowSloshes)
    {
        if ((aPath == null) || aPath.length() == 0)
        {
            return EMPTY_STRING_ARRAY;
        }

        if (aPath.charAt(0) == '/')
        {
            if (aPath.length() == 1)
            {
                return EMPTY_STRING_ARRAY;
            }
            aPath = aPath.subSequence(1, aPath.length()); // the split function includes an extra match, otherwise
        }

        final Pattern splitPattern = allowSloshes ? SLOSH_SPLIT_PATTERN : SPLIT_PATTERN;
        return splitPattern.split(aPath);
    }

    public boolean isRoot()
    {
        return path.length == 0 || (path.length == 1 && path[0].isEmpty());
    }

    public int compareTo(Path o)
    {
        return (o == null) ? -1 : compare(path, o.path);
    }

    private static int compare(String[] path1, String[] path2)
    {
        final int limit = Math.min(path1.length, path2.length);
        for (int i = 0; i < limit; i++)
        {
            final int r = path1[i].compareTo(path2[i]);
            if (r != 0)
            {
                return r;
            }
        }

        // Length is always non-negative, so these is no sign overflow risk.
        return path1.length - path2.length;
    }

    /**
     * abbreviate a path by removing path components from the middle until the length of the path (as via .getPath()) is
     * not greater then maxLength.
     * <p/>
     * The first and last path component are always in the returned path.
     * <p/>
     * If any path components were removed, an ellipsis ("...") will appear in the middle
     * <p/>
     * The returned path may in some cases still be greater.
     */
    public Path abbreviate(int maxLength)
    {
        if (path.length <= 2 || getPath().length() <= maxLength)
        {
            return this;
        }

        final List<String> components = Lists.newArrayList(path);
        int insertEllipsesAt = components.size() / 2;
        int len = getPath().length();

        while (len > maxLength && components.size() > 2)
        {
            final int midPoint = components.size() / 2;
            insertEllipsesAt = Math.min(insertEllipsesAt, midPoint);

            final String s = components.remove(midPoint);
            len -= s.length() + 1;
        }

        components.add(insertEllipsesAt, ELLIPSIS);
        return new Path(components);
    }

    public String getPath(boolean caseSensitive)
    {
        String normalized;
        if (caseSensitive)
        {
            normalized = getPath();
        }
        else
        {
            normalized = getPath().toLowerCase(Locale.US);
        }
        return normalized;
    }

    public void toLowerCase()
    {
        for (int i = 0; i < path.length; i++)
        {
            path[i] = path[i].toLowerCase();
        }
    }

    /**
     * @return the file extension of the path (with no .), or null if there is no file extension
     */
    public String getExtension()
    {
        String extn = null;
        if (path.length > 0)
        {
            String file = path[path.length - 1];
            int dot = file.lastIndexOf('.');
            if (dot != -1 && dot != file.length() - 1)
            {
                extn = file.substring(dot + 1);
            }
        }
        return extn;
    }

    private static String[] canonicalize(String[] components)
    {
        for (int i = 0; i < components.length; i++)
        {
            components[i] = INTERNER.intern(components[i]);
        }
        return components;
    }
}
