<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>
<%@ taglib prefix="page" uri="sitemesh-page" %>
<html>
<head>
    <ww:if test="/canDelete == true">
        <title><ww:text name="'portal.deletepage.title'"><ww:param name="'value0'"><ww:property value="pageName" /></ww:param></ww:text></title>
        <meta name="decorator" content="panel-general" />
    </ww:if>
    <ww:else>
        <title><ww:text name="'common.words.error'"/></title>
        <meta name="decorator" content="message" />
    </ww:else>
</head>
<body>
<ww:if test="/canDelete == true">
    <page:applyDecorator id="delete-portal-page" name="auiform">
        <page:param name="action"><ww:property value="./actionName"/>.jspa</page:param>
        <page:param name="cssClass">delete-portal-page-<ww:property value="pageId" /></page:param>
        <page:param name="submitButtonName">Delete</page:param>
        <page:param name="submitButtonText"><ww:text name="'common.words.delete'"/></page:param>
        <%-- This cancelLinkURI is only used when you are coming from the manage dashboards page, the actual
        dashboard pages override this value by appending a magical returnUrl parameter to the link to open this page --%>
        <page:param name="cancelLinkURI"><ww:url value="'/secure/ConfigurePortalPages!default.jspa'" atltoken="false"/></page:param>
        <aui:component name="'targetUrl'" value="/targetUrl" template="hidden.jsp" theme="'aui'"/>

        <aui:component template="formHeading.jsp" theme="'aui'">
            <aui:param name="'text'"><ww:text name="'portal.deletepage.title'"><ww:param name="'value0'"><ww:property value="pageName" /></ww:param></ww:text></aui:param>
            <aui:param name="'escape'" value="'false'" />
        </aui:component>

        <aui:component template="formDescriptionBlock.jsp" theme="'aui'">
            <aui:param name="'messageHtml'">
                <p><ww:text name="'portal.deletepage.confirm.desc'"/></p>
                <ww:if test="otherFavouriteCount > 0">
                    <p id="otherFavouriteCount">
                        <ww:if test="otherFavouriteCount == 1">
                            <ww:text name="'portal.deletepage.other.favourite.filter.one'"/>
                        </ww:if>
                        <ww:elseIf test="otherFavouriteCount > 1">
                            <ww:text name="'portal.deletepage.other.favourite.filter.many'">
                                <ww:param name="'value0'"><ww:property value="otherFavouriteCount"/></ww:param>
                            </ww:text>
                        </ww:elseIf>
                    </p>
                </ww:if>
            </aui:param>
        </aui:component>
    <aui:component name="'searchName'" template="hidden.jsp" theme="'aui'" />
    <aui:component name="'searchOwnerUserName'" template="hidden.jsp" theme="'aui'" />
    <aui:component name="'sortColumn'" template="hidden.jsp" theme="'aui'" />
    <aui:component name="'sortAscending'" template="hidden.jsp" theme="'aui'" />
    <aui:component name="'pagingOffset'" template="hidden.jsp" theme="'aui'" />
    <aui:component name="'totalResultCount'" template="hidden.jsp" theme="'aui'" />
    <aui:component name="'pageId'" template="hidden.jsp" theme="'aui'" />

    </page:applyDecorator>
</ww:if>
<ww:else>
    <div class="form-body">
        <header>
            <h1><ww:text name="'common.words.error'"/></h1>
        </header>
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">error</aui:param>
            <aui:param name="'titleText'"><ww:text name="'portal.deletepage.can.not.delete'" /></aui:param>
            <aui:param name="'messageHtml'">
                <ww:if test="hasErrorMessages == 'true'">
                    <ww:iterator value="flushedErrorMessages">
                        <p><ww:property value="." /></p>
                    </ww:iterator>
                </ww:if>
            </aui:param>
        </aui:component>
    </div>
</ww:else>
</body>
</html>
