plan(key: 'UNITJ8', name: 'CI Unit Tests - Java 8', description: 'Unit tests running with JDK 8') {
    def compileTimeJdk = 'JDK 1.7'
    def runTimeJdk = 'JDK 1.8'
    def repoName = 'JIRA 6.3.x branch'

    java8Project()
    java8Labels()

    notifications()
    globalVariables()
    branches(pattern: 'issue/JDEV-28412-plan-templates', removeAfterDays: '10')
    repositoryPolling(repoName: repoName)

    dependencies(triggerOnlyAfterAllStagesGreen: 'false', automaticDependency: 'false', triggerForBranches: 'false'){
        childPlan(planKey: 'JJDK8-BPJ8')
    }

    stage(name: 'Default stage', description: 'Runs unit tests', manual: 'false') {
        unitTests(repoName: repoName, compileTimeJdk: compileTimeJdk, runTimeJdk: runTimeJdk)
    }
}