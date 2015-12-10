# File organization

**plan.*.groovy**

These files contain the build plans that will be imported to Bamboo:

**shortcuts.*.groovy**

These files contain all the helper shortcuts (aka functions) used by the plans.

**shortcuts.common.groovy**

This file contains all the helper shortcuts that are used by all plans.

**load.js**

This file is the NodeJS script used to quickly deploy the plan changes to JBAC.

# Developing plan templates

## Validation

After modifying a plan template, you can ask JBAC to validate your changes. Use this system to ensure your plan templates are valid and do not contain errors.
```
# clone bamboo-plan-templates source

$ git clone git@bitbucket.org:atlassian/bamboo-plan-templates.git

$ cd bamboo-plan-templates/src/main/script

# Use validate.groovy  (if you don't have groovy just $ brew install groovy)
# $ validate.groovy <template_file> <shortcuts_file> <bamboo_url> <username>

# Example
$ ./validate.groovy <jira-dir>/conf/plan-templates/java8/plan.func.groovy <jira-dir>/conf/plan-templates/java8/shortcuts.func.groovy https://jira.bamboo.atlassian.com jsanchez
```

Note: check the [Getting started with Plan Templates](https://extranet.atlassian.com/display/RELENG/HOWTO+-+Getting+started+with+Plan+Templates) page to get the latest documentation.


## Applying your changes

### Normal way

There should be a build in Bamboo that will pick up your plan templates and modify/create the existing builds to match the templates.
For the plan templates related with Java 8, you can find the build [here](https://jira-bamboo.internal.atlassian.com/browse/JJDK8-JJ8PT).

### Fast way

By using the NodeJS loader, you can upload your tests to JBAC using a REST endpoint. The main benefit of this approach is that you don't need to commit your changes first. This is very useful for debugging and developing purposes. Please, note that this action will effectively change the existing builds to match your templates, **this is not a dry-run**. If you break the current plans, re-run your Plan Template build to restore them.

```
# Install the node_modules, only required the first time
npm install

node conf/plan-templates/load.js conf/plan-templates/java8/plan.unit.groovy conf/plan-templates/java8/shortcuts.unit.groovy
> JBAC username: jsanchez
> JBAC password: ***

>Loading plan 'conf/plan-templates/java8/plan.unit.groovy'
> done!
```

# Documentation

* [HOWTO - Getting started with Plan Templates](https://extranet.atlassian.com/display/RELENG/HOWTO+-+Getting+started+with+Plan+Templates)
* [Plan Templates DSL examples](https://extranet.atlassian.com/display/RELENG/Plan+Templates+DSL+examples)
* [Tests for the PlanTemplates plugin](https://bitbucket.org/atlassian/bamboo-plan-templates/src/master/src/test/resources/?at=master)
