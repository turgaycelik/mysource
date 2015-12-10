<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="sitemesh-decorator" prefix="decorator" %>

<ol class="steps">
 	<ww:if test="/rootBulkEditBean/singleMode == false">
	    <ww:if test="/rootBulkEditBean/currentStep == 1">
	        <li class="current"><ww:text name="'bulkedit.step1.title'"/></li>
	    </ww:if>
	    <ww:else>
	        <li>
	            <ww:if test="/rootBulkEditBean/availablePreviousStep(1) == true">
	                <ww:if test="/rootBulkEditBean/maxIssues > 0">
	                    <a id="bulkedit_chooseissues" href="BulkEdit1!default.jspa?currentStep=1&tempMax=<ww:property value="/rootBulkEditBean/maxIssues"/>"><ww:text name="'bulkedit.step1.title'"/></a>
	                </ww:if>
	                <ww:else>
	                    <a id="bulkedit_chooseissues" href="BulkEdit1!default.jspa?currentStep=1"><ww:text name="'bulkedit.step1.title'"/></a>
	                </ww:else>
	            </ww:if>
	            <ww:else>
	                <ww:text name="'bulkedit.step1.title'"/>
	            </ww:else>
	            <br />
	            <ww:text name="'bulkedit.step1.status'">
	                <ww:param name="'value0'"><strong><ww:property value="/rootBulkEditBean/selectedIssues/size"/></strong></ww:param>
	                <ww:param name="'value1'"><strong><ww:property value="/rootBulkEditBean/projectIds/size"/></strong></ww:param>
	            </ww:text>
	        </li>
	    </ww:else>
	
	    <ww:if test="/rootBulkEditBean/currentStep == 2">
	        <li class="current"><ww:text name="'bulkedit.step2.title'"/></li>
	    </ww:if>
	    <ww:elseIf test="/rootBulkEditBean/currentStep > 2">
	        <li>
	            <ww:if test="/rootBulkEditBean/availablePreviousStep(2) == true">
	                <a href="BulkChooseOperation!default.jspa"><ww:text name="'bulkedit.step2.title'"/></a>
	            </ww:if>
	            <ww:else>
	                <ww:text name="'bulkedit.step2.title'"/>
	            </ww:else>
	        </li>
	    </ww:elseIf>
	    <ww:else>
	        <li><ww:text name="'bulkedit.step2.title'"/></li>
	    </ww:else>
    </ww:if>
     
    <ww:if test="/rootBulkEditBean/currentStep == 3">
        <li class="current"><ww:text name="'bulkedit.step3.title'"/>
            <ww:if test="!@hideSubMenu">
                <ww:property value="/rootBulkEditBean/relatedMultiBulkMoveBean">
                    <ww:if test=".">
                        <ol class="steps">
                            <ww:iterator value="./bulkEditBeans" status="'status'">
                                <li<ww:if test="@status/index == ../currentBulkEditBeanIndex"> class="current" title="<ww:text name="'bulk.migrate.currently'" ><ww:param name="'value0'"><ww:property value="./key/project/string('name')" /></ww:param><ww:param name="'value1'"><ww:property value="./key/issueTypeObject/name" /></ww:param></ww:text>"</ww:if>>
                                    <ww:property value="./key/project/string('name')" /> - <ww:property value="./key/issueTypeObject/name" />
                                </li>
                            </ww:iterator>
                        </ol>
                    </ww:if>
                </ww:property>
            </ww:if>
        </li>
    </ww:if>
    <ww:elseIf test="/rootBulkEditBean/currentStep > 3">
        <li>
            <ww:if test="/rootBulkEditBean/availablePreviousStep(3) == true">
                <a href="<ww:property value="/operationDetailsActionName"/>"><ww:text name="'bulkedit.step3.title'"/></a>
            </ww:if>
            <ww:else>
               <ww:text name="'bulkedit.step3.title'"/>
            </ww:else>
        </li>
    </ww:elseIf>
    <ww:else>
        <li><ww:text name="'bulkedit.step3.title'"/></li>
    </ww:else>
    <ww:if test="/rootBulkEditBean/currentStep == 4">
        <li class="current"><ww:text name="'bulkedit.step4.title'"/></li>
    </ww:if>
    <ww:else>
        <li><ww:text name="'bulkedit.step4.title'"/></li>
    </ww:else>
</ol>