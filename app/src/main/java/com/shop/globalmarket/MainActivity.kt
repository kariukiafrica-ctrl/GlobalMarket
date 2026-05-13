package com.shop.globalmarket

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import com.shop.globalmarket.data.model.CartItem
import com.shop.globalmarket.data.model.Order
import com.shop.globalmarket.ui.admin.AdminDashboardScreen
import com.shop.globalmarket.ui.admin.ProductModerationScreen
import com.shop.globalmarket.ui.admin.SellerApprovalScreen
import com.shop.globalmarket.ui.admin.UserManagementScreen
import com.shop.globalmarket.ui.auth.LoginScreen
import com.shop.globalmarket.ui.auth.SignupScreen
import com.shop.globalmarket.ui.cart.CartScreen
import com.shop.globalmarket.ui.cart.CartViewModel
import com.shop.globalmarket.ui.chat.ChatScreen
import com.shop.globalmarket.ui.checkout.CheckoutScreen
import com.shop.globalmarket.ui.home.HomeScreen
import com.shop.globalmarket.ui.navigation.Screen
import com.shop.globalmarket.ui.orders.MyOrdersScreen
import com.shop.globalmarket.ui.orders.OrderViewModel
import com.shop.globalmarket.ui.product.ProductDetailScreen
import com.shop.globalmarket.ui.profile.ProfileScreen
import com.shop.globalmarket.ui.seller.AddProductScreen
import com.shop.globalmarket.ui.seller.EditProductScreen
import com.shop.globalmarket.ui.seller.SellerDashboardScreen
import com.shop.globalmarket.ui.settings.SettingsScreen
import com.shop.globalmarket.ui.splash.SplashScreen
import com.shop.globalmarket.ui.theme.GlobalMarketTheme
import com.shop.globalmarket.ui.categories.CategoriesScreen
import com.shop.globalmarket.ui.wishlist.WishlistScreen
import com.shop.globalmarket.ui.wishlist.WishlistViewModel
import com.shop.globalmarket.ui.checkout.CheckoutViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GlobalMarketTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GlobalMarketApp()
                }
            }
        }
    }
}

