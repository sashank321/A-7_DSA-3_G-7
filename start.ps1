$java21Path = "D:\java-21-openjdk-21.0.4.0.7-1.win.jdk.x86_64\java-21-openjdk-21.0.4.0.7-1.win.jdk.x86_64"
if (Test-Path "$java21Path\bin\java.exe") {
    $env:JAVA_HOME = $java21Path
    $env:Path = "$java21Path\bin;" + $env:Path
}

Write-Host "[StrataSearch] Starting Spring Boot Backend on port 8080..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", "`$env:JAVA_HOME = '$java21Path'; `$env:Path = '$java21Path\bin;' + `$env:Path; java -jar stratasearch-backend\target\stratasearch-backend-0.0.1-SNAPSHOT.jar"

Write-Host "[StrataSearch] Starting React Frontend on port 5173..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd stratasearch-frontend; npm run dev"

Write-Host "[StrataSearch] Both services are running!" -ForegroundColor Green
Write-Host "Frontend: http://localhost:5173" -ForegroundColor Yellow
Write-Host "Backend API / Swagger: http://localhost:8080/swagger-ui.html" -ForegroundColor Yellow
