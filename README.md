# ⚡ AuraRoutine | AI Behavioral Daily Task Scheduler

An intelligent daily routine task scheduling application built with a **pure Java backend** (Spring Boot 3 / Java 17+) and a sleek glassmorphic Web UI.

AuraRoutine schedules tasks around your fixed daily routine anchors (sleep, meals, workouts, meetings) and continuously learns your behavior over time:
- **Circadian Energy Matching**: Places high-demand tasks (deep work, creative problem solving) during your peak cognitive energy hours, and shallow work (administrative, chores) in lower energy windows.
- **Duration Estimation Learning (Bayesian Velocity Adaptation)**: Learns whether you overestimate or underestimate tasks by category (e.g. adding a +25% buffer if coding tasks consistently run longer).
- **Dual AI Engine**:
  - **Ollama Open-Source LLM Integration**: Connects to local models (`llama3.2`, `mistral`, `gemma2`, `qwen2.5`) for deep reasoning and routine extraction.
  - **Pure Java Behavioral Engine (Offline-First Fallback)**: Built-in deterministic constraint-satisfaction and probabilistic scheduler that runs 100% offline with zero cost.
- **Routine Ingestion**: Upload past routines in **CSV**, **JSON**, **iCalendar (.ics)**, or freeform natural text notes.
- **Calendar Sync (.ics Feed)**: One-click export to Google Calendar, Apple Calendar, or Outlook.
- **Habit Drift Analytics**: Detects schedule slippage, delayed starts, and shifting focus windows.
- **Micro-Feedback Loop**: 2-second check-in upon task completion to refine your personal chronotype profile.

---

## 🚀 Quick Start

### 1. Start the Java Backend & Web UI
Run the included startup script:
```bash
./run.sh
```
Or with Maven directly:
```bash
/opt/homebrew/opt/openjdk/bin/java -jar target/task-planner-1.0.0.jar
```
The server will start on **`http://localhost:8080`**.

### 2. Open in Browser
Visit **[http://localhost:8080](http://localhost:8080)** to access the dashboard.

---

## 🤖 Using Open-Source AI Models (Ollama)

AuraRoutine works immediately out of the box using its pure Java Circadian Heuristics engine. If you want full neural scheduling powered by local open-source LLMs:

1. **Install Ollama** (if not already installed):
   ```bash
   brew install ollama
   ```
2. **Download and run an open-source model**:
   ```bash
   ollama run llama3.2
   ```
   *(Or `ollama run mistral` or `ollama run qwen2.5`)*

3. The Java backend detects Ollama at `http://localhost:11434` automatically, switching the status badge from *Java Heuristics Engine* to *Ollama: LLAMA3.2*!

---

## 📁 Ingesting Past Routines

You can paste or upload routine files from the `sample-routines/` directory or use the UI buttons:

1. **CSV Format** (`sample-routines/weekly_routine.csv`):
   ```csv
   Name,StartTime,EndTime,Category,Flexible
   Morning Jog,06:30,07:15,HEALTH_FITNESS,false
   Breakfast,07:15,08:00,REST_LEISURE,false
   Daily Standup,09:30,10:00,ADMINISTRATIVE,false
   Lunch & Walk,12:30,13:30,REST_LEISURE,false
   Gym Workout,17:30,18:45,HEALTH_FITNESS,true
   Dinner & Family,19:30,20:30,REST_LEISURE,false
   ```
2. **JSON Format** (`sample-routines/developer_routine.json`)
3. **iCalendar Format** (`sample-routines/calendar_export.ics`)
4. **Natural Text Notes** (`sample-routines/freeform_notes.txt`)

---

## 🔌 REST API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/tasks/day?date=YYYY-MM-DD` | Get all tasks and schedule for a date |
| `POST` | `/api/tasks` | Create or update a task |
| `POST` | `/api/tasks/schedule` | Run AI circadian scheduling algorithm for the day |
| `POST` | `/api/tasks/feedback` | Submit completion feedback (trains behavioral model) |
| `GET` | `/api/routine/anchors` | Retrieve recurring fixed routine anchors |
| `POST` | `/api/routine/upload` | Parse & learn routine from CSV, JSON, ICS, or text |
| `GET` | `/api/routine/profile` | View learned circadian curve & velocity multipliers |
| `GET` | `/api/routine/drift` | View Habit Drift report & AI recommendations |
| `GET` | `/api/calendar/export.ics` | Download RFC 5545 `.ics` calendar feed |
| `GET` | `/api/ai/status` | Check Ollama connectivity & active model |

---

## 🧪 Running Tests
```bash
JAVA_HOME=/opt/homebrew/opt/openjdk /opt/homebrew/bin/mvn test -Dmaven.repo.local=".m2/repository"
```
All unit tests verify Bayesian multiplier updates, circadian energy alignment scoring, and multi-format parsing.
