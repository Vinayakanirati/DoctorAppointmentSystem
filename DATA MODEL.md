📘 **2. COMPLETE DATA MODEL**

🧩 Entity Relationship

Patient ───< Appointment >─── Doctor ─── Specialty

&#x20;                   │

&#x20;                   └── Slot

Doctor ───< Earnings

Appointment ───< AuditLog



#### **🧾 Entities**



**Patient**



* id (UUID)
* name
* email
* phone
* password
* created\_at



**Doctor**



* id (UUID)
* name
* email
* phone
* password
* specialty\_id
* mode (ONLINE / OFFLINE)
* clinic\_address
* consultation\_fee
* is\_active
* created\_at





**Specialty**





* id
* name
* Slot
* id
* doctor\_id
* start\_time
* end\_time
* is\_available





**Appointment**





* id
* patient\_id
* doctor\_id
* slot\_id
* mode
* status
* video\_link
* clinic\_address
* fee
* created\_at





**AuditLog**





* id
* appointment\_id
* status
* timestamp





**DoctorEarnings**



* id
* doctor\_id
* date
* total\_earnings

