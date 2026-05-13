package com.shop.globalmarket.ui.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shop.globalmarket.data.model.Order
import com.shop.globalmarket.data.model.OrderStatus
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
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("No orders found", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(orders) { order ->
                        OrderCard(
                            order = order,
                            onCancel = { viewModel.cancelOrder(order.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OrderCard(order: Order, onCancel: () -> Unit) {
    var showTracker by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Order #${order.id.takeLast(6).uppercase()}", fontWeight = FontWeight.Bold)
                    val date = Date(order.timestamp)
                    val format = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    Text(format.format(date), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
                
                StatusBadge(order.status)
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            
            order.products.forEach { item ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${item.quantity}x ${item.productName}", style = MaterialTheme.typography.bodyMedium)
                    Text("Ksh ${String.format("%,.0f", item.productPrice * item.quantity)}")
                }
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total Amount", fontWeight = FontWeight.Bold)
                Text("Ksh ${String.format("%,.0f", order.totalAmount)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (showTracker) {
                OrderTracker(status = order.status)
                Spacer(modifier = Modifier.height(16.dp))
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { showTracker = !showTracker },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (showTracker) "Hide Tracking" else "Track Order")
                }
                
                if (order.status == OrderStatus.PENDING) {
                    Button(
                        onClick = onCancel,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel Order")
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: OrderStatus) {
    val color = when (status) {
        OrderStatus.PENDING -> Color(0xFFFFA000)
        OrderStatus.PROCESSING -> Color(0xFF1976D2)
        OrderStatus.SHIPPED -> Color(0xFF7B1FA2)
        OrderStatus.DELIVERED -> Color(0xFF388E3C)
        OrderStatus.CANCELLED -> Color(0xFFD32F2F)
    }
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = CircleShape,
        border = androidx.compose.foundation.BorderStroke(1.dp, color)
    ) {
        Text(
            text = status.name,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun OrderTracker(status: OrderStatus) {
    val steps = listOf(
        Triple("Placed", Icons.Default.PendingActions, OrderStatus.PENDING),
        Triple("Processing", Icons.Default.PendingActions, OrderStatus.PROCESSING),
        Triple("Shipped", Icons.Default.LocalShipping, OrderStatus.SHIPPED),
        Triple("Delivered", Icons.Default.CheckCircle, OrderStatus.DELIVERED)
    )

    val currentStepIndex = when (status) {
        OrderStatus.PENDING -> 0
        OrderStatus.PROCESSING -> 1
        OrderStatus.SHIPPED -> 2
        OrderStatus.DELIVERED -> 3
        OrderStatus.CANCELLED -> -1
    }

    if (status == OrderStatus.CANCELLED) {
        Text("This order has been cancelled.", color = MaterialTheme.colorScheme.error)
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            steps.forEachIndexed { index, (label, icon, _) ->
                val isActive = index <= currentStepIndex
                val color = if (isActive) MaterialTheme.colorScheme.primary else Color.Gray
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
                    Text(label, style = MaterialTheme.typography.labelSmall, color = color)
                }
                
                if (index < steps.size - 1) {
                    Box(modifier = Modifier.weight(1f).height(2.dp).background(if (index < currentStepIndex) MaterialTheme.colorScheme.primary else Color.LightGray))
                }
            }
        }
    }
}
