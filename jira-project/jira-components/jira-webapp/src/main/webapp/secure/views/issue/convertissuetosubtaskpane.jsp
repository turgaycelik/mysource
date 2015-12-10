<%@ taglib uri="webwork" prefix="ww" %>

    <ol class="steps">
        <ww:if test="currentStep == 1">
            <li class="current"><ww:text name="textKey('step1.title')"/></li>
        </ww:if>
        <ww:elseIf test="currentStep > 1">
            <li class="done">
                <a href="<ww:property value="/actionPrefix"/>.jspa?id=<ww:property value="issue/id"/>&guid=<ww:property value="guid"/>"><ww:text name="textKey('step1.title')"/></a>

                <ww:if test="/issue/subTask == false">
                    <br/>
                    <ww:text name="textKey('step1.parentissue')">
                        <ww:param name="'value0'"><strong><ww:property value="/parentIssueKey"/></strong></ww:param>
                    </ww:text>
                </ww:if>
                <br/>
                <ww:text name="textKey('step1.issuetype')">
                    <ww:param name="'value0'"><strong><ww:property value="/targetIssue/issueTypeObject/nameTranslation(.)"/></strong></ww:param>
                </ww:text>
            </li>
        </ww:elseIf>
        <ww:else>
            <li class="todo"><ww:text name="textKey('step1.title')"/></li>
        </ww:else>

        <ww:if test="/currentStep == 2">
            <li class="current"><ww:text name="textKey('step2.title')"/></li>
        </ww:if>
        <ww:elseIf test="/currentStep > 2">
            <li class="done">
                <ww:if test="/statusChangeRequired == true">
                    <a href="<ww:property value="/actionPrefix"/>SetIssueType.jspa?id=<ww:property value="issue/id"/>&guid=<ww:property value="guid"/>"><ww:text name="textKey('step2.title')"/></a>
                </ww:if>
                <ww:else>
                    <ww:text name="textKey('step2.title')"/>
                </ww:else>
                <br/>
                <ww:text name="textKey('step2.status')">
                    <ww:param name="'value0'"><strong><ww:property value="/targetIssue/statusObject/nameTranslation(.)"/></strong></ww:param>
                </ww:text>
            </li>
        </ww:elseIf>
        <ww:else>
            <li class="todo"><ww:text name="textKey('step2.title')"/></li>
        </ww:else>

        <ww:if test="/currentStep == 3">
            <li class="current"><ww:text name="textKey('step3.title')"/></li>
        </ww:if>
        <ww:elseIf test="/currentStep > 3">
            <li class="done">
                <a href="<ww:property value="/actionPrefix"/>SetStatus.jspa?id=<ww:property value="issue/id"/>&guid=<ww:property value="guid"/>"><ww:text name="textKey('step3.title')"/></a>
            </li>
        </ww:elseIf>
        <ww:else>
            <li class="todo"><ww:text name="textKey('step3.title')"/></li>
        </ww:else>

        <ww:if test="/currentStep == 4">
            <li class="current"><ww:text name="textKey('step4.title')"/></li>
        </ww:if>
        <ww:else>
            <li class="todo"><ww:text name="textKey('step4.title')"/></li>
        </ww:else>
    </ol>
