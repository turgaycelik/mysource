<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="aui" %>
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
                <p>
                    <ww:if test="versions/size <= 0 && styleNames/size <=0">
                        <ww:text name="'releasenotes.generate.note'"/>
                    </ww:if>
                    <ww:elseIf test="versions/size <= 0">
                        <ww:text name="'releasenotes.generate.versions'"/>
                    </ww:elseIf>
                    <ww:elseIf test="styleNames/size <= 0">
                        <ww:text name="'releasenotes.generate.styles'"/>
                    </ww:elseIf>
                </p>
            </aui:param>
        </aui:component>
    </div>
</body>
</html>
