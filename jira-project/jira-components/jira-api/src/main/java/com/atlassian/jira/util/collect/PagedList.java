package com.atlassian.jira.util.collect;

import java.util.Iterator;
import java.util.List;

/**
 * Page over an underlying list, either by navigating to a page or iterating a page at a time.
 *
 * @since v6.1
 */
public interface PagedList<E>
{

    List<E> getPage(int pageNumber);

    Iterator<List<E>> iterator();

    List<E> getCompleteList();

    int getSize();

    int getPageSize();
}
