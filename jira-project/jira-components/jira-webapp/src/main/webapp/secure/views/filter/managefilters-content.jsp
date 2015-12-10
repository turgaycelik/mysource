<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="jiratags" prefix="jira" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%--  // --%>
<%--  // SEARCH RESULTS SECTION HERE--%>
<%-- // --%>
<ww:if test="filterView == 'search' && tabShowing('search') == true">
    <ww:if test="searchContentOnly == false">
        <ui:soy moduleKey="'jira.webresources:soy-templates'" template="'JIRA.Templates.Headers.pageHeader'">
            <ui:param name="'tagName'" value="'div'"/>
            <ui:param name="'mainContent'">
                <h2><ww:text name="'managefilters.search.desc'"/></h2>
            </ui:param>
            <ui:param name="'helpContent'">
                <ww:component template="help.jsp" name="'issue_filters'" />
            </ui:param>
        </ui:soy>

        <p><ww:text name="'managefilters.search.long.desc'"/></p>
        <page:applyDecorator id="filterSearchForm" name="auiform">
            <page:param name="action">ManageFilters.jspa</page:param>
            <page:param name="submitButtonName">Search</page:param>
            <page:param name="submitButtonText"><ww:text name="'common.concepts.search'"/></page:param>

            <ui:component name="filterView" value="/filterView" template="hidden.jsp" theme="'aui'"/>

            <page:applyDecorator name="auifieldgroup">
                <ui:textfield label="text('common.concepts.search')" name="'searchName'" theme="'aui'">
                    <ui:param name="'description'" value="text('filters.search.text.desc')"/>
                </ui:textfield>
            </page:applyDecorator>

            <page:applyDecorator name="auifieldgroup">
                <ui:component label="text('admin.common.words.owner')" name="'searchOwnerUserName'" template="singleSelectUserPicker.jsp" theme="'aui'">
                    <ui:param name="'description'" value="text('user.picker.ajax.desc')"/>
                    <ww:if test="searchOwnerUserName">
                        <ui:param name="'userName'" value="searchOwnerUserName"/>
                        <ui:param name="'userAvatar'"><ww:url value="'/secure/useravatar'" atltoken="'false'"><ww:param name="'ownerId'" value="searchOwnerUserName"/></ww:url></ui:param>
                        <ui:param name="'userFullName'"><jira:formatuser userName="searchOwnerUserName" type="'fullName'" escape="false" /></ui:param>
                    </ww:if>
                </ui:component>
            </page:applyDecorator>

            <ww:if test="/userLoggedIn == true">
                <page:applyDecorator name="auifieldgroup">
                    <ww:component name="'shares'" label="text('common.concepts.shared.with')" template="select-share-types.jsp" theme="'aui'">
                        <ww:param name="'class'" value="'filterSearchInputRightAligned fieldLabelArea'"/>
                        <ww:param name="'valueColSpan'" value="3"/>
                        <ww:param name="'noJavaScriptMessage'" value="text('common.sharing.no.share.javascript')"/>
                        <ww:param name="'shareTypeList'" value="/filtersViewHelper/shareTypeRendererBeans"/>
                        <ww:param name="'dataString'" value="/filtersViewHelper/searchShareTypeJSON"/>
                        <ww:param name="'anyDescription'"><ww:text name="'common.sharing.search.template.any.desc.SearchRequest'"/></ww:param>
                    </ww:component>
                </page:applyDecorator>
            </ww:if>
            <ww:else>
                <ui:component template="multihidden.jsp" >
                    <ui:param name="'fields'">searchShareType,groupShare,projectShare,roleShare</ui:param> <%-- TODO: why not use the back end ShareType* shit to get these? --%>
                </ui:component>
            </ww:else>
        </page:applyDecorator>

        <div id="filter_search_results">
    </ww:if>
    <ww:if test="/searchRequested == true && /filters/size > 0">
        <ww:component name="text('common.concepts.search')" template="filter-list.jsp">
            <ww:param name="'id'" value="'mf_browse'"/>
            <ww:param name="'filterList'" value="/filters"/>
            <ww:param name="'operations'">false</ww:param>
            <ww:param name="'shares'" value="true"/>
            <ww:param name="'favourite'" value="/canShowFavourite"/>

            <ww:param name="'sort'" value="true"/>
            <ww:param name="'sortColumn'" value="/sortColumn"/>
            <ww:param name="'viewHelper'" value="/filtersViewHelper"/>
            <ww:param name="'linkRenderer'" value="/filterLinkRenderer"/>
            <ww:param name="'issuecount'" value="false"/>

            <ww:param name="'paging'" value="true"/>
            <ww:param name="'pagingMessage'">
                <ww:text name="'common.sharing.searching.results.message'">
                    <ww:param name="'value0'"><ww:property value="/startPosition"/></ww:param>
                    <ww:param name="'value1'"><ww:property value="/endPosition"/></ww:param>
                    <ww:param name="'value2'"><ww:property value="/totalResultCount"/></ww:param>
                </ww:text>
            </ww:param>
            <ww:param name="'pagingPrevUrl'" value="/previousUrl"/>
            <ww:param name="'pagingNextUrl'" value="/nextUrl"/>
            <ww:param name="'emptyMessage'"><ww:text name="/searchEmptyMessageKey"/></ww:param>
        </ww:component>
    </ww:if>
    <ww:else>
        <ww:if test="/searchRequested == true">
            <aui:component template="auimessage.jsp" theme="'aui'">
                <aui:param name="'messageType'">info</aui:param>
                <aui:param name="'messageHtml'">
                    <p><ww:text name="/searchEmptyMessageKey"/></p>
                </aui:param>
            </aui:component>
        </ww:if>
    </ww:else>
    <ww:if test="searchContentOnly == false">
        </div>
    </ww:if>
