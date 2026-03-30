# Testing Guide - Pending Doctors Implementation

## Manual Testing Checklist

### Setup
- ✅ Backend running on `http://localhost:8080`
- ✅ Frontend running on `http://localhost:5173` (or your Vite dev port)
- ✅ Database has test data with at least one unverified doctor
- ✅ Authentication token available

---

## Backend Testing

### 1. Get Pending Doctors

**Test Case 1.1: Successful Fetch with Data**
```bash
curl -X GET http://localhost:8080/api/admin/doctors/pending \
  -H "Authorization: Bearer $(YOUR_ADMIN_TOKEN)" \
  -H "Content-Type: application/json"
```

**Expected Result (200):**
```json
[
  {
    "id": 1,
    "name": "Dr. Rajesh Kumar",
    "email": "rajesh@example.com",
    "specialty": "Cardiology",
    "mode": "ONLINE",
    "fees": 250.0,
    "phone": "+91-9876543210",
    "verified": false,
    "registrationTimeAgo": "3 days ago"
  }
]
```

**Verification:**
- ✅ Response code is 200
- ✅ Response is an array
- ✅ Each doctor has required fields
- ✅ `verified` field is `false`
- ✅ Time format is human-readable

---

**Test Case 1.2: Fetch When No Pending Doctors**
- Manually verify all pending doctors first
- Then call the endpoint
- **Expected Result:** Empty array `[]` with 200 status

---

**Test Case 1.3: Unauthorized Access (Non-Admin)**
```bash
curl -X GET http://localhost:8080/api/admin/doctors/pending \
  -H "Authorization: Bearer $(PATIENT_TOKEN)"
```

**Expected Result (403):**
Access denied - Forbidden

---

### 2. Verify Doctor Profile

**Test Case 2.1: Successful Verification**
```bash
curl -X PATCH http://localhost:8080/api/admin/doctors/1/verify \
  -H "Authorization: Bearer $(ADMIN_TOKEN)" \
  -H "Content-Type: application/json"
```

**Expected Result (200):**
```json
{
  "message": "Doctor profile verified successfully",
  "profileId": 1,
  "doctorName": "Dr. Rajesh Kumar",
  "verified": true
}
```

**Verification:**
- ✅ Response code is 200
- ✅ Message indicates success
- ✅ Doctor name is correct
- ✅ `verified` field is `true`

---

**Test Case 2.2: Verify Non-Existent Doctor**
```bash
curl -X PATCH http://localhost:8080/api/admin/doctors/9999/verify \
  -H "Authorization: Bearer $(ADMIN_TOKEN)"
```

**Expected Result (404):**
```json
{
  "error": "Doctor profile not found or already verified",
  "status": 404,
  "timestamp": 1711833600000
}
```

---

**Test Case 2.3: Verify Already Verified Doctor**
- First verify doctor with ID 1
- Try to verify the same doctor again
```bash
curl -X PATCH http://localhost:8080/api/admin/doctors/1/verify \
  -H "Authorization: Bearer $(ADMIN_TOKEN)"
```

**Expected Result (409):**
```json
{
  "error": "Doctor profile is already verified",
  "status": 409,
  "timestamp": 1711833600000
}
```

**Verification:**
- ✅ Status code is 409 (Conflict)
- ✅ Error message clearly indicates already verified
- ✅ Prevents duplicate verification

---

**Test Case 2.4: Invalid Profile ID (Negative)**
```bash
curl -X PATCH http://localhost:8080/api/admin/doctors/-1/verify \
  -H "Authorization: Bearer $(ADMIN_TOKEN)"
```

**Expected Result (400):**
```json
{
  "error": "Invalid profile ID provided",
  "status": 400,
  "timestamp": 1711833600000
}
```

---

**Test Case 2.5: Invalid Profile ID (Zero)**
```bash
curl -X PATCH http://localhost:8080/api/admin/doctors/0/verify \
  -H "Authorization: Bearer $(ADMIN_TOKEN)"
```

**Expected Result (400):** Invalid profile ID error

---

### 3. Pending Doctor Count

**Test Case 3.1: Get Count**
```bash
curl -X GET http://localhost:8080/api/admin/doctors/pending/count \
  -H "Authorization: Bearer $(ADMIN_TOKEN)"
```

**Expected Result (200):**
```json
{
  "pendingCount": 3
}
```

---

### 4. Analytics with Pending Count

**Test Case 4.1: Get Analytics**
```bash
curl -X GET http://localhost:8080/api/admin/analytics \
  -H "Authorization: Bearer $(ADMIN_TOKEN)"
```

**Expected Result (200):**
```json
{
  "totalRevenue": 15750.50,
  "totalAppointments": 125,
  "totalPatients": 45,
  "totalDoctors": 8,
  "pendingDoctorVerifications": 2,
  ...
}
```

