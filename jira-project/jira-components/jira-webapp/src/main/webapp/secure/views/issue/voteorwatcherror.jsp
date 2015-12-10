<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ taglib uri="jiratags" prefix="jira" %>
<%-- Triggered mainly by trying to vote on an issue you reported or changing a vote on a resolved issue --%>
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
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">error</aui:param>
            <aui:param name="'messageHtml'">
                <ww:if test="hasErrorMessages == 'true'">
                    <ww:iterator value="flushedErrorMessages">
                        <p><ww:property value="." /></p>
                    </ww:iterator>
                </ww:if>
            </aui:param>
        </aui:component>
    </div>
</body>
</html>
