# API Reference & Code Snippets

## Backend API Endpoints

### 1. Get All Pending Doctors
**Endpoint:** `GET /api/admin/doctors/pending`

**Authentication:** Required (Admin role)

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "name": "Dr. Rajesh Kumar",
    "email": "rajesh.kumar@hospital.com",
    "specialty": "Cardiology",
    "mode": "ONLINE",
    "fees": 250.0,
    "phone": "+91-9876543210",
    "verified": false,
    "registrationTimeAgo": "3 days ago"
  },
  {
    "id": 2,
    "name": "Dr. Priya Sharma",
    "email": "priya.sharma@hospital.com",
    "specialty": "Dermatology",
    "mode": "OFFLINE",
    "fees": 200.0,
    "phone": "+91-9123456789",
    "verified": false,
    "registrationTimeAgo": "2 hours ago"
  }
]
```

**Error Response (500):**
```json
[]
```

---

### 2. Verify a Doctor Profile
**Endpoint:** `PATCH /api/admin/doctors/{profileId}/verify`

**Authentication:** Required (Admin role)

**Path Parameters:**
- `profileId` (Long): Doctor profile ID (must be > 0)

**Response (200 OK) - Success:**
```json
{
  "message": "Doctor profile verified successfully",
  "profileId": 1,
  "doctorName": "Dr. Rajesh Kumar",
  "verified": true
}
```

**Response (400 Bad Request) - Invalid ID:**
```json
{
  "error": "Invalid profile ID provided",
  "status": 400,
  "timestamp": 1711833600000
}
```

**Response (404 Not Found) - Profile Doesn't Exist:**
```json
{
  "error": "Doctor profile not found",
  "status": 404,
  "timestamp": 1711833600000
}
```

**Response (409 Conflict) - Already Verified:**
```json
{
  "error": "Doctor profile is already verified",
  "status": 409,
  "timestamp": 1711833600000
}
```

**Response (500 Internal Server Error):**
```json
{
  "error": "Doctor profile not found or already verified",
  "status": 500,
  "timestamp": 1711833600000
}
```

---

### 3. Get Pending Doctor Count
**Endpoint:** `GET /api/admin/doctors/pending/count`

**Authentication:** Required (Admin role)

**Response (200 OK):**
```json
{
  "pendingCount": 5
}
```

---

### 4. Get Admin Analytics
**Endpoint:** `GET /api/admin/analytics`

**Authentication:** Required (Admin role)

**Response (200 OK):**
```json
{
  "totalRevenue": 15750.50,
  "totalAppointments": 125,
  "totalPatients": 45,
  "totalDoctors": 8,
  "pendingDoctorVerifications": 3,
  "appointmentsByStatus": {
    "COMPLETED": 78,
    "PENDING": 12,
    "CONFIRMED": 25,
    "CANCELLED": 10
  },
  "appointmentsBySpecialty": {
    "Dr. Rajesh Kumar": 45,
    "Dr. Priya Sharma": 38,
    "Dr. Amit Singh": 42
  },
  "revenueByDoctor": {
    "Dr. Rajesh Kumar": 5250.0,
    "Dr. Priya Sharma": 4800.0,
    "Dr. Amit Singh": 5700.50
  }
}
```

---

## Frontend Examples

### Using the Pending Doctors API

#### React Hook for Fetching Pending Doctors:
```javascript
import { useEffect, useState } from 'react';
import api from '../utils/api';

function usePendingDoctors() {
  const [doctors, setDoctors] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchPendingDoctors = async () => {
      try {
        setLoading(true);
        setError(null);
        
        const response = await api.get('/admin/doctors/pending');
        
        if (!Array.isArray(response.data)) {
          throw new Error('Invalid response format');
        }
        
        setDoctors(response.data);
      } catch (err) {
        setError(err.message || 'Failed to fetch pending doctors');
      } finally {
        setLoading(false);
      }
    };

    fetchPendingDoctors();
  }, []);

  return { doctors, loading, error };
}

