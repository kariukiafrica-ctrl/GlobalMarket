package com.shop.globalmarket.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String = "", val icon: ImageVector? = null) {
    object Splash : Screen("splash")
    object Home : Screen("home?category={category}", "Home", Icons.Default.Home) {
        fun createRoute(category: String? = null) = if (category != null) "home?category=$category" else "home"
    }
    object Categories : Screen("categories", "Categories", Icons.Default.Category)
    object Wishlist : Screen("wishlist", "Wishlist", Icons.Default.Favorite)
    object Cart : Screen("cart", "Cart", Icons.Default.ShoppingCart)
    object Profile : Screen("profile", "Account", Icons.Default.Person)
    
    object ProductDetail : Screen("product_detail/{productId}") {
        fun createRoute(productId: String) = "product_detail/$productId"
    }
    
    object MyOrders : Screen("my_orders")
    object Login : Screen("login")
    object Signup : Screen("signup")
    object SellerDashboard : Screen("seller_dashboard")
    object AddProduct : Screen("add_product")
    object EditProduct : Screen("edit_product/{productId}") {
        fun createRoute(productId: String) = "edit_product/$productId"
    }
    object AdminDashboard : Screen("admin_dashboard")
    object SellerApproval : Screen("seller_approval")
    object ProductModeration : Screen("product_moderation")
    object Checkout : Screen("checkout")
    object Settings : Screen("settings")
    object Chat : Screen("chat/{receiverId}/{receiverName}?productId={productId}&productName={productName}") {
        fun createRoute(receiverId: String, receiverName: String, productId: String? = null, productName: String? = null) : String {
            var r = "chat/$receiverId/$receiverName"
            val params = mutableListOf<String>()
            if (productId != null) params.add("productId=$productId")
            if (productName != null) params.add("productName=$productName")
            if (params.isNotEmpty()) {
                r += "?" + params.joinToString("&")
            }
            return r
        }
    }

    companion object {
        // Use a getter to avoid initialization order issues (NPE)
        val bottomNavItems get() = listOf(Home, Categories, Wishlist, Cart, Profile)
    }
}
