<%--
  -- WebWork, Web Application Framework
  --
  -- Distributable under LGPL license.
  -- See terms of license at opensource.org
  --
  --
  -- tabbedpane.jsp	
  --
  -- Required Parameters:
  --   * contentName      - The name of the data map to be used.  
  --
  -- Optional Parameters:
  --   * tabAlign 	-	 The Alignment of the tabs. Default is the CENTER of the control.
  --   * id  				- 	 Id of the control.
  --
  --%>

<%@ taglib uri="webwork" prefix="ww" %>

<ww:bean name="'webwork.util.Counter'" id="tabIndex">
	<ww:param name="'first'" value="0"/>
	<ww:param name="'last'" value="content/size"/>
</ww:bean>
			
<table border="1" cellspacing="0" cellpadding="5" id="<ww:property value="id"/>">

	<tr valign="bottom" align="<ww:property value="tabAlign"/>">

		<ww:if test="tabAlign == 'CENTER' || tabAlign == 'RIGHT'"><th colspan ="1" width="*"></th></ww:if>

		<ww:iterator value="content">
			<th width="10%"
				<ww:if id="isCur" test="selectedIndex == @tabIndex/current">bgcolor="#A0B3FC"</ww:if>
				<ww:else>bgcolor="#C0C0C0"</ww:else>>
				<a href="<ww:url><ww:param name="indexLink" value="@tabIndex/next"/></ww:url>">
				<ww:if test="@isCur == true"><em></ww:if>
					<ww:property value="key"/>
				<ww:if test="@isCur == true"></em></ww:if>
				</a>
			</th>		
		</ww:iterator>
		
		<ww:if test="tabAlign == 'CENTER' || tabAlign == 'LEFT'"><th colspan ="1" width="*"></th></ww:if>
	
	</tr>
	<tr>
		<td bgcolor="#E1EAE8" colspan="<ww:property value="colSpanLength"/>" width="100%">
			<ww:include value="selectedUrl"/>
		</td>
	</tr>
</table>
