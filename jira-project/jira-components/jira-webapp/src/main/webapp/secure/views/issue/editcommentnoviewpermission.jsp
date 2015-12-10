<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="aui" %>
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
        <ww:if test="/issueValid == true">
            <aui:component template="auimessage.jsp" theme="'aui'">
                <aui:param name="'messageType'">error</aui:param>
                <aui:param name="'messageHtml'">
                    <p><ww:text name="'comment.service.error.no.comment.visibility.no.user'"/></p>
                </aui:param>
            </aui:component>
        </ww:if>
        <ww:elseIf test="/hasErrorMessages == true">
            <aui:component template="auimessage.jsp" theme="'aui'">
                <aui:param name="'messageType'">error</aui:param>
                <aui:param name="'messageHtml'">
                    <ww:iterator value="flushedErrorMessages">
                        <p><ww:property /></p>
                    </ww:iterator>
                </aui:param>
            </aui:component>
        </ww:elseIf>
    </div>
</body>
</html>