<%@ taglib uri="webwork" prefix="ww" %>
<ul class="vertical tabs">
    <ww:if test="tabShowing('favourites') == true">
        <li id="mf_fav_li" class="<ww:if test="filterView == 'favourites'">active</ww:if><ww:if test="firstTab('favourites') == true"> first</ww:if>"><a id="fav-filters-tab" title="<ww:text name="'managefilters.favourite.desc'"/>" href="<%= request.getContextPath() %>/secure/ManageFilters.jspa?filterView=favourites"><strong><ww:text name="'common.favourites.favourite'"/></strong></a></li>
    </ww:if>
    <ww:if test="tabShowing('my') == true">
        <li id="mf_my_li" class="<ww:if test="filterView == 'my'">active</ww:if><ww:if test="firstTab('my') == true"> first</ww:if>"><a id="my-filters-tab" title="<ww:text name="'managefilters.my.desc'"/>" href="<%= request.getContextPath() %>/secure/ManageFilters.jspa?filterView=my"><strong><ww:text name="'managefilters.my'"/></strong></a></li>
    </ww:if>
    <ww:if test="tabShowing('popular') == true">
        <li id="mf_pop_li" class="<ww:if test="filterView == 'popular'">active</ww:if><ww:if test="firstTab('popular') == true"> first</ww:if>"><a id="popular-filters-tab" title="<ww:text name="'managefilters.popular.desc'"/>" href="<%= request.getContextPath() %>/secure/ManageFilters.jspa?filterView=popular"><strong><ww:text name="'common.concepts.popular'"/></strong></a></li>
    </ww:if>
    <ww:if test="tabShowing('search') == true">
        <li id="mf_search_li" class="<ww:if test="filterView == 'search'">active</ww:if><ww:if test="firstTab('search') == true"> first</ww:if>"><a id="search-filters-tab" title="<ww:text name="'managefilters.search.desc'"/>" href="<%= request.getContextPath() %>/secure/ManageFilters.jspa?filterView=search"><strong><ww:text name="'common.concepts.search'"/></strong></a></li>
    </ww:if>
</ul>
