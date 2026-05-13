package com.shop.globalmarket.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shop.globalmarket.data.model.Product
import com.shop.globalmarket.ui.components.ProductCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onProductClick: (Product) -> Unit,
    onCartClick: () -> Unit,
    onProfileClick: () -> Unit,
    onAddToCart: (Product) -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val products by viewModel.products.collectAsState()
    val recommendations by viewModel.recommendedProducts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var isSearchActive by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            if (isSearchActive) {
                TopAppBar(
                    title = {
                        TextField(
                            value = searchQuery,
                            onValueChange = { viewModel.onSearchQueryChange(it) },
                            placeholder = { Text("Search products...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { 
                            isSearchActive = false
                            viewModel.onSearchQueryChange("")
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Close Search")
                        }
                    }
                )
            } else {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "GlobalMarket",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    },
                    actions = {
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                        IconButton(onClick = onCartClick) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "Cart")
                        }
                        IconButton(onClick = onProfileClick) {
                            Icon(Icons.Default.Person, contentDescription = "Profile")
                        }
                    }
                )
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                // AI Recommendations Section
                if (recommendations.isNotEmpty() && searchQuery.isEmpty()) {
                    Text(
                        "Recommended for You",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(16.dp),
                        fontWeight = FontWeight.Bold
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.height(260.dp)
                    ) {
                        items(recommendations) { product ->
                            Box(modifier = Modifier.width(180.dp)) {
                                ProductCard(
                                    product = product,
                                    onClick = { 
                                        viewModel.onProductViewed(product.category)
                                        onProductClick(product) 
                                    },
                                    onAddToCart = { onAddToCart(product) }
                                )
                            }
                        }
                    }
                }

                // Featured Products Section (Main Grid)
                Text(
                    if (searchQuery.isEmpty()) "Featured Products" else "Search Results",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp),
                    fontWeight = FontWeight.Bold
                )
                
                if (products.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No products found.")
                    }
                }

                products.chunked(2).forEach { rowProducts ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowProducts.forEach { product ->
                            Box(modifier = Modifier.weight(1f)) {
                                ProductCard(
                                    product = product,
                                    onClick = { 
                                        viewModel.onProductViewed(product.category)
                                        onProductClick(product) 
                                    },
                                    onAddToCart = { onAddToCart(product) }
                                )
                            }
                        }
                        if (rowProducts.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