**Verification:**
- ✅ `pendingDoctorVerifications` matches actual unverified count
- ✅ Updated after verification

---

## Frontend Testing

### 1. Admin Dashboard Load

**Test Case 1.1: Dashboard Loads Successfully**
1. Navigate to Admin Dashboard
2. Wait for data to load
3. **Verify:**
   - ✅ Loading message appears briefly
   - ✅ Stats cards display correctly
   - ✅ Charts render
   - ✅ Pending doctors section shows

---

**Test Case 1.2: Dashboard Error Handling**
1. Stop backend server
2. Refresh page
3. **Verify:**
   - ✅ Error banner appears
   - ✅ Error message is user-friendly
   - ✅ "Retry" button is present
   - ✅ Click retry after restarting server should work

---

### 2. Pending Doctors Table

**Test Case 2.1: Display Pending Doctors**
1. Load Admin Dashboard with pending doctors
2. **Verify:**
   - ✅ Table displays all pending doctors
   - ✅ Columns show: Name, Email, Specialty, Mode, Fees, Registered
   - ✅ Doctor names are correct
   - ✅ Specialties match backend data
   - ✅ Mode shows as ONLINE/OFFLINE with color
   - ✅ Registration time shows as "X days ago"

---

**Test Case 2.2: Empty Pending Doctors**
1. Verify all pending doctors
2. Refresh page
3. **Verify:**
   - ✅ Message shows "No pending doctor profiles to verify."
   - ✅ Checkmark icon displays
   - ✅ Table is not shown
   - ✅ Badge showing "Action Needed" disappears

---

### 3. Verify Button Behavior

**Test Case 3.1: Verify Doctor - Success**
1. Click "Verify Profile" button for any pending doctor
2. **Verify:**
   - ✅ Button text changes to "Verifying..."
   - ✅ Button becomes disabled
   - ✅ Page refreshes after verification
   - ✅ Verified doctor disappears from table
   - ✅ Badge count decreases

---

**Test Case 3.2: Verify Doctor - Already Verified**
1. Open browser console
2. Manually trigger verify on same doctor twice rapidly
3. **Verify:**
   - ✅ Error message appears: "Doctor profile is already verified"
   - ✅ Only shows once (not for both requests)
   - ✅ Error clears after 5 seconds
   - ✅ Page doesn't refresh unnecessarily

---

**Test Case 3.3: Verify Doctor - Network Error**
1. Open Developer Tools (F12)
2. Go to Network tab
3. Throttle connection to "Offline"
4. Click verify button
5. **Verify:**
   - ✅ Error message appears
   - ✅ Button remains accessible
   - ✅ Can retry after going online

---

**Test Case 3.4: Verify Doctor - Multiple Doctors**
1. Have 3+ pending doctors
2. Click verify on doctor 1, then quickly on doctor 2 (before first completes)
3. **Verify:**
   - ✅ Only the clicked doctor shows "Verifying..."
   - ✅ Other buttons remain clickable
   - ✅ Each completes independently
   - ✅ Table updates correctly

---

### 4. Data Validation

**Test Case 4.1: Missing Doctor Data**
1. Edit backend to return incomplete DTO (mock for testing)
2. **Verify:**
   - ✅ Missing fields show "N/A"
   - ✅ No console errors
   - ✅ Table renders correctly

---

**Test Case 4.2: Null Registration Time**
1. Test with doctor that has null createdAt
2. **Verify:**
   - ✅ Shows "Unknown" instead of error
   - ✅ Table displays normally

---

### 5. API Response Validation

**Test Case 5.1: Invalid Response Format**
1. Intercept API response
2. Return non-array for doctors endpoint
3. **Verify:**
   - ✅ Error banner shows
   - ✅ Error message: "Invalid pending doctors data format"
   - ✅ Retry button works

---

**Test Case 5.2: Missing Analytics Data**
1. Return null for analytics
2. **Verify:**
   - ✅ Stats cards show fallback values
   - ✅ Charts show empty state
   - ✅ No crashes

---

## Integration Testing

### Test Flow 1: Complete Verification Process
1. ✅ Admin logs in
2. ✅ Opens Admin Dashboard
3. ✅ Sees pending doctors list
4. ✅ Clicks verify on first doctor
5. ✅ Doctor status updates to verified
6. ✅ Doctor removed from pending list
7. ✅ Analytics updated
8. ✅ If available, verify doctor receives email

---

### Test Flow 2: Error Recovery
1. ✅ Trigger network error during verification
2. ✅ Error message appears
3. ✅ Restore connection
4. ✅ Click retry (or refresh)
5. ✅ Page recovers
6. ✅ Can perform operations normally

---

### Test Flow 3: Concurrent Users
1. ✅ Open Admin Dashboard in 2 browser windows
2. ✅ In Window 1, verify a doctor
3. ✅ In Window 2, check if list updates automatically
   - Note: Manual refresh needed for now (no WebSocket)
