# 🏥 Doctor Appointment System - Arundhati Medical Clinic

A professional, full-stack medical clinic management system built with modern aesthetics and robust functionality. This platform streamlines doctor-patient interactions, appointment scheduling, and administrative oversight.

---

## ✨ Key Features
- **Patient Portal**: Browse verified doctors, view real-time availability (slots), and book appointments.
- **Doctor Dashboard**: Manage daily schedules, view patient history, and track appointments.
- **Admin Analytics**: Comprehensive dashboard for system oversight, user verification, and clinic stats.
- **Automated Notifications**: OTP-based email verification and appointment reminders.
- **Modern UI**: Clean, "Hospital Vibes" design with glassmorphism and smooth animations.

---

## 🛠️ Tech Stack

### Frontend
- **Framework**: React 19 (Vite)
- **Styling**: Vanilla CSS (Modern Design System with variables)
- **Animations**: Framer Motion
- **Icons**: Lucide React
- **Data Fetching**: Axios
- **Charts**: Chart.js / React-Chartjs-2

### Backend
- **Framework**: Spring Boot 3.2.4 (Java 17)
- **Security**: Spring Security + JWT (JSON Web Tokens)
- **Persistence**: Spring Data JPA + Hibernate
- **Database**: MySQL 8.0
- **Mailing**: Spring Mail (SMTP Integration)

---

## ⚙️ Configuration & Ports

| Service | Port | Base URL |
| :--- | :--- | :--- |
| **Frontend** | `5173` | `http://localhost:5173` |
| **Backend** | `8080` | `http://localhost:8080/api` |

---

## 🚀 Implementation Steps

### 1. Database Setup
1. Ensure **MySQL** is running on your machine.
2. Create a database named `arundhaticlinic`.
   ```sql
   CREATE DATABASE arundhaticlinic;
   ```

### 2. Backend Configuration
1. Navigate to: `backend/src/main/resources/application.properties`
2. **Critical Step**: Update the SMTP/Email settings to enable OTP verification.
   - Replace `spring.mail.username` with your email.
   - Replace `spring.mail.password` with your **App Password** (not your regular email password).
3. Update Database credentials if they differ from:
   - `spring.datasource.username=root`
   - `spring.datasource.password=1234`

### 3. Running the Backend
```bash
cd backend
mvn spring-boot:run
```

### 4. Running the Frontend
```bash
cd frontend
npm install
npm run dev
```

---

## 🔐 Default Access (Testing)

The system automatically seeds the database with initial users for easy testing.

| Role | Email / Username | Password |
| :--- | :--- | :--- |
| **Admin** | `vivekperla333@gmail.com` | `123456` |
| **Doctor** | `dr_cardio@clinic.com` | `1234` |
| **Doctor** | `dr_neuro@clinic.com` | `1234` |

> [!TIP]
> To test the **Patient** flow, use the **Register** page. You will receive an OTP on the email provided (if SMTP is configured correctly).

---

## 📂 Project Structure
```text
DoctorAppointmentSystem/
├── frontend/             # React + Vite Application
│   ├── src/
│   │   ├── components/   # Reusable UI components
│   │   ├── pages/        # Dashboard & Auth views
│   │   └── utils/        # Axios & API config
├── backend/              # Spring Boot API
│   ├── src/main/java/    # Core logic (Auth, Security, Services)
│   └── src/main/resources/ # application.properties
└── docs/                 # Mockups and Documentation
```

---

## 📸 UI Previews
- **Patient Dashboard**: [View Image](./docs/patient_dashboard_mockup.png)
- **Doctor Dashboard**: [View Image](./docs/doctor_dashboard_mockup.png)
- **Admin Analytics**: [View Image](./docs/admin_analytics_mockup.png)
