<%@ taglib uri="webwork" prefix="ww" %>
<input type="text"
       name="<ww:property value="parameters['namePrevious']"/>"
      <ww:property value="parameters['sizePrevious']">
         <ww:if test=".">size="<ww:property value="."/>"</ww:if>
      </ww:property>
      <ww:property value="parameters['maxlengthPrevious']">
         <ww:if test=".">maxlength="<ww:property value="."/>"</ww:if>
      </ww:property>
      <ww:property value="parameters['valuePrevious']">
         <ww:if test=".">value="<ww:property value="."/>"</ww:if>
      </ww:property>
      <ww:property value="parameters['disabledPrevious']">
         <ww:if test="{parameters['disabledPrevious']}">DISABLED</ww:if>
      </ww:property>
      <ww:property value="parameters['readonlyPrevious']">
         <ww:if test="{parameters['readonlyPrevious']}">READONLY</ww:if>
      </ww:property>
      <ww:property value="parameters['onkeyupPrevious']">
         <ww:if test=".">onkeyup="<ww:property value="."/>"</ww:if>
      </ww:property>
      <ww:property value="parameters['tabindexPrevious']">
         <ww:if test=".">tabindex="<ww:property value="."/>"</ww:if>
      </ww:property>
      <ww:property value="parameters['onchangePrevious']">
         <ww:if test=".">onchange="<ww:property value="."/>"</ww:if>
      </ww:property>
      <ww:property value="parameters['stylePrevious']">
         <ww:if test=".">style="<ww:property value="."/>"</ww:if>
      </ww:property>
>
