package com.globalmarket.service

import com.globalmarket.client.MpesaClient
import com.globalmarket.config.MpesaConfig
import com.globalmarket.models.*
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.util.Base64

/**
 * M-Pesa Service - Business logic for payment operations
 * Handles STK Push, transaction queries, and validation
 */
class MpesaService(
    private val client: MpesaClient,
    private val config: MpesaConfig = MpesaConfig()
) {
    
    /**
     * Initiate STK Push payment request
     * Shows payment prompt on customer's phone
     */
    suspend fun initiatePayment(
        shortCode: String = "174379",
        amount: String,
        phoneNumber: String,
        callbackUrl: String,
        accountReference: String = "GlobalMarket"
    ): Result<StkPushResponse> = try {
        // Validate inputs
        if (amount.toIntOrNull() == null || amount.toInt() < 1) {
            return Result.failure(Exception("Invalid amount"))
        }
        
        val formattedPhone = client.formatPhoneNumber(phoneNumber)
        if (!isValidPhoneNumber(formattedPhone)) {
            return Result.failure(Exception("Invalid phone number"))
        }
        
        val timestamp = client.getTimestamp()
        val password = generatePassword(shortCode, timestamp)
        
        val request = StkPushRequest(
            businessShortCode = shortCode,
            password = password,
            timestamp = timestamp,
            amount = amount,
            partyA = formattedPhone,
            partyB = shortCode,
            phoneNumber = formattedPhone,
            callBackUrl = callbackUrl,
            accountReference = accountReference
        )
        
        val response = client.post<StkPushResponse>(config.stkPushUrl, request)
        Result.success(response)
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    /**
     * Query transaction status
     */
    suspend fun queryTransactionStatus(
        shortCode: String = "174379",
        checkoutRequestID: String
    ): Result<StkQueryResponse> = try {
        val timestamp = client.getTimestamp()
        val password = generatePassword(shortCode, timestamp)
        
        val request = StkQueryRequest(
            businessShortCode = shortCode,
            password = password,
            timestamp = timestamp,
            checkoutRequestID = checkoutRequestID
        )
        
        val response = client.post<StkQueryResponse>(config.queryUrl, request)
        Result.success(response)
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    /**
     * Generate password for API requests
     * Format: base64(ShortCode + Passkey + Timestamp)
     */
    private fun generatePassword(shortCode: String, timestamp: String): String {
        val passkey = "bfb279f9aa9bdbcf158e97dd1a503017"  // Replace with your actual passkey
        val password = "$shortCode$passkey$timestamp"
        return Base64.getEncoder().encodeToString(password.toByteArray())
    }
    
    /**
     * Validate phone number format
     */
    private fun isValidPhoneNumber(phoneNumber: String): Boolean {
        return phoneNumber.matches(Regex("^254[0-9]{9}$"))
    }
    
    /**
     * Process callback from M-Pesa
     */
    fun processCallback(callbackData: MpesaCallbackData): PaymentResult {
        val stkCallback = callbackData.body?.stkCallback
        
        return if (stkCallback != null) {
            PaymentResult(
                success = stkCallback.resultCode == 0,
                resultCode = stkCallback.resultCode,
                description = stkCallback.resultDesc,
                merchantRequestID = stkCallback.merchantRequestID,
                checkoutRequestID = stkCallback.checkoutRequestID,
                metadata = extractMetadata(stkCallback.callbackMetadata)
            )
        } else {
            PaymentResult(
                success = false,
                description = callbackData.error ?: "Unknown error"
            )
        }
    }
    
    /**
     * Extract transaction metadata from callback
     */
    private fun extractMetadata(callbackMetadata: CallbackMetadata?): Map<String, String> {
        val metadata = mutableMapOf<String, String>()
        callbackMetadata?.items?.forEach { item ->
            if (item.value != null) {
                metadata[item.name] = item.value!!
            }
        }
        return metadata
    }
}

/**
 * Result data class for payment operations
 */
data class PaymentResult(
    val success: Boolean,
    val resultCode: Int = -1,
    val description: String,
    val merchantRequestID: String = "",
    val checkoutRequestID: String = "",
    val metadata: Map<String, String> = emptyMap()
)
