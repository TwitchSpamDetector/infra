def call(Map config = [:]) {
    def serviceName = config.serviceName ?: error("serviceName es requerido")
    def nodeVersion = config.node ?: 'node20'

    pipeline {
        agent any

        tools {
            nodejs nodeVersion
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
                    sh 'npm ci'
                }
            }

            stage('Test') {
                steps {
                    sh 'npm test'
                }
            }

            stage('Build') {
                steps {
                    sh 'npm run build'
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