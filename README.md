# 🌐 RefNet – Job Referral Management Platform

> A professional platform that bridges the gap between **candidates** and **employees** for seamless referral collaboration — without interfering with internal hiring systems.

---

## 📖 Overview

**RefNet** is designed to simplify and organize the referral process by enabling:

* Pre-referral discussions
* Transparent request tracking
* Structured communication between candidates and employees

It ensures a **smooth, independent, and efficient referral workflow**.

---

## 🏗️ Project Structure

```
RefNet/
│
├── backend/    → Java Spring Boot 3 API
└── frontend/   → User dashboard (HTML-based UI)
```

---

## 🚀 Core Features

### 👩‍💼 Candidate Tools

* Create and manage profiles
* Upload resumes
* Request job referrals

### 🧑‍💻 Employee Tools

* View referral requests
* Communicate with candidates
* Submit formal referrals

### 📊 HR Dashboard

* Track referral pipeline
* Monitor hiring progress
* Access analytics and insights

### 💬 Real-Time Chat

* Direct messaging between users
* Powered by WebSockets

### 🔔 Notifications

* Automated email alerts
* Status updates for every stage

---

## 🛠️ Tech Stack

| Category      | Technology                                     |
| ------------- | ---------------------------------------------- |
| Language      | Java 17                                        |
| Framework     | Spring Boot 3                                  |
| Databases     | PostgreSQL (Primary), MongoDB (Audit Logs)     |
| Security      | JWT Authentication + Role-Based Access Control |
| Communication | WebSockets                                     |

---

## ⚙️ Getting Started

### 🔹 Backend Setup

```bash
cd backend
```

✔ Make sure the following are running:

* PostgreSQL
* MongoDB

▶ Run the backend server:

```bash
mvn spring-boot:run
```

---

### 🔹 Frontend Setup

```bash
cd frontend
```

✔ Open directly in browser:

```bash
index.html
```

---

## 🔐 Security Features

* JWT-based authentication
* Role-based authorization (Candidate / Employee / HR)
* Secure API endpoints

---

## 📌 Key Highlights

✨ Clean separation from company hiring systems
✨ Real-time interaction between users
✨ Scalable architecture with dual databases
✨ Transparent and trackable referral flow

---

## 🤝 Contribution

Contributions are welcome!

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Submit a pull request

---
For queries or collaboration, feel free to reach out!

