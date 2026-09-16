def call(Map config = [:]) {
    def serviceName = config.serviceName ?: error("serviceName es requerido")
    def jdkVersion = config.jdk ?: 'jdk21'

    pipeline {
        agent any

        tools {
            jdk jdkVersion
        }

        options {
            timestamps()
            disableConcurrentBuilds()
        }

        stages {
            stage('Checkout') {
                steps {
                    echo "Build #${env.BUILD_NUMBER} - ${serviceName}"
                    sh 'ls -la'
                }
            }

            stage('Install') {
                steps {
                    sh 'chmod +x mvnw'
                    sh './mvnw -B dependency:go-offline'
                }
            }

            stage('Test') {
                steps {
                    sh './mvnw -B test'
                }
            }

            stage('Build') {
                steps {
                    sh './mvnw -B clean package -DskipTests'
                }
            }
        }

        post {
            always {
                cleanWs()
            }
            success {
                echo "✅ ${serviceName} build #${env.BUILD_NUMBER} OK"
            }
            failure {
                echo "❌ ${serviceName} build #${env.BUILD_NUMBER} FALLÓ"
            }
        }
    }
}