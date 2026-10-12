pipeline {
    agent any

    stages {

        stage('Stop Application') {
            steps {
                bat '''
                    for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8081 ^| findstr LISTENING') do (
                        echo Stopping process %%a on port 8081...
                        taskkill /F /PID %%a
                    )
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
                    if not exist "C:\\SpringBootApp" mkdir "C:\\SpringBootApp"
                    copy /Y "target\\demo-0.0.1-SNAPSHOT.jar" "C:\\SpringBootApp\\app.jar"
                    if errorlevel 1 exit /b 1
                '''
            }
        }

        stage('Start Application') {
            steps {
                bat '''
                    schtasks /run /tn "SpringBootApp"

                    echo Waiting for Spring Boot to start...
                    ping -n 16 127.0.0.1 > nul

                    netstat -ano | findstr :8081 | findstr LISTENING
                    if errorlevel 1 (
                        echo ERROR: Application failed to start!
                        type C:\\SpringBootApp\\app.log
                        exit /b 1
                    )
                    echo Spring Boot started successfully!
                '''
            }
        }
    }
}