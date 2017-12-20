# Jenkins DevOps Shared Libraries

A collection of Jenkins Pipeline shared libraries for common DevOps software. Usage and library dependencies for each can be found in the [documentation](docs).

Unsure how to use these in your declarative syntax `Jenkinsfile`? Check the declarative Jenkinsfile shared library  [documentation](https://jenkins.io/doc/book/pipeline/shared-libraries/#using-libraries).

Basically, if you have the GitHub Branch Source plugin installed, then you can load the current master branch like:

```groovy
@Library('github.com/mschuchard/jenkins-devops-libs')
```

If you do not have this plugin installed, or want more flexibility over the version used, then you can use or expand upon this class:

```groovy
library identifier: 'jenkins-devops-libs@master', retriever: modernSCM(
  [$class: 'GitSCMSource',
   remote: 'https://github.com/mschuchard/jenkins-devops-libs.git'])
```

Additionally, you can pare down the libraries available from this repo and then load those in yourself from your own git repo or otherwise.

## TODO

- Puppet
- Serverspec
- Else, replace all `sh` with API where possible and abstract common methods into utils.
