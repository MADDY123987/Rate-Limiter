<div align="center">

<img src="https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white"/>
<img src="https://img.shields.io/badge/Spring_Boot-WebFlux-6DB33F?style=for-the-badge&logo=springboot&logoColor=white"/>
<img src="https://img.shields.io/badge/Redis-Token_Bucket-DC382D?style=for-the-badge&logo=redis&logoColor=white"/>
<img src="https://img.shields.io/badge/Docker-Containerized-2496ED?style=for-the-badge&logo=docker&logoColor=white"/>

<br/><br/>

# 🚦 Distributed Rate Limiter

### Token Bucket Algorithm · Redis-Backed · Production-Grade

*Designed for high-concurrency API Gateways — enforcing fair usage at scale.*

---

</div>

## 📌 What This Project Does

This is a **distributed rate limiter** built on **Redis** using the **Token Bucket Algorithm**. It plugs into any API layer and controls how many requests a client can fire — preventing abuse, ensuring fairness, and keeping your backend alive under traffic spikes.

> Think of it as a bouncer for your API. Fast, stateless, and unapologetically strict.

---

## ⚙️ How the Token Bucket Works

```
Client makes a request
        │
        ▼
  [Redis Bucket]
   ┌──────────────────────────────┐
   │  Refill tokens based on time │
   │  elapsed since last request  │
   └──────────────────────────────┘
        │
        ▼
  Tokens available?
   ├── ✅ YES → Consume 1 token → Allow request (200)
   └── ❌ NO  → Reject request (429 Too Many Requests)
```

**Refill Formula:**
```
tokensToAdd = (elapsedTimeMs × refillRate) / 1000
```

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot (WebFlux — reactive) |
| Rate Limit Store | Redis |
| Redis Client | Jedis |
| Containerization | Docker |

---

## 📊 Default Configuration

| Parameter | Value | Notes |
|---|---|---|
| `capacity` | 10 tokens | Max burst size per client |
| `refillRate` | 1 token/sec | Sustained request rate |
| `algorithm` | Token Bucket | Allows burst, prevents flooding |
| `clientKey` | IP Address | Identifies unique clients |
| `storage` | Redis | Shared state across instances |

---

## 🔁 Behavior Example

```
Setup: capacity = 10, refillRate = 1 token/sec

Burst Phase:
  Requests 1–10  →  ✅ Allowed
  Request 11     →  ❌ 429 Too Many Requests

After 1 second:
  +1 token refilled
  Request 12     →  ✅ Allowed again
```

This is what makes Token Bucket superior to a Fixed Window — **burst traffic is absorbed**, not instantly rejected.

---

## 📡 API Reference

### `GET /test`
Fire a request through the rate limiter.

**Response (200):**
```
Request allowed
```

**Response (429):**
```
Too many requests
```

---

### `GET /rate-limit/status`
Inspect the current token state for the calling client.

**Response:**
```json
{
  "clientId": "127.0.0.1",
  "capacity": 10,
  "availableTokens": 7
}
```

---

## 🧩 Architecture

```
┌─────────────────────────────────────────────────┐
│                  HTTP Request                   │
└────────────────────┬────────────────────────────┘
                     │
                     ▼
         ┌───────────────────────┐
         │   RateLimiterService  │  ← Abstraction layer
         └───────────┬───────────┘
                     │
                     ▼
      ┌──────────────────────────────┐
      │   RedisTokenBucketService    │  ← Core logic
      │  • Token consumption         │
      │  • Refill calculation        │
      │  • Redis read/write          │
      └──────────────┬───────────────┘
                     │
                     ▼
              ┌─────────────┐
              │    Redis    │  ← Shared distributed state
              └─────────────┘
```

### Key Components

**`RedisTokenBucketService`**
The heart of the system. Handles token consumption, refill logic, and all Redis operations.

**`RateLimiterService`**
Clean abstraction layer sitting between controllers/filters and the Redis service. Keeps concerns separated.

**`StatusController`**
Exposes the `/rate-limit/status` endpoint for real-time observability.

---

## 💡 Design Decisions

### Why Redis?
- **Distributed-first**: State is shared across all service instances — no per-instance counters.
- **Sub-millisecond latency**: Redis is fast enough to add near-zero overhead to every request.
- **Persistence-optional**: Can survive restarts with Redis AOF/RDB if needed.

### Why Token Bucket over Fixed Window?
| Property | Fixed Window | Token Bucket |
|---|---|---|
| Burst handling | ❌ Hard cutoff | ✅ Absorbs bursts |
| Smoothness | ❌ Spiky at window reset | ✅ Gradual refill |
| Client experience | Poor | Fair |

### ⚠️ Known Limitation
The current implementation uses Redis `DECR` which is **not fully atomic** with the refill step. For production systems under extreme concurrency, this can cause minor over-admission.

**Fix (planned):** Replace with a **Redis Lua script** to make consume + refill a single atomic operation.

---

## 🚀 Running Locally

### 1. Start Redis
```bash
docker run -d -p 6379:6379 redis
```

### 2. Start the Application
```bash
./mvnw spring-boot:run
```

### 3. Test Rate Limiting
```bash
for i in {1..20}; do
  curl -s http://localhost:8080/test
  echo
done
```

### Expected Output
```
Request allowed
Request allowed
Request allowed
...
Too many requests
Too many requests
```

---

## 🔥 Roadmap

- [ ] **Lua Script Atomicity** — Make token consume + refill a single Redis transaction
- [ ] **Sliding Window Limiter** — More precise than token bucket for some use cases
- [ ] **Per-User Auth-Based Limiting** — JWT/API-key aware rate limiting
- [ ] **Spring Cloud Gateway Filter** — Drop-in integration for microservice gateways
- [ ] **Prometheus + Grafana** — Real-time metrics dashboard for rate limit hits/passes

---

## 👨‍💻 Author

**Madhavan R**
B.Tech CSE · VIT Vellore

---

<div align="center">

*This project demonstrates distributed systems thinking, reactive backend design, and production-oriented architecture decisions — built to be extended, not just demoed.*

</div>
