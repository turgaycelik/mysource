<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="aui" %>
<html>
<head>
	<title><ww:text name="'project.does.not.exist.title'"/></title>
    <meta name="decorator" content="message" />
</head>
<body>
    <div class="form-body">
        <header>
            <h1><ww:text name="'project.does.not.exist.title'"/></h1>
        </header>
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">error</aui:param>
            <aui:param name="'iconText'"><ww:text name="'common.words.error'"/></aui:param>
            <aui:param name="'messageHtml'">
                <p>
                    <ww:text name="'project.does.not.exist.desc'">
                        <ww:param name="'value0'"><a href="<%= request.getContextPath()%>/secure/BrowseProjects.jspa#all"></ww:param>
                        <ww:param name="'value1'"></a></ww:param>
                    </ww:text>
                </p>
                <p>
                    <ww:text name="'contact.admin.for.perm'">
                        <ww:param name="'value0'"><%= request.getAttribute("administratorContactLink")%></ww:param>
                    </ww:text>
                </p>
            </aui:param>
        </aui:component>
    </div>
</body>
</html>