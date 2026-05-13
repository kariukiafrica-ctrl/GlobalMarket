package com.shop.globalmarket.ui.checkout

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
    onOrderPlaced: () -> Unit,
    cartViewModel: CartViewModel
) {
    var phoneNumber by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    val paymentMethods = listOf("Mpesa", "Airtel Money", "Card")
    var selectedMethod by remember { mutableStateOf(paymentMethods[0]) }

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
        ) {
            Text("Shipping Information", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Delivery Address") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )

            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text("Phone Number (for Payment)") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text("Payment Method", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            
            paymentMethods.forEach { method ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = (method == selectedMethod),
                            onClick = { selectedMethod = method }
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (method == selectedMethod),
                        onClick = { selectedMethod = method }
                    )
                    Text(text = method, modifier = Modifier.padding(start = 16.dp))
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
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
                        Text("Total", fontWeight = FontWeight.Bold)
                        Text("Ksh ${String.format("%,.0f", cartViewModel.subtotal + 200)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            Button(
                onClick = onOrderPlaced,
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                enabled = address.isNotEmpty() && phoneNumber.isNotEmpty()
            ) {
                Text("Place Order")
            }
        }
    }
}
