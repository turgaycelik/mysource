<%@ taglib uri="webwork" prefix="ww" %>
<ww:if test="./archived == true">
    <font color="#999999"><ww:property value="name" /></font>
</ww:if>
<ww:else>
    <ww:property value="name" />
</ww:else>
