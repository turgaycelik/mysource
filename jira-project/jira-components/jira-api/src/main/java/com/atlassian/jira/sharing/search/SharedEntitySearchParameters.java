package com.atlassian.jira.sharing.search;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.sharing.SharedEntityColumn;

/**
 * Interface for the passing parameters when searching for SharedEntity instances.
 *
 * @since v3.13
 */
@PublicApi
public interface SharedEntitySearchParameters
{
    /**
     * Returns the name fragments to search for.  The string is broken into white-space delimited words and each word is
     * used as a OR'ed partial match for the name.  If this is null, then SharedEntity's will be returned without regard
     * to their name.
     *
     * @return the name fragments to search for.
     */
    String getName();

    /**
     * Returns the description fragments to search for.  The string is broken into white-space delimited words and each
     * word is used as a OR'ed partial match for the description.  If this is null, then SharedEntity's will be returned
     * without regard to their description.
     *
     * @return the description fragments to search for.
     */
    String getDescription();

    /**
     * This controls how the text fragment parameters (name and description) are combined and searched.
     * <p/>
     * If this value is TextSearchMode.AND then the name AND the description will be used to match
     * <p/>
     * if this value is TextSearchMode.OR then the name OR the description will be used to match
     *
     * @return one of TextSearchMode.AND or TextSearchMode.OR.  A value of null will be considered as
     *         TextSearchMode.AND
     */
    TextSearchMode getTextSearchMode();

    /**
     * Returns the user name to search for.  If this is null, then SharedEntity's will be returned without regard to
     * which user shared them.
     *
     * @return the user name to search for.
     */
    String getUserName();

    /**
     * Returns a tri-state Boolean indicating how favourites are to be searched.
     * <p/>
     * <ul> <li>IF the Boolean is null, then it doesn't care if the SharedEntity is a favourite or not</li> <li>IF the
     * Boolean is true, then only favourited SharedEntity are returned</li> <li>IF the Boolean is false, then only non
     * favourited SharedEntity are returned</li> </ul>
     *
     * @return a tri-state Boolean indicating how favourites are to be searched.
     */
    Boolean getFavourite();

    /**
     * @return true if the results should be sorted in ascending order
     */
    boolean isAscendingSort();

    /**
     * Returns the SharedEntityColumn to sort the results by.  {@link SharedEntityColumn#NAME} will be used by default.
     * <p/>
     * The following constants MUST be used <ul> <li>{@link com.atlassian.jira.sharing.SharedEntityColumn#DESCRIPTION}</li>
     * <li>{@link com.atlassian.jira.sharing.SharedEntityColumn#FAVOURITE_COUNT}</li> <li>{@link
     * com.atlassian.jira.sharing.SharedEntityColumn#ID}</li> <li>{@link com.atlassian.jira.sharing.SharedEntityColumn#NAME}</li>
     * <li>{@link com.atlassian.jira.sharing.SharedEntityColumn#OWNER}</li> </ul>
     *
     * @return the SharedEntityColumn used for searching.  It MUST not be null.
     */
    SharedEntityColumn getSortColumn();

    /**
     * Returns the ShareTypeSearchParameter object to be used.  When this parameter is non null, then only
     * SharedEntity's shared via that share type will be returned.  If the value is null, then SharedEntity's will be
     * returned without regard to how they were shared.
     *
     * @return the ShareTypeSearchParameter to use.  It can be null.
     */
    ShareTypeSearchParameter getShareTypeParameter();

    /**
     * Returns the SharedEntitySearchContext under which this search is to be executed. This can either be
     * {@link com.atlassian.jira.sharing.search.SharedEntitySearchContext#ADMINISTER} or
     * {@link SharedEntitySearchContext#USE}.
     *
     * @return the SharedEntitySearchContext under which this search is to be executed. Can not be null.
     */
    SharedEntitySearchContext getEntitySearchContext();

    /**
     * A type safe enumeration for specify text searching mode.  It can be either TextSearchMode.AND or
     * TextSearchMode.OR
     */
    public final class TextSearchMode
    {
        public static final TextSearchMode OR = new TextSearchMode();
        public static final TextSearchMode AND = new TextSearchMode();
        public static final TextSearchMode EXACT = new TextSearchMode();
        public static final TextSearchMode WILDCARD = new TextSearchMode();

        private TextSearchMode()
        {}
    }
}
