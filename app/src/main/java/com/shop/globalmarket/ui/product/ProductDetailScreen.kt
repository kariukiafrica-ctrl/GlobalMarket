package com.shop.globalmarket.ui.product

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.shop.globalmarket.data.model.Product
import com.shop.globalmarket.data.repository.ProductRepository
import com.shop.globalmarket.ui.wishlist.WishlistViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProductDetailViewModel : ViewModel() {
    private val repository = ProductRepository()
    private val _product = MutableStateFlow<Product?>(null)
    val product: StateFlow<Product?> = _product

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadProduct(productId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _product.value = repository.getProductById(productId)
            _isLoading.value = false
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: String,
    onBack: () -> Unit,
    onAddToCart: (Product) -> Unit,
    onChatWithSeller: (String, String, String, String) -> Unit,
    viewModel: ProductDetailViewModel = viewModel(),
    wishlistViewModel: WishlistViewModel = viewModel()
) {
    val product by viewModel.product.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val wishlistItems by wishlistViewModel.wishlistItems.collectAsState()
    val isWishlisted = product?.let { p -> wishlistItems.any { it.id == p.id } } ?: false

    LaunchedEffect(productId) {
        viewModel.loadProduct(productId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Product Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    product?.let { p ->
                        IconButton(onClick = { wishlistViewModel.toggleWishlist(p) }) {
                            Icon(
                                imageVector = if (isWishlisted) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Toggle Wishlist",
                                tint = if (isWishlisted) Color.Red else LocalContentColor.current
                            )
                        }
                        IconButton(onClick = { onChatWithSeller(p.sellerId, "Seller of ${p.name}", p.id, p.name) }) {
                            Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Chat with Seller")
                        }
                    }
                }
            )
        },
        bottomBar = {
            product?.let { p ->
                BottomAppBar {
                    Button(
                        onClick = { onAddToCart(p) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Add to Cart - ${p.formattedPrice}")
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (product != null) {
                val p = product!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    AsyncImage(
                        model = p.imageUrl,
                        contentDescription = p.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        contentScale = ContentScale.Crop
                    )

                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = p.name,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = p.formattedPrice,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.ExtraBold
                            )
                            if (p.discountPercentage > 0) {
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = p.formattedDiscountedPrice,
                                    style = MaterialTheme.typography.bodyMedium.copy(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough),
                                    color = Color.Gray
                                )
                                Spacer(Modifier.width(8.dp))
                                Badge(containerColor = Color.Red, contentColor = Color.White) {
                                    Text("-${p.discountPercentage.toInt()}%")
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = "Category: ${p.category}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                        Text(
                            text = "Description",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Text(
                            text = p.description,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(text = "Rating: ⭐ ${p.rating}", fontWeight = FontWeight.Bold)
                                    Text(text = "Based on ${p.reviewCount} reviews", style = MaterialTheme.typography.labelSmall)
                                }
                                TextButton(onClick = { /* Navigate to reviews */ }) {
                                    Text("View All")
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text(
                            text = "Delivery Information",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Standard delivery within 3-5 business days. Returns accepted within 7 days of delivery.",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            } else {
                Text("Product not found", modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}
