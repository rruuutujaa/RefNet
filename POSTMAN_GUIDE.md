# RefNet Real-World Testing Flow (Postman Guide)

This guide is organized chronologically based on how the RefNet platform operates in a real-world scenario. Follow these phases to test the full system lifecycle.

---

## 🚀 Getting Started
**Base URL**: `http://localhost:8080/api/v1`  
**Auth Tip**: Copy the `accessToken` from login/register responses and use it as a **Bearer Token** in the Authorization tab for all subsequent requests.

---

## Phase 1: Platform Infrastructure (The Setup)
Before any referrals happen, the platform needs an Admin and a company context.

### 1.1 Create the Platform Admin
- **Endpoint**: `POST /auth/register`
- **Body**: Use `roles: ["ADMIN"]`. This user has god-mode access to manage all companies and jobs.

### 1.2 Setup Company & HR
- **Register HR Admin**: `POST /auth/register` with `roles: ["HR_ADMIN"]` and a specific `companyId`.
- **IRL Note**: In a real system, the Admin would create the company first. Here, the `companyId` is the linking factor.

---

## Phase 2: Building the Marketplace (Jobs & Candidates)
Now that the company exists, we need jobs to refer to and candidates to be referred.

### 2.1 HR Creates Job Openings
- **Auth**: Login as `HR_ADMIN` or `ADMIN`.
- **Endpoint**: `POST /jobs`
- **Goal**: Create 2-3 jobs for your `companyId`. These will now appear in the public marketplace.

### 2.2 Candidates Join the Platform
- **Auth**: `POST /auth/register` with `roles: ["CANDIDATE"]`.
- **Candidate Profile**: `PUT /users/me/profile`.
- **IRL Note**: A candidate *must* complete their profile (skills, resume, bio) before an employee will trust them enough to refer them.

### 2.3 Employees Join the Company
- **Auth**: `POST /auth/register` with `roles: ["EMPLOYEE"]` using the same `companyId` as the jobs.
- **Goal**: These are the "Referrers" who have the power to submit formal applications to HR.

---

## Phase 3: The Connection (Networking)
The candidate finds a job and reaches out to an employee for a referral.

### 3.1 Candidate Sends Referral Request
- **Auth**: Login as `CANDIDATE`.
- **Action**: `POST /referral-requests`. 
- **IRL Note**: The candidate sends a "pitch" message to the employee.

### 3.2 Real-time Chat (Optional)
- **Action**: Use `GET /chat/{userId}` to simulate the conversation between Candidate and Employee as they discuss the role.

### 3.3 Employee Accepts the Request
- **Auth**: Login as `EMPLOYEE`.
- **Action**: `PATCH /referral-requests/{id}/accept`.
- **IRL Note**: This signals to the candidate that the employee is ready to vouch for them.

---

## Phase 4: Formal Referral (The Submission)
The "Handshake" is complete. The employee now formally refers the candidate to the company's HR.

### 4.1 Employee Submits Formal Referral
- **Auth**: Login as `EMPLOYEE`.
- **Endpoint**: `POST /referrals`
- **Validation**: The system checks if the employee and job belong to the same company and prevents duplicate referrals.

---

## Phase 5: HR Lifecycle (The Hiring Pipeline)
The referral is now in the hands of the company's HR department.

### 5.1 HR Reviews Referrals
- **Auth**: Login as `HR_ADMIN`.
- **Action**: `GET /referrals` (scoped to their company).

### 5.2 The State Machine (Status Updates)
HR moves the candidate through the formal stages. 
- **Step 1**: `PATCH /referrals/{id}/status` -> `UNDER_REVIEW`
- **Step 2**: `PATCH /referrals/{id}/status` -> `ACCEPTED` (Candidate enters interview loop)
- **Step 3**: `PATCH /referrals/{id}/status` -> `HIRED` or `NOT_SELECTED` (The final outcome)

---

## Phase 6: Feedback & History
Throughout the process, everyone stays informed.

### 6.1 Notifications
- **Action**: `GET /notifications`
- **IRL Note**: Every status change in Phase 5 automatically triggers notifications for both the Candidate and the Employee.

### 6.2 Audit Trail
- **Action**: `GET /referrals/{id}/history`
- **IRL Note**: View the immutable timeline of who changed the status and what notes they left.

---

## 🛑 Testing Constraints to Watch For
1. **Security**: Try creating a job as a `CANDIDATE` (Should return `403 Forbidden`).
2. **Data Integrity**: Try referring the same candidate to the same job twice (Should return `409 Conflict`).
3. **Logic**: Try moving a referral from `SUBMITTED` directly to `HIRED` (Should return `400 Bad Request` via the State Machine).
