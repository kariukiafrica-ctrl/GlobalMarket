package com.shop.globalmarket.data

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import com.google.gson.annotations.SerializedName

interface MpesaApiService {
    @POST("oauth/v1/generate?grant_type=client_credentials")
    suspend fun getAccessToken(
        @Header("Authorization") auth: String
    ): AccessTokenResponse

    @POST("mpesa/stkpush/v1/processrequest")
    suspend fun initiatePayment(
        @Header("Authorization") auth: String,
        @Body request: StkPushRequest
    ): StkPushResponse
}

data class AccessTokenResponse(
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("expires_in")
    val expiresIn: Int
)

data class StkPushRequest(
    @SerializedName("BusinessShortCode")
    val businessShortCode: String,
    @SerializedName("Password")
    val password: String,
    @SerializedName("Timestamp")
    val timestamp: String,
    @SerializedName("TransactionType")
    val transactionType: String = "CustomerPayBillOnline",
    @SerializedName("Amount")
    val amount: Int,
    @SerializedName("PartyA")
    val partyA: String,
    @SerializedName("PartyB")
    val partyB: String,
    @SerializedName("PhoneNumber")
    val phoneNumber: String,
    @SerializedName("CallBackURL")
    val callBackUrl: String,
    @SerializedName("AccountReference")
    val accountReference: String,
    @SerializedName("TransactionDesc")
    val transactionDesc: String
)

data class StkPushResponse(
    @SerializedName("ResponseCode")
    val responseCode: String,
    @SerializedName("ResponseDescription")
    val responseDescription: String,
    @SerializedName("MerchantRequestID")
    val merchantRequestId: String,
    @SerializedName("CheckoutRequestID")
    val checkoutRequestId: String
)
