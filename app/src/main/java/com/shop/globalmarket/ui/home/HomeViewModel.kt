package com.shop.globalmarket.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shop.globalmarket.data.ai.RecommendationService
import com.shop.globalmarket.data.model.Product
import com.shop.globalmarket.data.repository.ProductRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class SortOrder {
    DEFAULT, PRICE_LOW_HIGH, PRICE_HIGH_LOW, RATING
}

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ProductRepository()
    private val recommendationService = RecommendationService(application)
    
    private val _allProducts = MutableStateFlow<List<Product>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory

    private val _sortOrder = MutableStateFlow(SortOrder.DEFAULT)
    val sortOrder: StateFlow<SortOrder> = _sortOrder

    private val _priceRange = MutableStateFlow(0f..1000000f)
    val priceRange: StateFlow<ClosedFloatingPointRange<Float>> = _priceRange

    val categories = listOf("All", "Electronics", "Fashion", "Home & Kitchen", "Computing", "Accessories", "Sports", "Beauty", "Food & Drinks")

    val products: StateFlow<List<Product>> = combine(
        _allProducts, _searchQuery, _selectedCategory, _sortOrder, _priceRange
    ) { products, query, category, sort, priceRange ->
        products.filter { product ->
            val matchesQuery = product.name.contains(query, ignoreCase = true) || 
                               product.category.contains(query, ignoreCase = true)
            val matchesCategory = category == "All" || product.category == category
            val matchesPrice = product.price.toFloat() in priceRange
            matchesQuery && matchesCategory && matchesPrice
        }.let { filtered ->
            when (sort) {
                SortOrder.PRICE_LOW_HIGH -> filtered.sortedBy { it.price }
                SortOrder.PRICE_HIGH_LOW -> filtered.sortedByDescending { it.price }
                SortOrder.RATING -> filtered.sortedByDescending { it.rating }
                else -> filtered
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
            _allProducts.value = allProducts
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

    fun onSortOrderChange(order: SortOrder) {
        _sortOrder.value = order
    }

    fun onPriceRangeChange(range: ClosedFloatingPointRange<Float>) {
        _priceRange.value = range
    }

    fun onProductViewed(category: String) {
        browsingHistory.add(category)
        _recommendedProducts.value = recommendationService.recommendProducts(browsingHistory)
    }
}
