package com.shop.globalmarket.ui.seller

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shop.globalmarket.data.model.ChatMessage
import com.shop.globalmarket.data.model.Order
import com.shop.globalmarket.data.model.OrderStatus
import com.shop.globalmarket.ui.components.ProductCard
import com.shop.globalmarket.ui.components.StatusBadge
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerDashboardScreen(
    onBack: () -> Unit,
    onAddProduct: () -> Unit,
    onEditProduct: (String) -> Unit,
    viewModel: SellerDashboardViewModel = viewModel()
) {
    val orders by viewModel.orders.collectAsState()
    val myProducts by viewModel.myProducts.collectAsState()
    val pendingOffers by viewModel.pendingOffers.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Seller Central") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Help */ }) {
                        Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = "Help")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddProduct,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("List Product") }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
            ) {
                item {
                    Spacer(Modifier.height(16.dp))
                    Text("Business Performance", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatCard("Revenue", "Ksh ${String.format(Locale.getDefault(), "%,.0f", viewModel.totalSales)}", Modifier.weight(1f))
                        StatCard("Store Rating", "4.8 ★", Modifier.weight(1f))
                    }
                }

                // HYBRID FEATURE: Quick Actions Grid (Like Amazon/Lazada)
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Quick Actions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        QuickActionCard("Promotions", Icons.Default.Campaign, Color(0xFFE91E63)) { }
                        QuickActionCard("Analytics", Icons.Default.BarChart, Color(0xFF4CAF50)) { }
                        QuickActionCard("Finances", Icons.Default.AccountBalanceWallet, Color(0xFF2196F3)) { }
                    }
                }

                // HYBRID FEATURE: Pending Price Negotiations (Bargaining - Pinduoduo style)
                if (pendingOffers.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalOffer, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text("Active Negotiations", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            items(pendingOffers) { offer ->
                                SellerOfferCard(
                                    offer = offer,
                                    onAccept = { viewModel.updateOfferStatus(offer.id, "ACCEPTED") },
                                    onReject = { viewModel.updateOfferStatus(offer.id, "REJECTED") }
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Manage Orders (${orders.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (orders.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        ) {
                            Text(
                                "No orders yet. Try promoting your products!",
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(orders) { order ->
                        SellerOrderCard(
                            order = order,
                            onUpdateStatus = { newStatus -> 
                                viewModel.updateOrderStatus(order.id, newStatus)
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Inventory Management", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (myProducts.isEmpty()) {
                    item {
                        Text("Your shop is empty.", style = MaterialTheme.typography.bodyMedium)
                    }
                } else {
                    items(myProducts.chunked(2)) { rowProducts ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowProducts.forEach { product ->
                                Box(modifier = Modifier.weight(1f)) {
                                    Column {
                                        ProductCard(
                                            product = product,
                                            onClick = { onEditProduct(product.id) },
                                            onAddToCart = null
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceEvenly
                                        ) {
                                            TextButton(onClick = { onEditProduct(product.id) }) {
                                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(Modifier.width(4.dp))
                                                Text("Edit")
                                            }
                                            TextButton(onClick = { viewModel.deleteProduct(product.id) }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Text("Remove")
                                            }
                                        }
                                    }
                                }
                            }
                            if (rowProducts.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(48.dp))
                }
            }
        }
    }
}

@Composable
fun QuickActionCard(title: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier.width(100.dp).clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = color)
            Spacer(Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun SellerOfferCard(
    offer: ChatMessage,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier.width(260.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = offer.productName ?: "Product Offer",
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1
            )
            Text(
                text = "Offer: Ksh ${String.format(Locale.getDefault(), "%,.0f", offer.offerAmount ?: 0.0)}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Reject", style = MaterialTheme.typography.labelSmall)
                }
                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Accept", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
fun SellerOrderCard(order: Order, onUpdateStatus: (OrderStatus) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Order #${order.id.takeLast(6).uppercase()}", fontWeight = FontWeight.Bold)
                    val date = Date(order.timestamp)
                    val format = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
                    Text(format.format(date), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusBadge(order.status)
                    Box {
                        IconButton(onClick = { expanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More")
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            OrderStatus.entries.forEach { status ->
                                DropdownMenuItem(
                                    text = { Text(status.name) },
                                    onClick = {
                                        onUpdateStatus(status)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            order.products.forEach { item ->
                Text("${item.quantity}x ${item.productName}", style = MaterialTheme.typography.bodySmall)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                "Total: Ksh ${String.format(Locale.getDefault(), "%,.0f", order.totalAmount)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            if (order.deliveryAddress.isNotEmpty()) {
                Text(
                    "Ship to: ${order.deliveryAddress}, ${order.deliveryCity}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium)
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
    }
}
