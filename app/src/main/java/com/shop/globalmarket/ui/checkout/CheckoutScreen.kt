package com.shop.globalmarket.ui.checkout

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shop.globalmarket.ui.cart.CartViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    onBack: () -> Unit,
    onOrderPlaced: (String, String, String) -> Unit, // address, city, phone
    cartViewModel: CartViewModel
) {
    var phoneNumber by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    val paymentMethods = listOf("Mpesa", "Airtel Money", "Credit/Debit Card")
    var selectedMethod by remember { mutableStateOf(paymentMethods[0]) }
    var isProcessing by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Shipping Information", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Delivery Address (Street, Building, House No.)") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )

            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                label = { Text("City / Town") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )

            Button(
                onClick = { 
                    address = "123 Business Park, Tower A"
                    city = "Nairobi"
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Use My Current Location")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Payment Details", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text("Phone Number (Mpesa/Airtel)") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                placeholder = { Text("07xxxxxxxx") }
            )

            Text("Select Payment Method", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
            
            paymentMethods.forEach { method ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = (method == selectedMethod),
                            onClick = { selectedMethod = method }
                        )
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (method == selectedMethod),
                        onClick = { selectedMethod = method }
                    )
                    Text(text = method, modifier = Modifier.padding(start = 16.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Order Summary", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Subtotal")
                        Text(cartViewModel.formattedSubtotal)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Delivery Fee")
                        Text("Ksh 200")
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Payable", fontWeight = FontWeight.Bold)
                        Text("Ksh ${String.format("%,.0f", cartViewModel.subtotal + 200)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    isProcessing = true
                    onOrderPlaced(address, city, phoneNumber)
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = address.isNotEmpty() && phoneNumber.isNotEmpty() && !isProcessing
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Pay and Place Order")
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
