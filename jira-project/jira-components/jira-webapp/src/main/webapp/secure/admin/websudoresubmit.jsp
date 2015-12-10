<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>
<%@ taglib prefix="page" uri="sitemesh-page" %>
<html>
<head>
    <title><ww:text name="'websudo.title'"/> - <ww:text name="'websudo.retry.name'"/></title>
    <meta name="decorator" content="message" />
</head>
<body>
    <div class="form-body">
        <header>
            <h1><ww:text name="'websudo.title'"/> - <ww:text name="'websudo.retry.name'"/></h1>
        </header>
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">warning</aui:param>
            <aui:param name="'messageHtml'">
                <p><ww:text name="'websudo.retry.message'"/></p>
                <p><em><ww:text name="'xsrf.retry.note2'"/></em></p>
            </aui:param>
        </aui:component>

        <page:applyDecorator id="resubmit-form" name="auiform">
            <page:param name="action"><%= request.getContextPath() %><ww:property value="/webSudoDestination"/></page:param>
            <page:param name="method">post</page:param>

            <ww:iterator value="/requestParameters">
                <ww:iterator value="./value">
                        <ww:component name="../key" value="." template="hidden.jsp"/>
                </ww:iterator>
            </ww:iterator>
            <input type="submit" name="retry_button" value="<ww:text name="'websudo.retry.name'"/>"/>

        </page:applyDecorator>
    </div>
</body>
</html>