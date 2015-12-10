<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="aui" %>
<html>
<head>
	<title><ww:text name="'session.timeout.message.title'"/></title>
    <meta name="decorator" content="message" />
</head>
<body>
    <div class="form-body">
        <header>
            <h1><ww:text name="'session.timeout.message.title'"/></h1>
        </header>
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">warning</aui:param>
            <aui:param name="'messageHtml'">
                <p><ww:property value="/errorMessage"/></p>
            </aui:param>
        </aui:component>
    </div>
</body>
</html>
