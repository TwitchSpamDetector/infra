# Jenkins Shared Library

Librería compartida para los pipelines de TwitchSpamDetector.

## Uso

En el `Jenkinsfile` de cada microservicio:

### Java / Maven

```groovy
@Library('jenkins-shared-library') _

standardJavaPipeline(serviceName: 'core-api')