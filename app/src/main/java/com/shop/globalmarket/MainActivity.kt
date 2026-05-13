package com.shop.globalmarket

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import com.shop.globalmarket.data.model.CartItem
import com.shop.globalmarket.data.model.Order
import com.shop.globalmarket.ui.admin.AdminDashboardScreen
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
import com.shop.globalmarket.ui.seller.SellerDashboardScreen
import com.shop.globalmarket.ui.settings.SettingsScreen
import com.shop.globalmarket.ui.splash.SplashScreen
import com.shop.globalmarket.ui.theme.GlobalMarketTheme
import com.shop.globalmarket.data.repository.OrderRepository
import kotlinx.coroutines.MainScope
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
    val cartViewModel: CartViewModel = viewModel()
    val orderViewModel: OrderViewModel = viewModel()
    val context = LocalContext.current
    
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
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

        composable(Screen.Home.route) {
            HomeScreen(
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
                            quantity = 1
                        )
                    )
                    Toast.makeText(context, "${product.name} added to cart", Toast.LENGTH_SHORT).show()
                }
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
                            quantity = 1
                        )
                    )
                    navController.navigate(Screen.Cart.route)
                },
                onChatWithSeller = { sellerId, sellerName ->
                    navController.navigate(Screen.Chat.createRoute(sellerId, sellerName))
                }
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
            val orderRepository = OrderRepository()
            CheckoutScreen(
                onBack = { navController.popBackStack() },
                onOrderPlaced = {
                    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                    val newOrder = Order(
                        buyerId = userId,
                        products = cartViewModel.cartItems.value,
                        totalAmount = cartViewModel.subtotal + 200, // Subtotal + Delivery
                    )
                    MainScope().launch {
                        orderRepository.placeOrder(newOrder)
                        cartViewModel.clearCart()
                        orderViewModel.fetchOrders() // Refresh orders
                        Toast.makeText(context, "Order placed successfully!", Toast.LENGTH_LONG).show()
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
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
                onAddProduct = { navController.navigate(Screen.AddProduct.route) }
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

        composable(Screen.AdminDashboard.route) {
            AdminDashboardScreen(
                onBack = { navController.popBackStack() },
                onManageUsers = { 
                    Toast.makeText(context, "User Management feature coming soon", Toast.LENGTH_SHORT).show()
                },
                onManageSellers = { 
                    Toast.makeText(context, "Seller Approvals feature coming soon", Toast.LENGTH_SHORT).show()
                },
                onManageProducts = { 
                    Toast.makeText(context, "Moderation Panel coming soon", Toast.LENGTH_SHORT).show()
                }
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = Screen.Chat.route,
            arguments = listOf(
                navArgument("receiverId") { type = NavType.StringType },
                navArgument("receiverName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val receiverId = backStackEntry.arguments?.getString("receiverId") ?: ""
            val receiverName = backStackEntry.arguments?.getString("receiverName") ?: ""
            ChatScreen(
                receiverId = receiverId,
                receiverName = receiverName,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
