package com.atlassian.jira.web.action.util.lists;

import com.atlassian.jira.util.dbc.Assertions;

import java.util.List;

/**
 * This is a simple pager that can reduce a big list into paged sizes.  Its designed for use within webwork actions/jsp
 * in that it is immutable (eg one page per created ListPager) and actions can only really show one page of data per
 * render.
 * <p/>
 * You need to specify the page number you want to go to, the number of rows per page and the full list you want to
 * page.
 */
public abstract class ListPager<T>
{
    private final List<T> pagedList;
    private final int pageNo;
    private final int rowsPerPage;
    private final int fullListSize;
    private final int fromIndex;
    private final int toIndex;

    /**
     * Creates a ListPager that pages a list from a certain page number.  If the page number is greater than makes
     * sense, it is trims it back to the last possible page number.
     *
     * @param fullList is the full list of object
     * @param pageNo is a zero based index
     * @param rowsPerPage must be > 0
     */
    public ListPager(List<T> fullList, int pageNo, int rowsPerPage)
    {
        Assertions.notNull("fullList", fullList);
        Assertions.stateTrue("rowsPerPage must be greater than 0", rowsPerPage > 0);

        this.rowsPerPage = rowsPerPage;
        this.fullListSize = fullList.size();

        // make sure we cant really go past the last page
        this.pageNo = Math.min(getEndPageNo(), Math.max(0, pageNo));

        // init the paged sub list
        int fromIndex = Math.min(this.fullListSize, this.pageNo * rowsPerPage);
        int toIndex = Math.min(this.fullListSize, fromIndex + rowsPerPage);

        if (fromIndex == 0 && toIndex == (fullListSize - 1))
        {
            this.pagedList = fullList;
        }
        else
        {
            this.pagedList = fullList.subList(fromIndex, toIndex);
        }
        this.fromIndex = fromIndex;
        this.toIndex = toIndex;
    }

    /**
     * @return null if there is no previous link url possible otherwise calls {@link #generatePageURL(int, int)} to
     *         generate an URL
     */
    public String getPrevPageURL()
    {
        if (fromIndex <= 0)
        {
            return null;
        }
        return generatePageURL(pageNo - 1, rowsPerPage);
    }

    /**
     * @return null if there is no next link url possible otherwise calls {@link #generatePageURL(int, int)} to generate
     *         an URL
     */
    public String getNextPageURL()
    {
        if (toIndex + 1 >= fullListSize || !hasMultiplePages())
        {
            return null;
        }
        return generatePageURL(pageNo + 1, rowsPerPage);
    }

    public String getStartPageURL()
    {
        if (!hasMultiplePages() || pageNo == 0)
        {
            return null;
        }
        return generatePageURL(0, rowsPerPage);
    }

    public String getEndPageURL()
    {
        int endPageNo = getEndPageNo();
        if (!hasMultiplePages()  || pageNo >= endPageNo)
        {
            return null;
        }
        return generatePageURL(endPageNo, rowsPerPage);
    }

    private int getEndPageNo()
    {
        int endPageNo = 0;
        if (fullListSize > rowsPerPage)
        {
            int leftOver = (fullListSize % rowsPerPage == 0 ? 0 : 1);
            endPageNo = ((fullListSize / rowsPerPage) + leftOver) - 1;
        }
        return endPageNo;
    }


    /**
     * Each implementation of this class is expected to generate their own URLS based on the passed in page number
     *
     * @param targetPageNo the target page number
     * @param rowsPerPage the rows per page
     * @return an non null URL
     */
    protected abstract String generatePageURL(int targetPageNo, int rowsPerPage);

    /**
     * @return the paged list of object
     */
    public List<T> getList()
    {
        return pagedList;
    }

    public int getPageNumber()
    {
        return pageNo;
    }

    public int getRowsPerPage()
    {
        return rowsPerPage;
    }

    public int getFullListSize()
    {
        return fullListSize;
    }

    public int getFromIndex()
    {
        return fromIndex;
    }

    public int getToIndex()
    {
        return toIndex;
    }

    /**
     * Returns true if the pager has multiple pages.  If this is false, then all the link generating methods will return
     * null, since there isnt more than one page of data.
     *
     * @return returns true if the pager has multiple pages
     */
    public boolean hasMultiplePages()
    {
        return (fullListSize > rowsPerPage);
    }
}
