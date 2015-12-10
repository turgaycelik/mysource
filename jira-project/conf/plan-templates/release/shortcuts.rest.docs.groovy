globalVariables() {
    releaseGlobalVariables()
}

publishRestDocs(['repoName']) {
    job(name: 'Default Job', key: 'JOB1', description: '') {
        requireLocalLinux()

        checkout(repoName: '#repoName', forceClean: 'false')
        executeRestDocsScript()
    }
}

executeRestDocsScript() {
    task(
            type: 'script',
            description: 'Executes Rest Docs Script',
            script: './bin/jira-rest-docs.sh',
            argument: '${bamboo.jira.6.3.release.version}'
    )
}