# Jenkins DevOps Shared Libraries

A collection of Jenkins Pipeline shared libraries for common DevOps software. Usage and dependencies for each can be found in the [documentation](docs).

Unsure how to use these in your declarative syntax `Jenkinsfile`? Check the declarative Jenkinsfile shared library [documentation](https://jenkins.io/doc/book/pipeline/shared-libraries/#using-libraries).

Additionally, you can pare down the libraries available from this repo and then load those in yourself from your own git repo or otherwise.

## Retrieve and use with Disabled Sandbox

Basically, if you have the GitHub Branch Source plugin installed, then you can [load a specific version](https://jenkins.io/doc/book/pipeline/shared-libraries/#library-versions) like:

```groovy
@Library('github.com/mschuchard/jenkins-devops-libs@version')_
```

If you do not have this plugin installed, or want more flexibility over the version used, then you can use or expand upon this class:

```groovy
library identifier: 'jenkins-devops-libs@master', retriever: modernSCM(
  [$class: 'GitSCMSource',
   remote: 'https://github.com/mschuchard/jenkins-devops-libs.git'])
```

Note that this latter example is also useful for circumventing Github API rate limit issues.

## Use with Enabled Sandbox

Basically, you need to first [add the shared library](https://jenkins.io/doc/book/pipeline/shared-libraries/#global-shared-libraries) in the Jenkins global configuration. Then, you can either load the library's methods with:

```groovy
@Library('jenkins-devops-libs@version')_
```

or using the defaults with:

```groovy
library('jenkins-devops-libs')
```

## Supported
- [AWX/Ansible Tower](docs/AWX.md) (beta)
- [Goss](docs/Goss.md)
- [Helm](docs/Helm.md)
- [OpenFaaS](docs/FaaS.md)
- [Packer](docs/Packer.md)
- [Puppet Enterprise](docs/Puppet.md) (beta)
- [Terraform](docs/Terraform.md)

## 2.0.0 Breaking Changes
Note the following breaking changes for new major version 2.0.0:

- All publicly accessible global variable methods and their associated parameters have been converted to camelCase to adhere to standard Groovy convention.
- The old DSL for Jenkines Pipeline global variable methods which accepted Closure type inputs is no longer supported. Usage will need to be updated to the new DSL with Map type inputs.
- Global variable methods for software installation and configuration are removed. Users should migrate to build agents managed with software provisioning, configuration management, and/or containerization.

## Contributing
Code should pass all acceptance tests. New features should involve new acceptance tests.

Please consult the GitHub Project for the current development roadmap.
