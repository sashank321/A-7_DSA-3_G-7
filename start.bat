@echo off
setlocal

echo [StrataSearch] Setting up Java 21 environment...
if exist "D:\java-21-openjdk-21.0.4.0.7-1.win.jdk.x86_64\java-21-openjdk-21.0.4.0.7-1.win.jdk.x86_64\bin\java.exe" (
    set "JAVA_HOME=D:\java-21-openjdk-21.0.4.0.7-1.win.jdk.x86_64\java-21-openjdk-21.0.4.0.7-1.win.jdk.x86_64"
    set "PATH=%JAVA_HOME%\bin;%PATH%"
)

echo [StrataSearch] Starting Spring Boot Backend on port 8080...
start "StrataSearch Backend (:8080)" cmd /k "java -jar stratasearch-backend\target\stratasearch-backend-0.0.1-SNAPSHOT.jar"

echo [StrataSearch] Starting React Frontend on port 5173...
start "StrataSearch Frontend (:5173)" cmd /k "cd /d stratasearch-frontend && npm run dev"

echo [StrataSearch] Services launched!
echo Frontend: http://localhost:5173
echo Backend API / Swagger: http://localhost:8080/swagger-ui.html
