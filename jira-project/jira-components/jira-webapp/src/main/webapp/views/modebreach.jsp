<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
	<title><ww:text name="'mode.breach.title'"/></title>
	<meta name="decorator" content="message" />
</head>

<body>
    <header>
	    <h1><ww:text name="'common.words.error'"/></h1>
    </header>
    <p>
    <ww:text name="'mode.breach.desc'"/>
    </p>
    <p>
    <ww:text name="'contact.admin.for.perm'">
        <ww:param name="'value0'"><ww:property value="administratorContactLink" escape="'false'"/></ww:param>
    </ww:text>
    </p>
</body>
</html>
