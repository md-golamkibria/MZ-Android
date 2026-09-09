# Login Failure Debug Guide

## 🚨 **Issue**: Login failing after logout

### **🔍 Debug Steps:**

1. **Build and run** the app with enhanced logging
2. **Try to login** with your credentials
3. **Check logcat** for these specific logs:

### **Expected Login Flow Logs:**

```
=== LOGIN ATTEMPT ===
URL: https://moviezoneonline.vercel.app/api/users/signin
Username: [your_username]
Request: LoginRequest(username=..., password=...)

=== REQUEST HEADERS ===
URL: https://moviezoneonline.vercel.app/api/users/signin
Method: POST
Header: Content-Type = application/json
Header: Accept = application/json

=== RESPONSE RECEIVED ===
Status Code: [200 or error code]
Is Successful: [true or false]
```

### **If Login Succeeds (Status 200):**
```
=== LOGIN SUCCESS ===
User ID: [user_id]
Username: [username]
Profile Image: [URL or null]

=== SAVING COOKIES ===
Cookie: mz_token = [JWT_token...]
```

### **If Login Fails (Status 401/400/500):**
```
=== LOGIN FAILED ===
Status: [error_code]
Error Body: [error_message]
Parsed Error: [specific_error]
```

## 🎯 **Common Login Failure Causes:**

### **1. Status 401 - Invalid Credentials:**
- **Error**: "Invalid credentials"
- **Solution**: Double-check username/password
- **Test**: Try logging into web app first

### **2. Status 400 - Bad Request:**
- **Error**: "Username and password are required"
- **Solution**: Ensure fields are not empty

### **3. Status 500 - Server Error:**
- **Error**: "Internal Server Error"
- **Solution**: Backend might be down

### **4. HTML Response (Wrong Endpoint):**
- **Error**: "Backend API endpoints not available"
- **Solution**: API endpoint might have changed

## 🔧 **Quick Fixes:**

### **1. Clear App Data:**
- Android Settings → Apps → MovieZone → Storage → Clear Data
- Fresh start with no cached data

### **2. Test Web App:**
- Go to: https://moviezoneonline.vercel.app
- Try logging in with same credentials
- Verify backend is working

### **3. Check Network:**
- Ensure device has internet connection
- Try on different network (WiFi vs Mobile)

### **4. Test Different Credentials:**
- Try creating new account on web
- Test with fresh credentials

## 📱 **What to Share:**

After testing, share these specific logs from logcat:
1. **Login attempt logs** (URL, username, request details)
2. **Response status code** and error message
3. **Any specific error** from "Parsed Error" line

This will show exactly why the login is failing!

## 🚀 **Expected Working Result:**

**Success Flow**: Login → Status 200 → JWT cookie saved → Profile picture loaded → Wishlist accessible

The enhanced logging will pinpoint the exact login failure cause!
