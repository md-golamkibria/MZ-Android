# Backend Integration Debug Guide

## Current Issues:

### 1. **Wishlist Error: "Failed to get wishlist"**
- **Problem**: Backend API call returning error
- **Likely Causes**:
  - Authentication cookies not being sent properly
  - 401/403 authentication/authorization error
  - Different API endpoint expectations

### 2. **Profile Picture: Showing "DI" avatar instead of real image**
- **Problem**: Backend profile image not loading
- **Current**: Generated avatar based on username "Dipto"
- **Expected**: Real profile picture from web backend

## Debug Steps Added:

### **Enhanced Logging**:
1. **Cookie Management**: Now logs all cookies saved/loaded
2. **Wishlist API**: Detailed request/response logging
3. **Profile Picture**: Image loading success/error logging
4. **Authentication**: Enhanced login response logging

### **Error Handling**:
- Specific error messages for 401 (auth required) and 403 (forbidden)
- Detailed exception logging with stack traces
- Better error reporting to UI

## Testing Instructions:

### **1. Check Logcat for Authentication**:
Look for these logs when logging in:
```
AuthRepository: API User profileImage: [URL or null]
AuthRepository: User profilePicture: [URL or null]
CookieJar: Saving X cookies for moviezoneonline.vercel.app
```

### **2. Check Logcat for Wishlist**:
Look for these logs when accessing favorites:
```
=== FETCHING WISHLIST ===
URL: https://moviezoneonline.vercel.app/api/wishlist
=== WISHLIST RESPONSE ===
Status Code: [200 or error code]
```

### **3. Check Profile Picture Loading**:
Look for these logs:
```
HomeScreen: Profile picture URL: [URL]
HomeScreen: Profile picture loaded successfully
```

## Potential Solutions:

### **If Wishlist Returns 401/403**:
- Authentication cookies not being sent
- Backend session expired
- Different authentication flow needed

### **If Profile Picture URL is Null**:
- Backend not returning profileImage field
- Field mapping issue (profileImage vs profilePicture)
- User doesn't have profile picture set in web app

### **If Profile Picture URL Exists but Image Doesn't Load**:
- CORS issues with image loading
- Invalid/expired image URL
- Network security blocking image requests

## Quick Test:

### **Manual Backend Test**:
You can test the backend directly:
```bash
# Test login
curl -X POST https://moviezoneonline.vercel.app/api/users/signin \
  -H "Content-Type: application/json" \
  -d '{"username":"Dipto","password":"your_password"}' \
  -c cookies.txt

# Test wishlist (using saved cookies)
curl -X GET https://moviezoneonline.vercel.app/api/wishlist \
  -b cookies.txt
```

## Next Steps:

1. **Login with your real credentials** (username: Dipto, password: your actual password)
2. **Check logcat** for detailed error messages
3. **Share the specific error codes/messages** from logcat
4. **Verify your web app** still works with same credentials

The enhanced logging will show us exactly where the integration is failing!
