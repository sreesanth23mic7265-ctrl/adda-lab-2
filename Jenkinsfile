pipeline {
    agent any

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                bat 'mvnw.cmd clean package -DskipTests'
            }
        }

        stage('Deploy') {
            steps {
                bat '''
                    for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8081 ^| findstr LISTENING') do (
                        taskkill /F /PID %%a
                    )

                    copy /Y target\\*.jar app.jar

                    start /B java -jar app.jar > app.log 2>&1
                '''
            }
        }
    }
}