// Usage:
function PendingDoctorsComponent() {
  const { doctors, loading, error } = usePendingDoctors();
  
  if (loading) return <div>Loading...</div>;
  if (error) return <div>Error: {error}</div>;
  
  return (
    <ul>
      {doctors.map(doc => (
        <li key={doc.id}>{doc.name} - {doc.specialty}</li>
      ))}
    </ul>
  );
}
```

#### Verify Doctor Function:
```javascript
async function verifyDoctor(profileId, onSuccess, onError) {
  if (!profileId || profileId <= 0) {
    onError('Invalid profile ID');
    return;
  }
  
  try {
    const response = await api.patch(`/admin/doctors/${profileId}/verify`);
    
    if (response.status === 200) {
      onSuccess(response.data);
    }
  } catch (error) {
    let errorMessage = 'Failed to verify doctor';
    
    switch (error.response?.status) {
      case 400:
        errorMessage = 'Invalid profile ID';
        break;
      case 404:
        errorMessage = 'Doctor profile not found';
        break;
      case 409:
        errorMessage = 'Doctor is already verified';
        break;
      default:
        errorMessage = error.response?.data?.error || errorMessage;
    }
    
    onError(errorMessage);
  }
}

// Usage:
verifyDoctor(
  1,
  (data) => console.log('Verified:', data),
  (error) => console.error('Error:', error)
);
```

---

## Java Backend Code Examples

### AdminService Usage:

```java
@Autowired
private AdminService adminService;

// Get all pending doctors
List<PendingDoctorDTO> pendingDoctors = adminService.getPendingDoctors();
for (PendingDoctorDTO doctor : pendingDoctors) {
    System.out.println(doctor.getName() + " - " + doctor.getSpecialty());
}

// Get pending count
long count = adminService.getPendingDoctorCount();
System.out.println("Pending verifications: " + count);

// Verify a doctor
try {
    DoctorProfile verified = adminService.verifyDoctor(1L);
    System.out.println("Verified: " + verified.getUser().getName());
} catch (BusinessException e) {
    if (e.getStatus() == HttpStatus.CONFLICT) {
        System.out.println("Doctor already verified");
    } else if (e.getStatus() == HttpStatus.NOT_FOUND) {
        System.out.println("Doctor not found");
    }
}

// Get analytics
AdminAnalyticsDTO analytics = adminService.getAnalytics();
System.out.println("Pending doctors: " + analytics.getPendingDoctorVerifications());
```

### Using the Repository:

```java
@Autowired
private DoctorProfileRepository doctorProfileRepository;

// Get all unverified doctors
List<DoctorProfile> unverified = doctorProfileRepository.findByIsVerifiedFalse();

// Get count of unverified
long count = doctorProfileRepository.countByIsVerifiedFalse();

