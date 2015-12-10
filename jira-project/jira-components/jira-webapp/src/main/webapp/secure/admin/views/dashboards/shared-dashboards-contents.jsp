<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<ww:if test="/pages && /pages/size > 0">
    <ww:component name="text('common.concepts.search')" template="portalpage-list.jsp">
            <ww:param name="'id'" value="'pp_browse'"/>
            <ww:param name="'portalPageList'" value="/pages"/>
            <ww:param name="'ordering'">false</ww:param>
            <ww:param name="'favourite'" value="false"/>
            <ww:param name="'usesimplefavcount'" value="true"/>
            <ww:param name="'sort'" value="true"/>
            <ww:param name="'sortColumn'" value="/sortColumn"/>
            <ww:param name="'viewHelper'" value="/portalPageViewHelper"/>
            <ww:param name="'linkRenderer'" value="/portalPageLinkRenderer"/>

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
        <ww:param name="'operations'">true</ww:param>
        <ww:param name="'includeOperationsId'">true</ww:param>
        <ww:param name="'dropDownModelProvider'" value="/"/>
        <ww:param name="'adminView'" value="true"/>
    </ww:component>
</ww:if>
<ww:else>
    <aui:component template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">info</aui:param>
        <aui:param name="'messageHtml'">
            <p><ww:text name="/searchEmptyMessageKey"/></p>
        </aui:param>
    </aui:component>
</ww:else>
