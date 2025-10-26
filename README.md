# Shodh-a-Code Contest Platform

A live coding contest platform with real-time code execution for Java, Python, and C++.

---

## Quick Start

### Prerequisites
- Docker Desktop (4GB+ RAM)
- Ports 3000 and 8080 available

### Setup

```bash

# 1. Start application
docker-compose up --build

# 2. Access
open http://localhost:3000
```

First build: 5-10 minutes. Subsequent builds: 1-2 minutes.

### Usage
1. Contest ID: `contest-1`
2. Enter any username
3. Select problem, write code, submit
4. View results in 2-5 seconds

---

## API Documentation

### Base URL
`http://localhost:8080/api`

### Endpoints

#### Get Contest
```http
GET /contests/{contestId}
```
Response:
```json
{
  "id": "contest-1",
  "name": "Sample Coding Contest",
  "problems": [{"id": "problem-1", "title": "Sum of Two Numbers"}]
}
```

#### Get Problem
```http
GET /problems/{problemId}
```
Response:
```json
{
  "id": "problem-1",
  "title": "Sum of Two Numbers",
  "statement": "Given two integers A and B, compute their sum.",
  "inputFormat": "Two space-separated integers A and B",
  "outputFormat": "Single integer representing A + B",
  "sampleTestCases": [{"input": "5 3", "expectedOutput": "8"}]
}
```

#### Submit Code
```http
POST /submissions
Content-Type: application/json

{
  "username": "vivek",
  "contestId": "contest-1",
  "problemId": "problem-1",
  "code": "import java.util.Scanner;\npublic class Main {...}",
  "language": "java"
}
```
Response:
```json
{
  "submissionId": 1,
  "status": "PENDING",
  "submittedAt": "2025-10-26T10:42:15.123"
}
```

#### Check Status
```http
GET /submissions/{submissionId}
```
Response:
```json
{
  "submissionId": 1,
  "status": "ACCEPTED",
  "verdict": "All test cases passed"
}
```

Status values: `PENDING`, `RUNNING`, `ACCEPTED`, `WRONG_ANSWER`, `RUNTIME_ERROR`, `TIME_LIMIT_EXCEEDED`

#### Get Leaderboard
```http
GET /contests/{contestId}/leaderboard
```
Response:
```json
[
  {
    "username": "vivek",
    "score": 300,
    "problemsSolved": 3,
    "rank": 1
  }
]
```

---

## Design Choices

### Backend Architecture

**Layered Structure**: Controller → Service → Repository
- **Why**: Clear separation of concerns, easy to test and scale
- **Example**: `ContestController` → `ContestService` → `ContestRepository`

**Async Code Execution**:
```java
@Async
public void judgeSubmission(Submission submission) {
    // Runs in background thread
}
```
- **Why**: Non-blocking API, immediate response to user
- **Trade-off**: Requires polling, but simpler than WebSockets

### Frontend Architecture

**State Management**: React hooks (`useState`, `useEffect`)
- **Why**: Simple state, no Redux overhead needed
- **When to upgrade**: Multiple contests or complex user auth

**Polling Strategy**:
- Submissions: Every 2-3 seconds
- Leaderboard: Every 20 seconds
- **Why**: Simpler than WebSockets, works through firewalls
- **Trade-off**: ~2 second latency vs instant updates

### Docker Orchestration

**Security Challenge**: How to safely run untrusted code?

**Solution**: Isolated Docker containers with limits
```bash
docker run --rm \
  --memory=256m \
  --cpus=0.5 \
  --network=none \
  -v /tmp/code:/code \
  shodh-judge:latest
```

**stdin/stdout Challenge**: How to pass input to programs?

**Solution**: Redirect inside container
```bash
# Works: Files accessible in container
java Main < input.txt > output.txt

# Doesn't work: Files outside container
ProcessBuilder.redirectInput(file)
```

**C++ Permission Issue**: Exit code 126 (permission denied)

**Solution**: Compile to `/tmp` which has execute permission
```bash
# Fails: ./main needs execute permission
g++ -o main main.cpp && ./main

# Works: /tmp/a.out has permission by default
g++ -o /tmp/a.out main.cpp && /tmp/a.out
```

**Multi-Stage Builds**: Reduce image size
- Backend: 1.2GB → 500MB (exclude Maven)
- Frontend: Build deps cached separately
- **Benefit**: Faster subsequent builds (layer caching)

### Key Trade-offs

| Choice | Pro | Con | When to Change |
|--------|-----|-----|----------------|
| H2 Database | Zero config, fast dev | No persistence | Need data to survive restarts |
| Polling | Simple, reliable | Network overhead | >100 concurrent users |
| Local judge | Fast, no queue | Limited concurrency | >50 submissions/sec |
| No auth | Easy testing | No security | Production deployment |

---

## Architecture

```
User Browser (localhost:3000)
       ↓
  Next.js Frontend
       ↓ REST API
  Spring Boot Backend (localhost:8080)
       ↓ Docker API
  Judge Container (isolated execution)
```

**Stack**:
- Frontend: Next.js 14, React 18, Tailwind CSS, Monaco Editor
- Backend: Spring Boot 3.1.5, Java 17, H2 Database
- Judge: Docker containers (Ubuntu + JDK + Python + GCC)

---

## Test Solutions

### Problem 1: Sum (Java)
```java
import java.util.Scanner;
public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println(sc.nextInt() + sc.nextInt());
    }
}
```

### Problem 1: Sum (Python)
```python
a, b = map(int, input().split())
print(a + b)
```

### Problem 1: Sum (C++)
```cpp
#include <iostream>
using namespace std;
int main() {
    int a, b;
    cin >> a >> b;
    cout << a + b << endl;
}
```

---

## Troubleshooting

**Port in use**: `lsof -ti:3000 | xargs kill -9` (Mac/Linux)

**Build fails**: Check Docker is running, restart Docker Desktop

**Submission stuck**: Check logs with `docker-compose logs backend`

---

## Future Enhancements

- [ ] User authentication (JWT)
- [ ] PostgreSQL database
- [ ] WebSocket real-time updates
- [ ] More languages (JavaScript, Rust)
- [ ] Contest scheduling

---