// Get specific doctor
Optional<DoctorProfile> profile = doctorProfileRepository.findById(1L);
if (profile.isPresent()) {
    profile.get().setVerified(true);
    doctorProfileRepository.save(profile.get());
}
```

---

## Testing the API with cURL

### Get Pending Doctors:
```bash
curl -X GET http://localhost:8080/api/admin/doctors/pending \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json"
```

### Verify a Doctor (Success):
```bash
curl -X PATCH http://localhost:8080/api/admin/doctors/1/verify \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json"
```

### Verify a Doctor (Already Verified - 409):
```bash
curl -X PATCH http://localhost:8080/api/admin/doctors/1/verify \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -v
# Response: HTTP/1.1 409 Conflict
```

### Get Pending Count:
```bash
curl -X GET http://localhost:8080/api/admin/doctors/pending/count \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Get Analytics:
```bash
curl -X GET http://localhost:8080/api/admin/analytics \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## Postman Collection (JSON)

```json
{
  "info": {
    "name": "Doctor Verification API",
    "description": "Pending doctors management endpoints",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Get Pending Doctors",
      "request": {
        "method": "GET",
        "header": [
          {
            "key": "Authorization",
            "value": "Bearer {{token}}",
            "type": "text"
          }
        ],
        "url": {
          "raw": "{{baseUrl}}/api/admin/doctors/pending",
          "host": ["{{baseUrl}}"],
          "path": ["api", "admin", "doctors", "pending"]
        }
      }
    },
    {
      "name": "Verify Doctor",
      "request": {
        "method": "PATCH",
        "header": [
          {
            "key": "Authorization",
            "value": "Bearer {{token}}",
            "type": "text"
          }
        ],
        "url": {
          "raw": "{{baseUrl}}/api/admin/doctors/1/verify",
          "host": ["{{baseUrl}}"],
          "path": ["api", "admin", "doctors", "1", "verify"]
        }
      }
    },
    {
      "name": "Get Pending Count",
      "request": {
        "method": "GET",
        "header": [
          {
            "key": "Authorization",
            "value": "Bearer {{token}}",
            "type": "text"
          }
        ],
        "url": {
          "raw": "{{baseUrl}}/api/admin/doctors/pending/count",
          "host": ["{{baseUrl}}"],
          "path": ["api", "admin", "doctors", "pending", "count"]
        }
      }
    },
    {
      "name": "Get Analytics",
      "request": {
        "method": "GET",
        "header": [
          {
            "key": "Authorization",
            "value": "Bearer {{token}}",
            "type": "text"
          }
        ],
        "url": {
          "raw": "{{baseUrl}}/api/admin/analytics",
          "host": ["{{baseUrl}}"],
          "path": ["api", "admin", "analytics"]
        }
      }
    }
  ]
}
```

---

## Error Handling Matrix

| Status Code | Scenario | Response | Action |
|-------------|----------|----------|--------|
| **200** | Success | Verified doctor data | Update UI, refresh list |
| **400** | Invalid input | Error: Invalid profile ID | Show validation error |
| **401** | Not authenticated | Redirect to login | Handle in interceptor |
| **403** | Not authorized (not admin) | Access denied | Show permission error |
| **404** | Doctor not found | Error: Profile not found | Show "Doctor not found" |
| **409** | Already verified | Error: Already verified | Show "Already verified" |
| **500** | Server error | Error message | Show "Try again later" |

---

## Best Practices

### For Developers:

1. **Always validate IDs** before making API calls:
   ```javascript
   if (!id || id <= 0) {
     throw new Error('Invalid ID');
   }
   ```

2. **Handle specific error codes**:
   ```javascript
   if (error.response?.status === 409) {
     // Already verified - refresh and show info
   } else if (error.response?.status === 404) {
     // Not found - remove from list
   }
   ```

3. **Use DTOs** instead of entities in responses

4. **Implement proper logging**:
   ```java
   log.info("Action performed");
   log.warn("Warning situation");
   log.error("Error occurred", exception);
   ```

5. **Use transactions** for data consistency:
   ```java
   @Transactional
   public void handleVerification(Long id) {
     // Operations here are atomic
   }
   ```

### For API Consumers:

1. **Always check response status codes**
2. **Validate response data before using**
3. **Implement exponential backoff for retries**
4. **Show specific error messages to users**
5. **Handle network timeouts**

---

## Performance Considerations

### Database Query (O(n) where n = unverified doctors):
```java
// Efficient - uses database index on isVerified
List<DoctorProfile> pending = repo.findByIsVerifiedFalse();

// Inefficient - loads all doctors into memory
List<DoctorProfile> all = repo.findAll();
List<DoctorProfile> pending = all.stream()
    .filter(p -> !p.isVerified())
    .collect(toList());
```

### Frontend Rendering:
- For < 100 pending doctors: Direct rendering is fine
- For > 100 doctors: Implement virtual scrolling or pagination

### Caching Strategy:
```javascript
const CACHE_DURATION = 5 * 60 * 1000; // 5 minutes
let lastFetched = 0;
let cachedData = null;

function getPendingDoctorsWithCache() {
  const now = Date.now();
  if (cachedData && (now - lastFetched) < CACHE_DURATION) {
    return Promise.resolve(cachedData);
  }
  return api.get('/admin/doctors/pending')
    .then(res => {
      cachedData = res.data;
      lastFetched = now;
      return cachedData;
    });
}
```

---

## Summary

### Key Endpoints:
- `GET /api/admin/doctors/pending` - List all pending doctors
- `PATCH /api/admin/doctors/{id}/verify` - Verify a doctor
- `GET /api/admin/doctors/pending/count` - Quick count
- `GET /api/admin/analytics` - System analytics

### Key DTOs:
- `PendingDoctorDTO` - Safe data transfer

### Key Validations:
- Profile ID must be > 0
- Profile must exist
- Profile must not be already verified
- Email service failures don't break verification

### Key Error Codes:
- 400: Invalid input
- 404: Not found
- 409: Already verified (conflict)
- 500: Server error

**Implementation is production-ready!**