</ww:if>
<%--//--%>
<%--// POPULAR RESULTS SECTION HERE--%>
<%--//--%>
<ww:elseIf test="filterView == 'popular' && tabShowing('popular') == true">
    <ui:soy moduleKey="'jira.webresources:soy-templates'" template="'JIRA.Templates.Headers.pageHeader'">
        <ui:param name="'tagName'" value="'div'"/>
        <ui:param name="'mainContent'">
            <h2><ww:text name="'managefilters.popular.desc'"/></h2>
        </ui:param>
        <ui:param name="'helpContent'">
            <ww:component template="help.jsp" name="'issue_filters'">
                <ww:param name="'helpURLFragment'">#managing_filters</ww:param>
            </ww:component>
        </ui:param>
    </ui:soy>

    <p><ww:text name="'managefilters.popular.long.desc'"/></p>
    <ww:component name="text('common.concepts.popular')" template="filter-list.jsp">
        <ww:param name="'id'" value="'mf_popular'"/>
        <ww:param name="'filterList'" value="/filters"/>
        <ww:param name="'operations'">false</ww:param>
        <ww:param name="'shares'" value="true"/>
        <ww:param name="'favourite'" value="/canShowFavourite"/>

        <ww:param name="'sort'" value="false"/>
        <ww:param name="'sortColumn'" value="/sortColumn"/>
        <ww:param name="'viewHelper'" value="/filtersViewHelper"/>
        <ww:param name="'linkRenderer'" value="/filterLinkRenderer"/>
        <ww:param name="'issuecount'" value="false"/>

        <ww:param name="'paging'" value="false"/>
        <ww:param name="'emptyMessage'"><ww:text name="'filters.no.popular'"/></ww:param>
        <ww:param name="'viewHelper'" value="/filtersViewHelper"/>
    </ww:component>
</ww:elseIf>
<%--//--%>
<%--// MY RESULTS SECTION HERE--%>
<%--//--%>
<ww:elseIf test="filterView == 'my' && tabShowing('my') == true">
    <ui:soy moduleKey="'jira.webresources:soy-templates'" template="'JIRA.Templates.Headers.pageHeader'">
        <ui:param name="'tagName'" value="'div'"/>
        <ui:param name="'mainContent'">
            <h2><ww:text name="'managefilters.my.desc'"/></h2>
        </ui:param>
        <ui:param name="'helpContent'">
            <ww:component template="help.jsp" name="'issue_filters'">
                <ww:param name="'helpURLFragment'">#managing_filters</ww:param>
            </ww:component>
        </ui:param>
    </ui:soy>

    <p><ww:text name="'managefilters.my.long.desc'"/></p>
    <ww:component name="text('managefilters.my')" template="filter-list.jsp">
        <ww:param name="'id'" value="'mf_owned'"/>
        <ww:param name="'filterList'" value="/filters"/>
        <ww:param name="'owner'">false</ww:param>
        <ww:param name="'favcount'">false</ww:param>
        <ww:param name="'favourite'" value="/canShowFavourite"/>
        <ww:param name="'shares'" value="true"/>
        <ww:param name="'emptyMessage'"><ww:text name="'filters.no.owned.filters'"/></ww:param>
        <ww:param name="'viewHelper'" value="/filtersViewHelper"/>
        <ww:param name="'linkRenderer'" value="/filterLinkRenderer"/>
        <ww:param name="'issuecount'" value="false"/>
        <ww:param name="'operations'">true</ww:param>
        <ww:param name="'dropDownModelProvider'" value="/"/>
    </ww:component>
</ww:elseIf>
<%--// --%>
<%--// FAVOURITE RESULTS SECTION HERE--%>
<%--//--%>
<ww:elseIf test="filterView == 'favourites' && tabShowing('favourites') == true">
    <ui:soy moduleKey="'jira.webresources:soy-templates'" template="'JIRA.Templates.Headers.pageHeader'">
        <ui:param name="'tagName'" value="'div'"/>
        <ui:param name="'mainContent'">
            <h2><ww:text name="'managefilters.favourite.desc'"/></h2>
        </ui:param>
        <ui:param name="'helpContent'">
            <ww:component template="help.jsp" name="'issue_filters'">
                <ww:param name="'helpURLFragment'">#managing_filters</ww:param>
            </ww:component>
        </ui:param>
    </ui:soy>

    <p><ww:text name="'managefilters.favourite.long.desc'"/></p>
    <aui:component id="'undo_div'" template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">warning</aui:param>
        <aui:param name="'cssClass'">hidden</aui:param>
    </aui:component>
    <ww:component name="text('common.favourites.favourite')" template="filter-list.jsp">
        <ww:param name="'id'" value="'mf_favourites'"/>
        <ww:param name="'filterList'" value="/filters"/>
        <ww:param name="'favcount'">false</ww:param>
        <ww:param name="'remove'">true</ww:param>
        <ww:param name="'shares'" value="true"/>
        <ww:param name="'favourite'" value="/canShowFavourite"/>
        <ww:param name="'emptyMessage'"><ww:text name="'filters.no.favourite'"/></ww:param>
        <ww:param name="'viewHelper'" value="/filtersViewHelper"/>
        <ww:param name="'linkRenderer'" value="/filterLinkRenderer"/>
        <ww:param name="'issuecount'" value="false"/>
        <ww:param name="'operations'">true</ww:param>
        <ww:param name="'dropDownModelProvider'" value="/"/>
    </ww:component>
</ww:elseIf>
<ww:else>
    <aui:component template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">warning</aui:param>
        <aui:param name="'messageHtml'">
            <p><ww:text name="'filters.no.tab.permssion'"/></p>
        </aui:param>
    </aui:component>
</ww:else>
