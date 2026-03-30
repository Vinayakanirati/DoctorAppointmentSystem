📘 1. COMPLETE REQUIREMENTS DOCUMENT
🎯 Actors
•	Patient 
•	Doctor 
•	Admin 
________________________________________
👤 Patient Features
•	Register/Login 
•	Browse specialties 
•	Filter doctors by: 
o	Specialty 
o	Mode (ONLINE / OFFLINE) 
•	View doctor details 
•	View available slots 
•	Book appointment 
•	Receive confirmation: 
o	Online → video link 
o	Offline → clinic details 
•	View appointment history 
•	Cancel appointment 
•	Receive reminders & email notifications 
________________________________________
👨‍⚕️ Doctor Features
•	Register/Login 
•	Manage profile 
•	Create/update/delete slots 
•	Bulk slot creation 
•	View appointments (daily/upcoming) 
•	Update appointment status:
CONFIRMED → COMPLETED | CANCELLED | NO_SHOW
•	View dashboard: 
o	Daily appointments 
o	Earnings 
________________________________________
🛠️ Admin Features
•	Manage doctors & specialties 
•	View system analytics: 
o	Appointments 
o	Revenue 
•	Generate daily summaries 
________________________________________
📌 Core Business Rules
1.	Mode Isolation 
o	ONLINE doctor → only online appointments 
o	OFFLINE doctor → only offline appointments 
2.	Slot Locking 
o	Prevent double booking 
3.	Artifacts 
o	ONLINE → video link 
o	OFFLINE → clinic address 
4.	Audit Trail 
o	Track all status changes 
________________________________________
⚙️ Non-Functional Requirements
•	ACID transactions 
•	High performance queries 
•	JWT Authentication 
•	RBAC Authorization 
•	Rate limiting 
•	Data privacy & security

![FLOW DIAGRAM](C:\Users\simle\OneDrive\Desktop\doctorbs\DoctorAppointmentSystem\image.png)