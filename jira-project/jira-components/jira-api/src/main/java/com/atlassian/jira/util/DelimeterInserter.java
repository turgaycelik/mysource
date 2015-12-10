package com.atlassian.jira.util;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * This can take an input string and look into it for occurences of given terms.  It will then
 * intert delimeters into the input string arround the terms.  You can use this to hilight
 * text with bold tags in HTML for example.
 * <p/>
 * This is smart enough to merge areas when they overlap or exit side by side, and only one set of delimeters
 * will be inserted.
 */
public class DelimeterInserter
{
    private String frontDelimeter;
    private String endDelimeter;
    private boolean prefixMode;
    private boolean caseInsensitive;
    private String consideredWhitespace;

    private static class DelimeterMarker
    {
        private int startIndex;
        private int endIndex;
        private final String targetString;

        public DelimeterMarker(int startIndex, int endIndex, String targetString)
        {
            this.startIndex = startIndex;
            this.endIndex = endIndex;
            this.targetString = targetString;
        }

        public int getEndIndex()
        {
            return endIndex;
        }

        public int getStartIndex()
        {
            return startIndex;
        }

        public boolean containsAllOf(DelimeterMarker m)
        {
            return (this.startIndex <= m.startIndex && this.endIndex >= m.endIndex);
        }

        public boolean containsSomeOf(DelimeterMarker m)
        {
            if (m.startIndex < this.startIndex)
            {
                return isWithin(m.endIndex);
            }
            if (m.endIndex > this.endIndex)
            {
                return isWithin(m.startIndex);
            }
            return false;
        }

        private boolean isWithin(int sIndex)
        {
            return (sIndex >= this.startIndex && sIndex <= this.endIndex);
        }

