@echo off
if "%OS%" == "Windows_NT" setlocal
rem ---------------------------------------------------------------------------
rem Stop script for the JIRA Service
rem

net stop <jira_service_id>
