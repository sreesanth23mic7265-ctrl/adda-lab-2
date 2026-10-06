pipeline {
    agent any

    stages {

        stage('Stop Application') {
            steps {
                bat '''
                    for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8081 ^| findstr LISTENING') do (
                        taskkill /F /PID %%a
                    )
                '''
            }
        }

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
                    start "" /B java -jar target\\demo-0.0.1-SNAPSHOT.jar > app.log 2>&1
                '''
            }
        }
    }
}