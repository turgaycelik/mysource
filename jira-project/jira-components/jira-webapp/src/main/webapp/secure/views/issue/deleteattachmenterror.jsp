<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="jiratags" prefix="jira" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <title><ww:text name="'common.words.error'"/></title>
    <meta name="decorator" content="message" />
</head>
<body>
    <div class="form-body">
        <header>
            <h1><ww:text name="'common.words.error'"/></h1>
        </header>
        <ww:if test="hasErrorMessages == 'true'">
            <aui:component template="auimessage.jsp" theme="'aui'">
                <aui:param name="'messageType'">error</aui:param>
                <aui:param name="'messageHtml'">
                    <ww:iterator value="flushedErrorMessages">
                        <p><ww:property value="." /></p>
                    </ww:iterator>
                </aui:param>
            </aui:component>
        </ww:if>
        <%-- TODO: SEAN do we need an else here if no messages? --%>
    </div>
</body>
</html>