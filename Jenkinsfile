pipeline {
    agent any

    stages {

        stage('Stop Application') {
            steps {
                bat '''
                    setlocal

                    for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8081 ^| findstr LISTENING') do (
                        echo Stopping process %%a on port 8081...
                        taskkill /F /PID %%a
                    )

                    echo Stop Application stage completed.
                    exit /B 0
                '''
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
                    echo Starting Spring Boot application...
                    start "" /B java -jar target\\demo-0.0.1-SNAPSHOT.jar > app.log 2>&1
                    echo Application started.
                '''
            }
        }
    }
}