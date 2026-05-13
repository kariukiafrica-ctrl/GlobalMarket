package com.shop.globalmarket.ui.seller

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shop.globalmarket.data.model.Product
import com.shop.globalmarket.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class EditProductViewModel : ViewModel() {
    private val repository = ProductRepository()
    private val _product = MutableStateFlow<Product?>(null)
    val product: StateFlow<Product?> = _product

    fun loadProduct(productId: String) {
        viewModelScope.launch {
            _product.value = repository.getProductById(productId)
        }
    }

    fun updateProduct(product: Product, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.updateProduct(product)
            onComplete()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductScreen(
    productId: String,
    onBack: () -> Unit,
    onProductUpdated: () -> Unit,
    viewModel: EditProductViewModel = viewModel()
) {
    val product by viewModel.product.collectAsState()
    
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }

    LaunchedEffect(productId) {
        viewModel.loadProduct(productId)
    }

    LaunchedEffect(product) {
        product?.let {
            name = it.name
            description = it.description
            price = it.price.toString()
            category = it.category
            stock = it.stock.toString()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Product") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (product == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("Price (Ksh)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = stock,
                        onValueChange = { stock = it },
                        label = { Text("Stock") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        val updatedProduct = product!!.copy(
                            name = name,
                            description = description,
                            price = price.toDoubleOrNull() ?: 0.0,
                            category = category,
                            stock = stock.toIntOrNull() ?: 0
                        )
                        viewModel.updateProduct(updatedProduct, onProductUpdated)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = name.isNotBlank() && price.isNotBlank()
                ) {
                    Text("Save Changes")
                }
            }
        }
    }
}
