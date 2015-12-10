<%@ taglib uri="webwork" prefix="ww" %>
<ww:if test="!@selectWithTextArea">
<ww:property value="'true'" id="selectWithTextArea" />
<script language="JavaScript">
<!--
function changeSelectTextArea(fieldName)
{
    var selectList = document.getElementById(fieldName + "_select");
    var textBox = document.getElementById(fieldName);

    if ('OTHER_VALUE' == selectList.value)
    {
        textBox.style.display = '';
        textBox.select();
    }
    else
    {
        textBox.style.display = 'none';
        textBox.value = selectList.value;
    }
}
//-->
</script>
</ww:if>
   <%-- Check if an explicit selected value has been specified --%>
   <ww:if test="parameters['selectedValue']">
       <ww:property value="parameters['selectedValue']" id="selectedValue" />
   </ww:if>
   <ww:elseIf test="parameters['nameValue']">
       <ww:property value="parameters['nameValue']" id="selectedValue" />
   </ww:elseIf>
   <ww:else>
       <ww:property value="''" id="selectedValue" />
   </ww:else>

<select name="<ww:property value="parameters['name']"/>_select"
        id="<ww:property value="parameters['name']"/>_select"
        onchange="changeSelectTextArea('<ww:property value="parameters['name']"/>');"
      <ww:property value="parameters['disabled']">
         <ww:if test="{parameters['disabled']}">DISABLED</ww:if>
      </ww:property>
      <ww:property value="parameters['tabindex']">
         <ww:if test=".">tabindex="<ww:property value="."/>"</ww:if>
      </ww:property>
      <ww:property value="parameters['style']">
         <ww:if test=".">style="<ww:property value="."/>"</ww:if>
      </ww:property>
      <ww:property value="parameters['class']">
         <ww:if test=".">class="<ww:property value="."/>"</ww:if>
      </ww:property>
      <ww:property value="parameters['size']">
         <ww:if test=".">size="<ww:property value="."/>"</ww:if>
      </ww:property>
      <ww:property value="parameters['title']">
         <ww:if test=".">title="<ww:property value="."/>"</ww:if>
      </ww:property>
>
    <ww:property value="'false'" id="selectWithTextAreaSelected" />
    
   <ww:property value="parameters['headerrow']">
      <ww:if test=". && . != ''"><option
		value="<ww:property value="parameters['headervalue']" />"
		<ww:if test="../parameters['headervalue'] == @selectedValue">selected <ww:property value="'true'" id="selectWithTextAreaSelected" /></ww:if>
		><ww:property value="parameters['headerrow']" /></option></ww:if>
   </ww:property>
   <ww:property value="parameters['headerrow2']">
      <ww:if test=". && . != ''"><option
		value="<ww:property value="parameters['headervalue2']" />"
		<ww:if test="../parameters['headervalue2'] == @selectedValue">selected <ww:property value="'true'" id="selectWithTextAreaSelected" /></ww:if>
		><ww:property value="parameters['headerrow2']" /></option></ww:if>
   </ww:property>

   <%-- If it has, compare each value to the specified selected value and if they are equal print 'SELECTED' next to it --%>
   <ww:iterator value="parameters['list']">
	  <option value="<ww:property value="{parameters['listKey']}"/>" <ww:if test="{parameters['listKey']} == @selectedValue">SELECTED <ww:property value="'true'" id="selectWithTextAreaSelected" /></ww:if>
              <ww:property value="parameters['optionIcon']"><ww:if test="."> class="imagebacked" style="background-image: url(<ww:url value="../{.}" />);"</ww:if></ww:property>
              <ww:property value="parameters['optionTitle']"><ww:if test="."> title="<ww:property value="../{.}" />"</ww:if></ww:property>
              <ww:if test="parameters['showOptionId']">id="<ww:property value="parameters['name']" />_select_<ww:property value="{parameters['listKey']}" />"</ww:if>
              >
        <ww:if test="parameters['internat'] == true"><ww:text name="{parameters['listValue']}"/></ww:if>
        <ww:else><ww:property value="{parameters['listValue']}"/></ww:else>
      </option>
   </ww:iterator>
    <option value="OTHER_VALUE" <ww:if test="@selectWithTextAreaSelected != 'true' && @selectedValue && @selectedValue != ''">SELECTED</ww:if>><ww:text name="'common.words.other'" /></option>
</select>
<br />
<input id="<ww:property value="parameters['name']"/>" name="<ww:property value="parameters['name']"/>" type="text" value="<ww:property value="@selectedValue" />" />
<fieldset class="hidden parameters">
    <input type="hidden" title="paramName" value="<ww:property value="parameters['name']"/>"/>
</fieldset>
<script language="JavaScript">
<!--
    AJS.$(function() {
        changeSelectTextArea(AJS.params.paramName);
    });

//-->
</script>
