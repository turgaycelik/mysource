<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <title><ww:text name="'resetpassword.title'"/></title>
    <meta name="decorator" content="message" />
</head>
<body>
<page:applyDecorator name="jiraform">
    <page:param name="title"><ww:text name="'resetpassword.title'"/></page:param>
    <page:param name="width">100%</page:param>

    <page:param name="description">
        <ww:if test="/remoteUser == null">
            <ww:text name="'resetpassword.well.done.not.logged.in'">
                <ww:param name="'value0'"><a href="<%=request.getContextPath()%>/login.jsp"></ww:param>
                <ww:param name="'value1'"></a></ww:param>
            </ww:text>
        </ww:if>
        <ww:else>
            <ww:text name="'resetpassword.well.done.logged.in'">
                <ww:param name="'value0'"><a href="<%=request.getContextPath()%>/login.jsp"></ww:param>
                <ww:param name="'value1'"></a></ww:param>
            </ww:text>
        </ww:else>
    </page:param>
</page:applyDecorator>
</body>
</html>
