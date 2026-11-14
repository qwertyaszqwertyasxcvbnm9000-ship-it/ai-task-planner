#!/usr/bin/env bash
set -e

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$DIR"

# Configure Java 27 / OpenJDK path
if [ -d "/opt/homebrew/opt/openjdk" ]; then
    export JAVA_HOME="/opt/homebrew/opt/openjdk"
    export PATH="/opt/homebrew/opt/openjdk/bin:$PATH"
fi

echo "=========================================================="
echo "⚡ Starting AuraRoutine | AI Daily Task Scheduler Backend"
echo "=========================================================="
echo "Java version: $(java -version 2>&1 | head -n 1)"
echo "Web UI URL:   http://localhost:8080"
echo "Calendar:     http://localhost:8080/api/calendar/export.ics"
echo "AI Status:    http://localhost:8080/api/ai/status"
echo "=========================================================="

/opt/homebrew/bin/mvn spring-boot:run -Dmaven.repo.local="$DIR/.m2/repository"
