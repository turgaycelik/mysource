// Depends on:
//     release/shortcuts.rest.docs.groovy
//     release/shortcuts.release.groovy
//     shortcuts.common.groovy

plan(key: 'PRD', name: 'Publish Rest Docs', description: '', enabled: 'true') {
    def repoName = 'JIRA 6.3.x Release Variable Branch stash'

    releaseProject()
    releaseLabels()

    globalVariables()

    repository(name: repoName)

    stage(name: 'Default Stage', manual: 'false') {
        publishRestDocs(repoName: repoName)
    }
}
