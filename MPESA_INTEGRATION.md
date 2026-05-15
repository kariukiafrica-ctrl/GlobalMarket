# M-Pesa Daraja API Integration Guide

## Overview
GlobalMarket now has complete M-Pesa Daraja API integration for STK Push payments.

## Credentials Embedded
- **Consumer Key:** xaqJOzw4DUK0m83yYTSPreO7L9IwslyBfzENKt7vqrnTbKa9
- **Consumer Secret:** 327ZvAhZDrdQcGAwbHhsuWgzlxPIfpOfSpcIAzPvGJJlXFAtRJN631yt8TIDOpuq
- **Business Short Code:** 174379
- **Passkey:** bfb279f9ba9b9d1380007152005c2f38
- **Callback URL:** https://globalmarket-callback.herokuapp.com/api/payment/callback

## Files Added

### 1. Data Layer
- **MpesaApiService.kt** - Retrofit API interface
- **RetrofitClient.kt** - Retrofit singleton configuration
- **PaymentRepository.kt** - Business logic layer

### 2. UI Layer
- **PaymentScreen.kt** - Complete Compose UI with state management
- **PaymentViewModel.kt** - ViewModel for payment state

### 3. Configuration
- **app/build.gradle.kts** - Dependencies added:
  - Retrofit 2.11.0
  - OkHttp 4.12.0
  - Kotlin Coroutines
  - ViewModel & LiveData

- **AndroidManifest.xml** - Permissions added:
  - android.permission.INTERNET
  - android.permission.ACCESS_NETWORK_STATE

## Payment Flow

```
Cart → Checkout → Payment Screen → M-Pesa STK → Enter PIN → Success
```

## Features

✅ STK Push Payment
✅ Phone Number Validation (0712345678, +254712345678, 254712345678)
✅ Amount Validation
✅ Account Reference
✅ Transaction Description
✅ Error Handling & Retry
✅ 5 Payment States:
   - Idle: Form ready
   - Processing: API call in progress
   - PendingUserInput: Waiting for M-Pesa PIN
   - Success: Payment completed
   - Error: Payment failed

✅ Material3 Design
✅ Real-time Feedback
✅ Smooth Animations

## Build & Run

```bash
# Sync Gradle
File > Sync Now

# Build
./gradlew clean build

# Run
./gradlew installDebug
```

## API Endpoints Used

1. **OAuth Token:** `POST /oauth/v1/generate?grant_type=client_credentials`
2. **STK Push:** `POST /mpesa/stkpush/v1/processrequest`

## Environment

- **Base URL:** https://sandbox.safaricom.co.ke/
- **API Version:** v1
- **Mode:** Sandbox (Testing)

## Testing

Use test phone numbers:
- 0712345678 (Kenyan format)
- 254712345678 (International format)
- +254712345678 (E.164 format)

## Callback Handling

Payment status callbacks are sent to:
```
https://globalmarket-callback.herokuapp.com/api/payment/callback
```

Webhook format:
```json
{
  "Body": {
    "stkCallback": {
      "MerchantRequestID": "...",
      "CheckoutRequestID": "...",
      "ResultCode": 0,
      "ResultDesc": "The service request has been accepted for processing"
    }
  }
}
```

## Troubleshooting

### Issue: "No internet permission"
**Solution:** Check AndroidManifest.xml has internet permissions

### Issue: "API returns 401"
**Solution:** Credentials may have expired, regenerate from Safaricom Daraja portal

### Issue: "Invalid phone number format"
**Solution:** App automatically converts to 254XXXXXXXXX format. Enter as 0712345678

## Next Steps

1. ✅ Gradle Sync
2. ✅ Build Project
3. ✅ Run on Device/Emulator
4. ✅ Test Payment Flow
5. ✅ Monitor Callbacks

## Support

For M-Pesa Daraja API documentation:
https://developer.safaricom.co.ke/

For issues with this integration, check:
- LogCat for error messages
- Network logs in Retrofit interceptor
- M-Pesa Daraja documentation

---

**Integration Complete!** 🎉
Your GlobalMarket app is ready with full M-Pesa payment capability.
