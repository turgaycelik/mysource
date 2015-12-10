<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <ww:if test="canDelete == true">
        <title><ww:text name="'deletefilter.title'"><ww:param name="'value0'"><ww:property value="filterName" /></ww:param></ww:text></title>
        <meta name="decorator" content="panel-general" />
    </ww:if>
    <ww:else>
        <title><ww:text name="'deletefilter.title.no.filter'" /></title>
        <meta name="decorator" content="message" />
    </ww:else>
</head>
<body>
<ww:if test="canDelete == true">
    <page:applyDecorator id="delete-filter" name="auiform">
        <page:param name="action"><ww:property value="./actionName"/>.jspa</page:param>
        <page:param name="id">delete-filter-confirm-form-<ww:property value="filterId" /></page:param>
        <page:param name="submitButtonName">Delete</page:param>
        <page:param name="showHint">true</page:param>
        <ww:property value="/hint('delete_filter')">
            <ww:if test=". != null">
                <page:param name="hint"><ww:property value="./text" escape="false" /></page:param>
                <page:param name="hintTooltip"><ww:property value="./tooltip" escape="false" /></page:param>
            </ww:if>
        </ww:property>
        <page:param name="submitButtonText"><ww:text name="'common.words.delete'"/></page:param>
        <page:param name="cancelLinkURI"><ww:url value="'/secure/ManageFilters.jspa'" atltoken="false"/></page:param>
        <page:param name="returnUrl"><ww:property value="./returnUrl"/></page:param>

        <aui:component template="formHeading.jsp" theme="'aui'">
            <aui:param name="'text'"><ww:text name="'deletefilter.title'"><ww:param name="'value0'"><ww:property value="filterName" /></ww:param></ww:text></aui:param>
            <aui:param name="'escape'" value="'false'" />
        </aui:component>

        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">warning</aui:param>
            <aui:param name="'titleText'"><ww:text name="'deletefilter.confirm'"/></aui:param>
            <aui:param name="'messageHtml'">
                <ww:if test="otherFavouriteCount == 1">
                    <p><ww:text name="'deletefilter.other.favourite.filter.one'"/></p>
                </ww:if>
                <ww:elseIf test="otherFavouriteCount > 1">
                    <p><ww:text name="'deletefilter.other.favourite.filter.many'"><ww:param name="'value0'"><ww:property value="otherFavouriteCount"/></ww:param></ww:text></p>
                </ww:elseIf>
                <ww:if test="subscriptionCount == 1">
                    <p><ww:text name="'deletefilter.view.subscriptions.one'">
                        <ww:param name="'value0'"><a href="<%= request.getContextPath() %>/secure/ViewSubscriptions.jspa?filterId=<ww:property value="filterId" />"></ww:param>
                        <ww:param name="'value1'"></a></ww:param>
                    </ww:text>
                    </p>
                </ww:if>
                <ww:elseIf test="subscriptionCount > 0">
                    <p>
                        <ww:text name="'deletefilter.view.subscriptions.many'">
                            <ww:param name="'value0'"><ww:property value="subscriptionCount"/></ww:param>
                            <ww:param name="'value1'"><a href="<%= request.getContextPath() %>/secure/ViewSubscriptions.jspa?filterId=<ww:property value="filterId" />"></ww:param>
                            <ww:param name="'value2'"></a></ww:param>
                        </ww:text>
                    </p>
                </ww:elseIf>
                <ww:else>
                    <p><ww:text name="'deletefilter.noSubs'"/></p>
                </ww:else>
            </aui:param>
        </aui:component>

        <aui:component name="'filterId'" template="hidden.jsp" theme="'aui'" />
        <aui:component name="'searchName'" template="hidden.jsp" theme="'aui'" />
        <aui:component name="'searchOwnerUserName'" template="hidden.jsp" theme="'aui'" />
        <aui:component name="'sortColumn'" template="hidden.jsp" theme="'aui'" />
        <aui:component name="'sortAscending'" template="hidden.jsp" theme="'aui'" />
        <aui:component name="'pagingOffset'" template="hidden.jsp" theme="'aui'" />
        <aui:component name="'totalResultCount'" template="hidden.jsp" theme="'aui'" />
    </page:applyDecorator>
</ww:if>
<ww:else>
    <div class="form-body">
        <header>
            <h1><ww:text name="'deletefilter.title.no.filter'" /></h1>
        </header>
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">warning</aui:param>
            <aui:param name="'messageHtml'">
                <p><ww:text name="'deletefilter.can.not.delete'"/></p>
            </aui:param>
        </aui:component>
    </div>
</ww:else>
</body>
</html>
