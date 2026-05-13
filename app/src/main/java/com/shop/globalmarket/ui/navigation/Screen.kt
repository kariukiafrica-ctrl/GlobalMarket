package com.shop.globalmarket.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object ProductDetail : Screen("product_detail/{productId}") {
        fun createRoute(productId: String) = "product_detail/$productId"
    }
    object Cart : Screen("cart")
    object Profile : Screen("profile")
    object MyOrders : Screen("my_orders")
    object Login : Screen("login")
    object Signup : Screen("signup")
    object SellerDashboard : Screen("seller_dashboard")
    object AddProduct : Screen("add_product")
    object AdminDashboard : Screen("admin_dashboard")
    object Checkout : Screen("checkout")
    object Settings : Screen("settings")
    object Chat : Screen("chat/{receiverId}/{receiverName}") {
        fun createRoute(receiverId: String, receiverName: String) = "chat/$receiverId/$receiverName"
    }
}
