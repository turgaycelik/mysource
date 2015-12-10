#!/bin/bash

unzip -qo *.zip
mkdir -p jira-distribution/jira-webdriver-tests-runner/target/generated-sources/jira-webdriver-tests
unzip -qo jira-distribution/jira-webdriver-tests-runner/PassedArtifacts/jira-webdriver-tests-*-jar-with-dependencies.jar xml/* com/atlassian/jira/* -d jira-distribution/jira-webdriver-tests-runner/target/generated-sources/jira-webdriver-tests
