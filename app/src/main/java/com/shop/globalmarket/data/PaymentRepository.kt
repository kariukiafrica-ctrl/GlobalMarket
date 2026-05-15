package com.shop.globalmarket.data

import android.util.Base64
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PaymentRepository(private val apiService: MpesaApiService) {
    
    companion object {
        private const val CONSUMER_KEY = "xaqJOzw4DUK0m83yYTSPreO7L9IwslyBfzENKt7vqrnTbKa9"
        private const val CONSUMER_SECRET = "327ZvAhZDrdQcGAwbHhsuWgzlxPIfpOfSpcIAzPvGJJlXFAtRJN631yt8TIDOpuq"
        private const val BUSINESS_SHORT_CODE = "174379"
        private const val PASSKEY = "bfb279f9ba9b9d1380007152005c2f38"
        private const val CALLBACK_URL = "https://globalmarket-callback.herokuapp.com/api/payment/callback"
    }

    suspend fun initiatePayment(
        amount: Int,
        phoneNumber: String,
        accountReference: String,
        transactionDesc: String
    ): StkPushResponse {
        try {
            // Validate phone number
            val cleanPhoneNumber = phoneNumber.replace(Regex("[^0-9]"), "")
            val formattedPhone = if (cleanPhoneNumber.startsWith("0")) {
                "254" + cleanPhoneNumber.substring(1)
            } else if (cleanPhoneNumber.startsWith("254")) {
                cleanPhoneNumber
            } else {
                "254$cleanPhoneNumber"
            }

            // Get access token
            val credentials = "$CONSUMER_KEY:$CONSUMER_SECRET"
            val encodedCredentials = Base64.encodeToString(
                credentials.toByteArray(),
                Base64.NO_WRAP
            )
            
            val tokenResponse = apiService.getAccessToken(
                "Basic $encodedCredentials"
            )
            
            // Generate timestamp
            val timestamp = SimpleDateFormat(
                "yyyyMMddHHmmss",
                Locale.getDefault()
            ).format(Date())
            
            // Generate password
            val passwordString = "$BUSINESS_SHORT_CODE$PASSKEY$timestamp"
            val password = Base64.encodeToString(
                passwordString.toByteArray(),
                Base64.NO_WRAP
            )
            
            // Initiate payment
            val request = StkPushRequest(
                businessShortCode = BUSINESS_SHORT_CODE,
                password = password,
                timestamp = timestamp,
                amount = amount,
                partyA = formattedPhone,
                partyB = BUSINESS_SHORT_CODE,
                phoneNumber = formattedPhone,
                callBackUrl = CALLBACK_URL,
                accountReference = accountReference.ifEmpty { "GlobalMarket" },
                transactionDesc = transactionDesc.ifEmpty { "Purchase" }
            )
            
            return apiService.initiatePayment(
                "Bearer ${tokenResponse.accessToken}",
                request
            )
        } catch (e: Exception) {
            throw PaymentException("Failed to initiate payment: ${e.message}")
        }
    }
}

class PaymentException(message: String) : Exception(message)
