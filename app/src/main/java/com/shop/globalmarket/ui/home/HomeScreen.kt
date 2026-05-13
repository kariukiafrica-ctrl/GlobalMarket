package com.shop.globalmarket.ui.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.shop.globalmarket.data.model.Product
import com.shop.globalmarket.ui.components.ProductCard
import com.shop.globalmarket.ui.wishlist.WishlistViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    initialCategory: String? = null,
    onProductClick: (Product) -> Unit,
    onCartClick: () -> Unit,
    onProfileClick: () -> Unit,
    onAddToCart: (Product) -> Unit,
    viewModel: HomeViewModel = viewModel(),
    wishlistViewModel: WishlistViewModel = viewModel()
) {
    val products by viewModel.products.collectAsState()
    val recommendations by viewModel.recommendedProducts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val wishlistItems by wishlistViewModel.wishlistItems.collectAsState()
    var isSearchActive by remember { mutableStateOf(false) }

    LaunchedEffect(initialCategory) {
        if (initialCategory != null) {
            viewModel.onCategorySelected(initialCategory)
        }
    }

    Scaffold(
        topBar = {
            if (isSearchActive) {
                TopAppBar(
                    title = {
                        TextField(
                            value = searchQuery,
                            onValueChange = { viewModel.onSearchQueryChange(it) },
                            placeholder = { Text("Search GlobalMarket...") },
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
                        BadgedBox(
                            badge = { Badge { Text("3") } },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            IconButton(onClick = onCartClick) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = "Cart")
                            }
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
                    .fillMaxSize()
            ) {
                // Category Tabs (Like Jumia/Amazon)
                ScrollableTabRow(
                    selectedTabIndex = viewModel.categories.indexOf(selectedCategory).coerceAtLeast(0),
                    edgePadding = 16.dp,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    divider = {}
                ) {
                    viewModel.categories.forEachIndexed { index, category ->
                        Tab(
                            selected = selectedCategory == category,
                            onClick = { viewModel.onCategorySelected(category) },
                            text = { Text(category) }
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .weight(1f)
                ) {
                    if (searchQuery.isEmpty() && selectedCategory == "All") {
                        // 1. Promotional Banner (Hybrid Feature: Like Shopee/Lazada)
                        BannerCarousel()

                        // 2. Flash Sales (Hybrid Feature: Like Jumia/Amazon)
                        FlashSaleSection(
                            products = products.shuffled().take(5),
                            onProductClick = onProductClick
                        )

                        // 3. AI Recommendations Section (Hybrid Feature: Like TikTok/Alibaba)
                        if (recommendations.isNotEmpty()) {
                            Text(
                                "Picked for You",
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
                                            onAddToCart = { onAddToCart(product) },
                                            isWishlisted = wishlistItems.any { it.id == product.id },
                                            onWishlistToggle = { wishlistViewModel.toggleWishlist(product) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 4. Main Product Grid
                    Text(
                        if (searchQuery.isNotEmpty()) "Search Results" 
                        else if (selectedCategory != "All") "$selectedCategory Products"
                        else "All Items",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(16.dp),
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (products.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("No products found matches your criteria.")
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
                                        onAddToCart = { onAddToCart(product) },
                                        isWishlisted = wishlistItems.any { it.id == product.id },
                                        onWishlistToggle = { wishlistViewModel.toggleWishlist(product) }
                                    )
                                }
                            }
                            if (rowProducts.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun BannerCarousel() {
    val banners = listOf(
        "https://graphicsfamily.com/wp-content/uploads/edd/2021/07/Shop-Products-Social-Media-Banner-Design-Template-scaled.jpg",
        "https://img.freepik.com/free-vector/horizontal-banner-template-online-fashion-sale_23-2148585404.jpg"
    )
    
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 0.dp)
    ) {
        items(banners) { url ->
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillParentMaxWidth()
            ) {
                AsyncImage(
                    model = url,
                    contentDescription = "Promo Banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
fun FlashSaleSection(products: List<Product>, onProductClick: (Product) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f))
            .padding(vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.FlashOn, contentDescription = null, tint = Color.Red)
                Spacer(Modifier.width(8.dp))
                Text(
                    "FLASH SALE",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Red
                )
            }
            Text("Ends in 02:45:12", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(products) { product ->
                Column(
                    modifier = Modifier
                        .width(120.dp)
                        .clickable { onProductClick(product) }
                ) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Text(
                        product.formattedPrice,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 14.sp
                    )
                    LinearProgressIndicator(
                        progress = { 0.7f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        color = Color.Red,
                        trackColor = Color.LightGray,
                    )
                    Text("70% Sold", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
