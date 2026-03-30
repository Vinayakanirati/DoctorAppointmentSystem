📘 1. REQUIREMENTS DOCUMENT
🎯 Functional Requirements
Patient Features
•	Browse specialties 
•	View doctors filtered by mode (Online / Offline) 
•	View doctor availability (slots) 
•	Book appointment: 
		Select doctor 
		Select slot 
		Choose mode (Online / Offline) 
•	Receive confirmation: 
		Online → video link 
		Offline → clinic address 
•	View appointment history 
•	Receive: 
		Email confirmations 
		Appointment reminders 
________________________________________
Admin Features
•	Manage: 
		Doctors 
		Availability slots 
•	View: 
		Daily summaries (appointments & revenue) 
		Analytics dashboard 
________________________________________
System Features
•	Appointment status lifecycle:
confirmed → completed | cancelled | no-show
•	Slot locking to prevent double booking 
•	Mode-specific doctor restriction: 
		Online doctors ≠ Offline doctors 
________________________________________
⚙️ Non-Functional Requirements
•	Reliability: ACID transactions for booking 
•	Performance: Fast doctor & slot listing 
•	Security: 
		Authentication (JWT) 
		Authorization (RBAC) 
		Rate limiting 
•	Auditability: 
		Track status changes 
•	Scalability: Handle high concurrent bookings 
________________________________________

