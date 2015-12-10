package com.atlassian.jira.web.action.filter;

import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.user.ApplicationUser;

/**
 * Encapsulates the logic of what filter operations are available in the UI.
 *
 * @since v4.0
 */
public final class FilterOperationsBean
{
    private boolean showEdit;
    private boolean showSave;
    private boolean showSaveNew;
    private boolean showSaveAs;
    private boolean showReload;
    private boolean showViewSubscriptions;
    private boolean showInvalid;

    FilterOperationsBean()
    {
        showEdit = false;
        showSave = false;
        showSaveNew = false;
        showSaveAs = false;
        showReload = false;
        showViewSubscriptions = false;
    }

    public boolean isShowInvalid()
    {
        return showInvalid;
    }

    public boolean isShowEdit()
    {
        return showEdit;
    }

    public void setShowEdit(final boolean showEdit)
    {
        this.showEdit = showEdit;
    }

    public boolean isShowSave()
    {
        return showSave;
    }

    public void setShowSave(final boolean showSave)
    {
        this.showSave = showSave;
    }

    public boolean isShowSaveNew()
    {
        return showSaveNew;
    }

    public void setShowSaveNew(final boolean showSaveNew)
    {
        this.showSaveNew = showSaveNew;
    }

    public boolean isShowSaveAs()
    {
        return showSaveAs;
    }

    public void setShowInvalid(final boolean showInvalid)
    {
        this.showInvalid = showInvalid;
    }

    public void setShowSaveAs(final boolean showSaveAs)
    {
        this.showSaveAs = showSaveAs;
    }

    public boolean isShowReload()
    {
        return showReload;
    }

    public void setShowReload(final boolean showReload)
    {
        this.showReload = showReload;
    }

    public boolean isShowViewSubscriptions()
    {
        return showViewSubscriptions;
    }

    public void setShowViewSubscriptions(final boolean showViewSubscriptions)
    {
        this.showViewSubscriptions = showViewSubscriptions;
    }

    public boolean hasOperation()
    {
        return isShowViewSubscriptions() || isShowReload() || isShowSaveAs() || isShowSaveNew() || isShowSave() || isShowReload() || isShowEdit() || isShowInvalid();
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append("FilterOperationsBean");
        sb.append("{showEdit=").append(showEdit);
        sb.append(", showSave=").append(showSave);
        sb.append(", showSaveNew=").append(showSaveNew);
        sb.append(", showSaveAs=").append(showSaveAs);
        sb.append(", showReload=").append(showReload);
        sb.append(", showViewSubscriptions=").append(showViewSubscriptions);
        sb.append(", showInvalid=").append(showInvalid);
        sb.append('}');
        return sb.toString();
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

        final FilterOperationsBean that = (FilterOperationsBean) o;

        if (showEdit != that.showEdit)
        {
            return false;
        }
        if (showReload != that.showReload)
        {
            return false;
        }
        if (showSave != that.showSave)
        {
            return false;
        }
        if (showSaveNew != that.showSaveNew)
        {
            return false;
        }
        if (showSaveAs != that.showSaveAs)
        {
            return false;
        }
        if (showViewSubscriptions != that.showViewSubscriptions)
        {
            return false;
        }
        if (showInvalid != that.showInvalid)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = (showEdit ? 1 : 0);
        result = 31 * result + (showSave ? 1 : 0);
        result = 31 * result + (showSaveNew ? 1 : 0);
        result = 31 * result + (showSaveAs ? 1 : 0);
        result = 31 * result + (showReload ? 1 : 0);
        result = 31 * result + (showViewSubscriptions ? 1 : 0);
        result = 31 * result + (showInvalid ? 1 : 0);
        return result;
    }

    /**
     * Factory method that uses common inputs to build a sensible default state.
     *
     * @param searchRequest the search request as provided by the action.
     * @param validFilter true only if the passed filter is valid for JIRA's current state.
     * @param user the remote user or null if there is none.
     * @param isAdvancedScreen true if the operations are to be displayed on the advanced screen
     * @return a FilterOperationsBean representing the correct state of the operations panel for filters.
     */
    public static FilterOperationsBean create(SearchRequest searchRequest, boolean validFilter, ApplicationUser user, final boolean isAdvancedScreen)
    {
        FilterOperationsBean bean = new FilterOperationsBean();
        if (searchRequest != null)
        {
            boolean ownFilter = searchRequest.getOwner() == null ||
                    searchRequest.getOwner().equals(user);

            boolean searchRequestLoaded = searchRequest.isLoaded();
            final boolean searchRequestModified = searchRequest.isModified();
            if (ownFilter)
            {
                if (searchRequestLoaded)
                {
                    bean.setShowEdit(true);
                    bean.setShowViewSubscriptions(true);
                    if (validFilter && searchRequestLoaded)
                    {
                        bean.setShowSave(searchRequestModified);
                        bean.setShowSaveAs(true);
                    }
                }
                bean.setShowSaveNew(!searchRequestLoaded && validFilter);
            }
            bean.setShowReload(searchRequestLoaded && searchRequestModified);
            bean.setShowInvalid(!validFilter && !isAdvancedScreen);
        }
        return bean;
    }
}
