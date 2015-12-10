<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'admin.jellyrunner.import.workflow.from.xml'"/></title>
    <meta name="admin.active.section" content="admin_system_menu/advanced_menu_section/advanced_section"/>
    <meta name="admin.active.tab" content="jelly_runner"/>
</head>
<body>
<page:applyDecorator name="jirapanel">
	<page:param name="title"><ww:text name="'admin.jellyrunner.import.workflow.from.xml'"/></page:param>
	<page:param name="description">
        <ww:if test="result">
            <p><ww:text name="'admin.jellyrunner.output'"/></p>
            <ww:property value="result" escape="false"/>
        </ww:if>
        <ww:else>
            <p><ww:text name="'admin.jellyrunner.success'"/></p>
        </ww:else>
    </page:param>
</page:applyDecorator>
</body>
</html>