4. ✅ Both windows show consistent state after refresh

---

## Performance Testing

### Test Case 1: Load with 100 Pending Doctors
1. Insert 100 pending doctors into database
2. Load Admin Dashboard
3. **Verify:**
   - ✅ Page loads in < 3 seconds
   - ✅ Table scrolls smoothly
   - ✅ No lag when clicking buttons

---

### Test Case 2: Rapid Verification Clicks
1. Click verify button rapidly 10 times on different doctors
2. **Verify:**
   - ✅ All requests complete successfully
   - ✅ No race conditions
   - ✅ No duplicate verifications
   - ✅ All doctors verified

---

## Security Testing

### Test Case 1: SQL Injection Prevention
```
Profile ID: 1 OR 1=1; DROP TABLE doctors;--
```
**Verify:**
- ✅ Status code 400
- ✅ Invalid input error
- ✅ Database unaffected

---

### Test Case 2: Authentication Required
```bash
curl -X GET http://localhost:8080/api/admin/doctors/pending
# No Authorization header
```

**Expected Result (401/403):**
Unauthorized or Forbidden

---

### Test Case 3: Authorization - Patient Role
1. Login as patient
2. Try to access `/api/admin/doctors/pending`
3. **Verify:**
   - ✅ Access denied
   - ✅ 403 status code

---

## Database Testing

### Test Case 1: Verify Updates Database
1. Check doctors table before verification
2. Verify a doctor via API
3. Check database - `is_verified` should be `true`
4. **Verify:**
   - ✅ Database updated correctly
   - ✅ No orphaned records

---

### Test Case 2: Email Sent
1. Verify a doctor
2. Check email service logs
3. **Verify:**
   - ✅ Email was sent to doctor
   - ✅ Email contains correct doctor name

---

## Test Data Setup

### SQL to Create Test Data:
```sql
-- Create test users (doctors)
INSERT INTO users (email, password, name, phone, role, created_at) VALUES
('dr1@test.com', 'hashed_pwd', 'Dr. Test One', '9876543210', 'DOCTOR', NOW()),
('dr2@test.com', 'hashed_pwd', 'Dr. Test Two', '9876543211', 'DOCTOR', NOW()),
('dr3@test.com', 'hashed_pwd', 'Dr. Test Three', '9876543212', 'DOCTOR', NOW());

-- Create doctor profiles (unverified)
INSERT INTO doctor_profiles (user_id, specialty, mode, fees, is_verified) VALUES
(10, 'Cardiology', 'ONLINE', 250.0, false),
(11, 'Neurology', 'OFFLINE', 300.0, false),
(12, 'Dermatology', 'ONLINE', 200.0, false);
```

---

## Regression Testing Checklist

After any changes, verify:

- ✅ Existing verified doctors are not affected
- ✅ Other admin functions still work (Analytics)
- ✅ Patient dashboard unaffected
- ✅ Doctor dashboard unaffected
- ✅ Authentication/Authorization still working
- ✅ Database integrity maintained
- ✅ Performance not degraded

---

## Final Sign-Off Checklist

Before production deployment:

- [ ] All unit tests pass
- [ ] All integration tests pass
- [ ] Manual smoke tests complete
- [ ] Performance tests acceptable
- [ ] Security tests pass
- [ ] Database migrations tested
- [ ] Rollback procedure documented
- [ ] Email notifications working
- [ ] Logging comprehensive
- [ ] Documentation updated

---

## Troubleshooting

### Common Issues:

#### Issue: "Doctor profile not found" even with valid ID
**Solution:** Check if doctorProfileRepository returns null. Add debug logging.

#### Issue: Already verified error on first verification
**Solution:** Check if multiple requests sent simultaneously. Add retry logic.

#### Issue: Email not sent but verification succeeds
**Solution:** This is expected behavior. Email failure doesn't block verification.

#### Issue: Browser shows different data than API
**Solution:** Check response validation. Add console.log to verify data format.

#### Issue: Button doesn't disable during verification
**Solution:** Check React state update. Ensure verifyingId state updates properly.

---

## Performance Benchmarks

Expected response times:

| Endpoint | Response Time | Notes |
|----------|---------------|-------|
| GET /doctors/pending | < 200ms | With < 100 doctors |
| PATCH /doctors/verify | < 150ms | Excluding email |
| GET /analytics | < 300ms | With full data |

---

## Success Criteria

Implementation is successful when:

✅ All pending doctors display correctly
✅ Verification works without errors
✅ Error handling is user-friendly
✅ Performance is acceptable
✅ Security validations pass
✅ No race conditions
✅ Database consistency maintained
✅ Email notifications sent
✅ Logging comprehensive
✅ Code is well-commented

**All criteria should pass before deployment!**
