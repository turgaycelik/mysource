<%@ taglib uri="webwork" prefix="ww" %>
<jsp:include page="/template/standard/controlheader.jsp" />

<ww:if test="!@summaryCounter">
    <ww:property value="0" id="summaryCounter" />
</ww:if>

<ww:property value="parameters['summary']" id="summary" />
<fieldset class="hidden parameters">
    <ww:property value="parameters['headerrow']">
        <ww:if test=". && . != ''">
            <input type="hidden" class="list" title="summaries" value="<ww:property value="parameters['headersummary']"/>"/>
        </ww:if>
    </ww:property>
    <ww:property value="parameters['headerrow2']">
        <ww:if test=". && . != ''">
            <input type="hidden" class="list" title="summaries" value="<ww:property value="parameters['headersummary2']"/>"/>
        </ww:if>
    </ww:property>
    <ww:iterator value="parameters['list']">
        <input type="hidden" class="list" title="summaries" value="<ww:property value="{parameters['summary']}"/>"/>
    </ww:iterator>
    <input type="hidden" title="paramName" value="<ww:property value="parameters['name']"/>"/>
</fieldset>
<%-- Is this still used?! --%>
<ww:if test="@summary">
<%
    Integer counter = (Integer) request.getAttribute("summaryCounter");
    if (counter != null)
    {
        int i = counter.intValue();
        int value =  i + 1;
        request.setAttribute("summaryCounter", new Integer(value));
    }
%>

<script type="text/javascript">
    function changeDescription<ww:property value="@summaryCounter" />(dropdown) {
        document.getElementById(AJS.params.paramName + "_summary").innerHTML = AJS.params.summaries[dropdown.selectedIndex];
    }

    AJS.$(function() {
        changeDescription<ww:property value="@summaryCounter" />(document.getElementById(AJS.params.paramName + "_select"));
    });
</script>
</ww:if>

<select name="<ww:property value="parameters['name']"/>"
        id="<ww:property value="parameters['name']"/>_select"
      <ww:property value="parameters['disabled']">
         <ww:if test="{parameters['disabled']}">DISABLED</ww:if>
      </ww:property>
      <ww:property value="parameters['tabindex']">
         <ww:if test=".">tabindex="<ww:property value="."/>"</ww:if>
      </ww:property>
      <ww:property value="parameters['onchange']">
         <ww:if test=".">onchange="<ww:property value="."/>"</ww:if>
         <ww:elseIf test="@summary"> onchange="changeDescription<ww:property value="@summaryCounter" />(this);"</ww:elseIf>
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
   <ww:property value="parameters['headerrow']">
      <ww:if test=". && . != ''"><option
		value="<ww:property value="parameters['headervalue']" />"
		<ww:if test="../parameters['headervalue'] == ../parameters['nameValue']">selected</ww:if>
		><ww:property value="parameters['headerrow']" /></option></ww:if>
   </ww:property>
   <ww:property value="parameters['headerrow2']">
      <ww:if test=". && . != ''"><option
		value="<ww:property value="parameters['headervalue2']" />"
		<ww:if test="../parameters['headervalue2'] == ../parameters['nameValue']">selected</ww:if>
		><ww:property value="parameters['headerrow2']" /></option></ww:if>
   </ww:property>

   <%-- Check if an explicit selected value has been specified --%>
   <ww:if test="parameters['selectedValue']">
       <ww:property value="parameters['selectedValue']" id="selectedValue" />
   </ww:if>
   <ww:else>
       <ww:property value="parameters['nameValue']" id="selectedValue" />
   </ww:else>

   <%-- If it has, compare each value to the specified selected value and if they are equal print 'SELECTED' next to it --%>
   <ww:iterator value="parameters['list']">
	  <option value="<ww:property value="{parameters['listKey']}"/>" <ww:if test="{parameters['listKey']} == @selectedValue">SELECTED</ww:if>
              <ww:property value="parameters['optionIcon']"><ww:if test="."> class="imagebacked" style="background-image: url(<ww:url value="../{.}" />);"</ww:if></ww:property>
              <ww:property value="parameters['optionTitle']"><ww:if test="."> title="<ww:property value="../{.}" />"</ww:if></ww:property>
              <ww:if test="parameters['showOptionId']">id="<ww:property value="parameters['name']" />_select_<ww:property value="{parameters['listKey']}" />"</ww:if>
              >
        <ww:if test="parameters['internat'] == true"><ww:text name="{parameters['listValue']}"/></ww:if>
        <ww:else><ww:property value="{parameters['listValue']}"/></ww:else>
      </option>
   </ww:iterator>
</select>

<ww:if test="@summary && !parameters['description']"><br /></ww:if>
<span class="selectDescription" id="<ww:property value="parameters['name']"/>_summary"></span>
<jsp:include page="/template/standard/controlfooter.jsp" />
