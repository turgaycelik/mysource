<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <title>Service Executor</title>
</head>
<body>
    <h1>Service Executor</h1>
    <page:applyDecorator name="jiraform">
	<table>
    	<thead>
            <tr>
                <th>ID</th>
                <th>Name</th>
                <th>ServiceClass</th>
            </tr>
        </thead>
        <ww:iterator value="/services">
            <tr>
                <td><ww:property value="./id"/></td>
                <td><ww:property value="./name"/></td>
                <td><ww:property value="./serviceClass"/></td>
            </tr>
        </ww:iterator>
		<tbody>
    		<page:param name="action">ServiceExecutor.jspa</page:param>
	    	<page:param name="submitId">schedule_submit</page:param>
	    	<page:param name="submitName">Schedule and Wait</page:param>
		    <page:param name="cancelURI">default.jsp</page:param>

    		<ui:textfield label="'Service ID'" name="'serviceId'" />
		<tbody>
	</table>
    </page:applyDecorator>
</body>
</html>
