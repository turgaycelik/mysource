<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'invalid.workflow.action.title'"/></title>
    <meta name="decorator" content="message" />
</head>
<body>
    <div class="form-body">
        <header>
            <h1><ww:text name="'invalid.workflow.action.title'"/></h1>
        </header>
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">warning</aui:param>
            <aui:param name="'messageHtml'">
                <p>
                    <ww:text name="'invalid.workflow.action.desc'" >
                        <ww:param name="'value0'"><a id="refreshIssue" href="<%=request.getContextPath()%>/browse/<ww:property value="issueObject/key" />"><ww:text name="'invalid.workflow.action.link.text'"/></a></ww:param>
                        <ww:param name="'value1'"><ww:component name="'status'" template="issuestatus.jsp" theme="'aui'">
                                <ww:param name="'issueStatus'" value="issueObject/statusObject"/>
                                <ww:param name="'isSubtle'" value="false"/>
                                <ww:param name="'isCompact'" value="false"/>
                            </ww:component></ww:param>
                        <ww:param name="'value2'"><ww:property value="workflowTransitionDisplayName" /></ww:param>
                    </ww:text>
                </p>
            </aui:param>
        </aui:component>
    </div>
</body>
</html>
