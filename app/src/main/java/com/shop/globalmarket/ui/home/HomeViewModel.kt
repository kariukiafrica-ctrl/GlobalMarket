package com.shop.globalmarket.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shop.globalmarket.data.ai.RecommendationService
import com.shop.globalmarket.data.model.Product
import com.shop.globalmarket.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ProductRepository()
    private val recommendationService = RecommendationService(application)
    
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory

    val categories = listOf("All", "Electronics", "Fashion", "Home & Kitchen", "Computing", "Accessories", "Sports", "Beauty", "Food & Drinks")

    val products: StateFlow<List<Product>> = combine(_products, _searchQuery, _selectedCategory) { products, query, category ->
        products.filter { product ->
            val matchesQuery = product.name.contains(query, ignoreCase = true) || 
                               product.category.contains(query, ignoreCase = true)
            val matchesCategory = category == "All" || product.category == category
            matchesQuery && matchesCategory
        }
    }.let { 
        val state = MutableStateFlow<List<Product>>(emptyList())
        viewModelScope.launch {
            it.collect { state.value = it }
        }
        state
    }

    private val _recommendedProducts = MutableStateFlow<List<Product>>(emptyList())
    val recommendedProducts: StateFlow<List<Product>> = _recommendedProducts

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val browsingHistory = mutableListOf<String>()

    init {
        fetchProducts()
    }

    private fun fetchProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            val allProducts = repository.getProducts()
            _products.value = allProducts
            _recommendedProducts.value = recommendationService.recommendProducts(browsingHistory)
            _isLoading.value = false
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onCategorySelected(category: String) {
        _selectedCategory.value = category
    }

    fun onProductViewed(category: String) {
        browsingHistory.add(category)
        _recommendedProducts.value = recommendationService.recommendProducts(browsingHistory)
    }
}
