<%@ taglib uri="jiratags" prefix="jira" %>
<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>
<%@ taglib prefix="page" uri="sitemesh-page" %>
<html>
<head>
    <title><ww:text name="'createissue.cant.browse.converted.issue.title'"/></title>
    <meta name="decorator" content="message" />
</head>
<body>
    <div class="form-body">
        <header>
            <h1><ww:text name="'createissue.cant.browse.converted.issue.title'"/></h1>
        </header>
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">warning</aui:param>
            <aui:param name="'messageHtml'">
                <p>
                    <ww:text name="'createissue.cant.browse.converted.issue.description'">
                        <ww:param name="'value0'"><ww:property value="/issueKey"/></ww:param>
                    </ww:text>
                </p>
                <ww:if test="remoteUser == null">
                    <p>
                        <ww:text name="'login.required.desc2'">
                            <ww:param name="'value0'"><jira:loginlink><ww:text name="'common.words.login'"/></jira:loginlink></ww:param>
                        </ww:text>
                        <ww:if test="/allowSignUp == true">
                            <ww:text name="'login.required.desc3'">
                                <ww:param name="'value0'"><a href="<%= request.getContextPath() %>/secure/Signup!default.jspa"></ww:param>
                                <ww:param name="'value1'"></a></ww:param>
                            </ww:text>
                        </ww:if>.
                    </p>
                </ww:if>
                <p>
                    <ww:text name="'contact.admin.for.perm'">
                        <ww:param name="'value0'"><ww:property value="administratorContactLink" escape="'false'"/></ww:param>
                    </ww:text>
                </p>
            </aui:param>
        </aui:component>
    </div>
</body>
</html>
