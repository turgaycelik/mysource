// --------------------------------------------------------------------------------------------------
// This file contains shortcuts common to all plans on Tier 1
// --------------------------------------------------------------------------------------------------
tier1Project() {
    project(key: 'J63STABLEPT', name: 'JIRA 6.3 Stable - Plan templates', description: '')
}

tier1Labels() {
    label(name: 'plan-templates')
    label(name: 'tier_1')
}
