package com.shop.globalmarket.ui.orders

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shop.globalmarket.data.model.Order
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyOrdersScreen(
    onBack: () -> Unit,
    viewModel: OrderViewModel = viewModel()
) {
    val orders by viewModel.orders.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Orders") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (orders.isEmpty()) {
                Text("No orders found", modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(orders) { order ->
                        OrderItem(order)
                    }
                }
            }
        }
    }
}

@Composable
fun OrderItem(order: Order) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Order ID: ${order.id.takeLast(8).uppercase()}", fontWeight = FontWeight.Bold)
                Text(
                    text = order.status.name,
                    color = when (order.status.name) {
                        "DELIVERED" -> MaterialTheme.colorScheme.primary
                        "CANCELLED" -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.secondary
                    },
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            order.products.forEach { item ->
                Text("${item.quantity}x ${item.productName}", style = MaterialTheme.typography.bodyMedium)
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val date = Date(order.timestamp)
                val format = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                Text(format.format(date), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
                Text("Total: Ksh ${String.format("%,.0f", order.totalAmount)}", fontWeight = FontWeight.Bold)
            }
        }
    }
}
