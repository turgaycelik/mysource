
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
	<title><ww:text name="'admin.mailservers.delete.mail.server'"/></title>
    <meta name="admin.active.section" content="admin_system_menu/top_system_section/mail_section"/>
    <meta name="admin.active.tab" content="<ww:property value='./activeTab'/>"/>
</head>

<body>

    <page:applyDecorator name="jiraform">
        <page:param name="action">DeleteMailServer.jspa</page:param>
		<page:param name="autoSelectFirst">false</page:param>
        <page:param name="submitId">delete_submit</page:param>
        <page:param name="submitName"><ww:text name="'common.words.delete'"/></page:param>
    	<page:param name="cancelURI"><ww:property value='./cancelURI'/></page:param>
        <page:param name="title"><ww:text name="'admin.mailservers.delete.mail.server'"/></page:param>
        <page:param name="width">100%</page:param>
	    <page:param name="description">
        <input type="hidden" name="id" value="<ww:property value="id" />">
        <input type="hidden" name="confirmed" value="true">

        <ww:text name="'admin.mailservers.delete.confirmation'">
            <ww:param name="'value0'"><b><ww:property value="name" /></b></ww:param>
        </ww:text>
        </page:param>
    </page:applyDecorator>

</body>
</html>
