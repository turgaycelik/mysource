<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
<%--	<title>Setup Enterprise License </title>--%>
</head>

<body>

<ww:if test="dataCleaned == true">
    <page:applyDecorator name="jirapanel">
	    <page:param name="width">100%</page:param>
	    <page:param name="title">Data Clean</page:param>
        <page:param name="description">
            <font color=#cc0000><b>Illegal XML characters have been removed from your data. Please restart your server so that all necessary changes can take effect.
            <p>
            Changes will not take place until server is restarted.</b></font>
            <p>Click <a href="<%= request.getContextPath() %>/">here</a> when the server is restarted.
        </page:param>
    </page:applyDecorator>
</ww:if>
<ww:else>
    <page:applyDecorator name="jiraform">
        <page:param name="action">CleanData.jspa</page:param>
        <page:param name="submitId">clean_submit</page:param>
        <page:param name="submitName">Clean database</page:param>
        <page:param name="width">100%</page:param>
        <page:param name="title">Invalid XML characters</page:param>
        <page:param name="cancelURI"><%=request.getContextPath() %>/</page:param>
        <page:param name="description">
            <p>Your data contains control characters that are not allowed in XML content. To export the data these characters need to be removed.</p>
        </page:param>

        <ui:component label="'Admin User Name'" name="'userName'" template="userselect.jsp">
            <ui:param name="'formname'" value="'jiraform'" />
            <ui:param name="'imageName'" value="'userImage'"/>
            <ui:param name="'size'" value="40"/>
        </ui:component>

        <ui:component label="'Password'" name="'password'" template="password.jsp">
            <ui:param name="'size'">40</ui:param>
            <ui:param name="'description'">You must provide a valid Administrator username and password to proceed</ui:param>
        </ui:component>

    </page:applyDecorator>
</ww:else>
</p>

</body>
</html>
