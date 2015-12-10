<%@ taglib prefix="ww" uri="webwork" %>

<html>
<head><title><ww:text name="'attachfile.title.multiple'"/></title></head>
<body>
<textarea>
    <ww:if test="/hasAnyErrors == false">
        {"id":"<ww:property value="/temporaryAttachment/id"/>", "name":"<ww:property value="/encode(/temporaryAttachment/filename)" />"}
    </ww:if>
    <ww:else>
        {"errorMsg":"<ww:property value="/encode(/errorMessage)"/>" }
    </ww:else>
</textarea>
</body>
</html>