@Composable
fun GlobalMarketApp() {
    val navController = rememberNavController()
    
    // Scoped ViewModels to MainActivity to share state across screens
    val cartViewModel: CartViewModel = viewModel()
    val orderViewModel: OrderViewModel = viewModel()
    val wishlistViewModel: WishlistViewModel = viewModel()
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = currentDestination?.route?.startsWith("home") == true || 
                       currentDestination?.route in listOf(
                           Screen.Categories.route,
                           Screen.Wishlist.route,
                           Screen.Cart.route,
                           Screen.Profile.route
                       )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    Screen.bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon!!, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = currentDestination?.hierarchy?.any { 
                                it.route?.startsWith(screen.route.split("?")[0]) == true 
                            } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(onTimeout = {
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    val destination = if (currentUser != null) Screen.Home.route else Screen.Login.route
                    navController.navigate(destination) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                })
            }

            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToSignup = {
                        navController.navigate(Screen.Signup.route)
                    }
                )
            }
            
            composable(Screen.Signup.route) {
                SignupScreen(
                    onSignupSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Signup.route) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = Screen.Home.route,
                arguments = listOf(navArgument("category") { 
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                })
            ) { backStackEntry ->
                val category = backStackEntry.arguments?.getString("category")
                HomeScreen(
                    initialCategory = category,
                    onProductClick = { product ->
                        navController.navigate(Screen.ProductDetail.createRoute(product.id))
                    },
                    onCartClick = {
                        navController.navigate(Screen.Cart.route)
                    },
                    onProfileClick = {
                        navController.navigate(Screen.Profile.route)
                    },
                    onAddToCart = { product ->
                        cartViewModel.addToCart(
                            CartItem(
                                productId = product.id,
                                productName = product.name,
                                productPrice = product.price,
                                imageUrl = product.imageUrl,
                                sellerId = product.sellerId,
                                quantity = 1
                            )
                        )
                        Toast.makeText(context, "${product.name} added to cart", Toast.LENGTH_SHORT).show()
                    },
                    wishlistViewModel = wishlistViewModel
                )
            }

            composable(Screen.Categories.route) {
                CategoriesScreen(
                    onCategoryClick = { categoryName ->
                        navController.navigate(Screen.Home.createRoute(categoryName))
                    }
                )
            }

            composable(Screen.Wishlist.route) {
                WishlistScreen(
                    onProductClick = { product ->
                        navController.navigate(Screen.ProductDetail.createRoute(product.id))
                    },
                    onAddToCart = { product ->
                        cartViewModel.addToCart(
                            CartItem(
                                productId = product.id,
                                productName = product.name,
                                productPrice = product.price,
                                imageUrl = product.imageUrl,
                                sellerId = product.sellerId,
                                quantity = 1
                            )
                        )
                        Toast.makeText(context, "${product.name} added to cart", Toast.LENGTH_SHORT).show()
                    },
                    viewModel = wishlistViewModel
                )
            }
            
            composable(
                route = Screen.ProductDetail.route,
                arguments = listOf(navArgument("productId") { type = NavType.StringType })
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("productId") ?: ""
                ProductDetailScreen(
                    productId = productId,
                    onBack = { navController.popBackStack() },
                    onAddToCart = { product ->
                        cartViewModel.addToCart(
                            CartItem(
                                productId = product.id,
                                productName = product.name,
                                productPrice = product.price,
                                imageUrl = product.imageUrl,
                                sellerId = product.sellerId,
                                quantity = 1
                            )
                        )
                        navController.navigate(Screen.Cart.route)
                    },
                    onChatWithSeller = { sellerId, sellerName, pId, pName ->
                        navController.navigate(Screen.Chat.createRoute(sellerId, sellerName, pId, pName))
                    },
                    wishlistViewModel = wishlistViewModel
                )
            }
            
            composable(Screen.Cart.route) {
                CartScreen(
                    onBack = { navController.popBackStack() },
                    onCheckout = { navController.navigate(Screen.Checkout.route) },
                    viewModel = cartViewModel
                )
            }
            
            composable(Screen.Checkout.route) {
                val checkoutViewModel: CheckoutViewModel = viewModel()
                CheckoutScreen(
                    onBack = { navController.popBackStack() },
                    onOrderPlaced = { address, city, phone ->
                        scope.launch {
                            val success = checkoutViewModel.placeOrder(
                                cartItems = cartViewModel.cartItems.value,
                                totalAmount = cartViewModel.subtotal + 200,
                                address = address,
                                city = city,
                                phone = phone
                            )
                            if (success) {
                                cartViewModel.clearCart()
                                orderViewModel.fetchOrders()
                                Toast.makeText(context, "Order placed successfully!", Toast.LENGTH_LONG).show()
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Home.route) { inclusive = true }
                                }
                            } else {
                                Toast.makeText(context, "Failed to place order. Please try again.", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    cartViewModel = cartViewModel
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    onLogout = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onNavigateToOrders = { navController.navigate(Screen.MyOrders.route) },
                    onNavigateToSellerDashboard = { navController.navigate(Screen.SellerDashboard.route) },
                    onNavigateToAdminDashboard = { navController.navigate(Screen.AdminDashboard.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
                )
            }
            
            composable(Screen.MyOrders.route) {
                MyOrdersScreen(onBack = { navController.popBackStack() }, viewModel = orderViewModel)
            }

            composable(Screen.SellerDashboard.route) {
                SellerDashboardScreen(
                    onBack = { navController.popBackStack() },
                    onAddProduct = { navController.navigate(Screen.AddProduct.route) },
                    onEditProduct = { productId ->
                        navController.navigate(Screen.EditProduct.createRoute(productId))
                    }
                )
            }

            composable(Screen.AddProduct.route) {
                AddProductScreen(
                    onBack = { navController.popBackStack() },
                    onProductAdded = {
                        Toast.makeText(context, "Product added successfully!", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = Screen.EditProduct.route,
                arguments = listOf(navArgument("productId") { type = NavType.StringType })
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("productId") ?: ""
                EditProductScreen(
                    productId = productId,
                    onBack = { navController.popBackStack() },
                    onProductUpdated = {
                        Toast.makeText(context, "Product updated successfully!", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.AdminDashboard.route) {
                AdminDashboardScreen(
                    onBack = { navController.popBackStack() },
                    onManageUsers = { navController.navigate("admin_manage_users") },
                    onManageSellers = { navController.navigate(Screen.SellerApproval.route) },
                    onManageProducts = { navController.navigate(Screen.ProductModeration.route) }
                )
            }
            
            composable("admin_manage_users") {
                UserManagementScreen(onBack = { navController.popBackStack() })
            }

            composable(Screen.SellerApproval.route) {
                SellerApprovalScreen(onBack = { navController.popBackStack() })
            }

            composable(Screen.ProductModeration.route) {
                ProductModerationScreen(onBack = { navController.popBackStack() })
            }
            
            composable(Screen.Settings.route) {
                SettingsScreen(onBack = { navController.popBackStack() })
            }

            composable(
                route = Screen.Chat.route,
                arguments = listOf(
                    navArgument("receiverId") { type = NavType.StringType },
                    navArgument("receiverName") { type = NavType.StringType },
                    navArgument("productId") { 
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                    navArgument("productName") { 
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val receiverId = backStackEntry.arguments?.getString("receiverId") ?: ""
                val receiverName = backStackEntry.arguments?.getString("receiverName") ?: ""
                val productId = backStackEntry.arguments?.getString("productId")
                val productName = backStackEntry.arguments?.getString("productName")
                
                ChatScreen(
                    receiverId = receiverId,
                    receiverName = receiverName,
                    initialProductId = productId,
                    initialProductName = productName,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