        public void extendToInclude(DelimeterMarker m)
        {
            // only do it if we are actually partially contained
            if (this.containsSomeOf(m))
            {
                this.startIndex = Math.min(this.startIndex, m.startIndex);
                this.endIndex = Math.max(this.endIndex, m.endIndex);
            }
        }


        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append("DelimeterMarker");
            sb.append(" si : ");
            sb.append(startIndex);
            sb.append(" ei : ");
            sb.append(endIndex);
            sb.append(" ( ");
            sb.append(targetString.substring(startIndex, endIndex));
            sb.append(" ) ");
            return sb.toString();
        }
    }

    /**
     * Creates a DelimeterInserter that is in prefix mode and case insenstive.
     *
     * @param frontDelimeter the delimeter to use as around the front of a term
     * @param endDelimeter   the delimeter to use as around the end of a term
     */
    public DelimeterInserter(String frontDelimeter, String endDelimeter)
    {
        this(frontDelimeter, endDelimeter, true, true);
    }

    /**
     * Creates a DelimeterInserter that is  case insenstive.
     *
     * @param frontDelimeter the delimeter to use as around the front of a term
     * @param endDelimeter   the delimeter to use as around the end of a term
     * @param prefixMode     whether a match must be made on word boundaries
     */
    public DelimeterInserter(String frontDelimeter, String endDelimeter, boolean prefixMode)
    {
        this(frontDelimeter, endDelimeter, prefixMode, true);
    }

    /**
     * @param frontDelimeter  the delimeter to use as around the front of a term
     * @param endDelimeter    the delimeter to use as around the end of a term
     * @param prefixMode      whether a match must be made on word boundaries
     * @param caseInsensitive whether matching is case insenstive
     */
    public DelimeterInserter(String frontDelimeter, String endDelimeter, boolean prefixMode, boolean caseInsensitive)
    {
        this.frontDelimeter = frontDelimeter;
        this.endDelimeter = endDelimeter;
        this.prefixMode = prefixMode;
        this.caseInsensitive = caseInsensitive;
    }


    /**
     * @return the String characters that can be considered whitespace ALONG with Character.iswhiteSpace().
     */
    public String getConsideredWhitespace()
    {
        return consideredWhitespace;
    }

    /**
     * The String characters that can be considered whitespace ALONG with Character.iswhiteSpace().
     *
     * @param consideredWhitespace the extra whitespace characters
     */

    public void setConsideredWhitespace(String consideredWhitespace)
    {
        this.consideredWhitespace = consideredWhitespace;
    }

    private boolean isAtStartOfWord(String targetString, int startIndex)
    {
        if (startIndex == 0)
        {
            return true;

        }
        char previousCh = targetString.charAt(startIndex - 1);
        return Character.isWhitespace(previousCh) || (this.consideredWhitespace != null && this.consideredWhitespace.indexOf(previousCh) != -1);
    }

    /**
     * Called to do the actual delimeter intertion
     *
     * @param targetString the target string to insert the delimeters into
     * @param terms        the terms to look for in the targetString
     * @return a string with delimeters around any terms within it
     */
    public String insert(String targetString, String[] terms)
    {
        if (targetString == null)
        {
            throw new IllegalArgumentException("targetString must be non null!");
        }
        frontDelimeter = (frontDelimeter == null ? "" : frontDelimeter);
        endDelimeter = (endDelimeter == null ? "" : endDelimeter);
        terms = (terms == null ? new String[0] : terms);
        if (terms.length == 0)
        {
            return targetString;
        }
        if (frontDelimeter.length() == 0 && endDelimeter.length() == 0)
        {
            return targetString;
        }

        List markers = new ArrayList();

        // sort terms into longest length and natural order
        Arrays.sort(terms, new Comparator()
        {
            public int compare(Object o1, Object o2)
            {
                String s1 = (String) (o1 != null ? o1 : "");
                String s2 = (String) (o2 != null ? o2 : "");
                if (s1.equals(s2))
                {
                    return 0;
                }
                if (s1.length() == s2.length())
                {
                    return s1.compareTo(s2);
                }
                else
                {
                    return s2.length() - s1.length(); // longest first
                }
            }
        });

        String matchString = this.caseInsensitive ? targetString.toLowerCase() : targetString;
        //
        // create markers for all the terms
        for (final String term1 : terms)
        {
            int currentIndex = 0;
            String term = term1;

            // a null term or empty string is a shite term to search for
            if (StringUtils.isBlank(term))
            {
                continue;
            }
            term = this.caseInsensitive ? term.toLowerCase() : term;
            //
            // find all occurences of the term inside the string
            while (true)
            {
                int startIndex = matchString.indexOf(term, currentIndex);
                int endIndex = startIndex + term.length();
                if (startIndex == -1)
                {
                    break;
                }
                currentIndex = endIndex;
                DelimeterMarker newMarker = new DelimeterMarker(startIndex, endIndex, targetString);

                if (markers.size() == 0)
                {
                    //
                    // are we in prefix mode and hence we must be at the prefix of a word in order to add a new marker
                    if (prefixMode)
                    {
                        if (isAtStartOfWord(targetString, startIndex))
                        {
                            markers.add(newMarker);
                        }
                    }
                    else
                    {
                        markers.add(newMarker);
                    }
                }
                else
                {
                    boolean addMarker = true;
                    for (final Object marker : markers)
                    {
                        DelimeterMarker m1 = (DelimeterMarker) marker;
                        if (m1.containsAllOf(newMarker))
                        {
                            // do nothing
                            addMarker = false;
                            break;
                        }
                        else if (m1.containsSomeOf(newMarker))
                        {
                            // just extend the current marker, dont add a new one
                            m1.extendToInclude(newMarker);
                            addMarker = false;
                            break;
                        }
                    }
                    if (addMarker)
                    {
                        //
                        // are we in prefix mode and hence we must be at the prefix of a word in order to add a new marker
                        if (prefixMode)
                        {
                            if (isAtStartOfWord(matchString, startIndex))
                            {
                                markers.add(newMarker);
                            }
                        }
                        else
                        {
                            markers.add(newMarker);
                        }
                    }
                }
            }
        }
        //
        // sort them into index order
        Collections.sort(markers, new Comparator()
        {

            public int compare(Object o1, Object o2)
            {
                DelimeterMarker m1 = (DelimeterMarker) o1;
                DelimeterMarker m2 = (DelimeterMarker) o2;
                return m1.startIndex - m2.startIndex;
            }
        });

        //
        // merge any markers that are side by side
        List mergedList = new ArrayList();
        Iterator iterator = markers.iterator();
        if (iterator.hasNext())
        {
            DelimeterMarker currentMarker = (DelimeterMarker) iterator.next();

            mergedList.add(currentMarker);

            while (iterator.hasNext())
            {
                DelimeterMarker next = (DelimeterMarker) iterator.next();
                if (currentMarker.containsSomeOf(next))
                {
                    currentMarker.extendToInclude(next);
                }
                else
                {
                    currentMarker = next;
                    mergedList.add(currentMarker);
                }
            }

        }

        //
        // now run through the markers and insert delimeters
        String subStr;
        StringBuilder sb = new StringBuilder();
        int index = 0;
        for (iterator = mergedList.iterator(); iterator.hasNext();)
        {
            DelimeterMarker m1 = (DelimeterMarker) iterator.next();
            // read from current to the start of marker
            subStr = targetString.substring(index, m1.getStartIndex());
            sb.append(subStr);
            // insert front delimeter
            sb.append(frontDelimeter);
            // read the marker text
            subStr = targetString.substring(m1.getStartIndex(), m1.getEndIndex());
            sb.append(subStr);
            // end delimeter next
            sb.append(endDelimeter);
            // move the cursor
            index = m1.getEndIndex();
            if (index >= targetString.length())
            {
                break;
            }
        }
        // any left over?
        if (index < targetString.length())
        {
            subStr = targetString.substring(index);
            sb.append(subStr);
        }
        return sb.toString();
    }
}
