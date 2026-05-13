package com.shop.globalmarket.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shop.globalmarket.data.model.User
import com.shop.globalmarket.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SellerApprovalViewModel : ViewModel() {
    private val userRepository = UserRepository()
    private val _sellers = MutableStateFlow<List<User>>(emptyList())
    val sellers: StateFlow<List<User>> = _sellers
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        fetchSellers()
    }

    private fun fetchSellers() {
        viewModelScope.launch {
            _isLoading.value = true
            _sellers.value = userRepository.getUnapprovedSellers()
            _isLoading.value = false
        }
    }

    fun approveSeller(uid: String) {
        viewModelScope.launch {
            userRepository.updateSellerApprovalStatus(uid, true)
            fetchSellers()
        }
    }

    fun rejectSeller(uid: String) {
        viewModelScope.launch {
            // Logic for rejection (maybe delete or mark as rejected)
            fetchSellers()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerApprovalScreen(
    onBack: () -> Unit,
    viewModel: SellerApprovalViewModel = viewModel()
) {
    val sellers by viewModel.sellers.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Seller Approvals", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
                .padding(padding)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (sellers.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(Modifier.height(16.dp))
                    Text("No pending seller approvals", color = MaterialTheme.colorScheme.outline)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(sellers) { seller ->
                        SellerApprovalCard(
                            seller = seller,
                            onApprove = { viewModel.approveSeller(seller.uid) },
                            onReject = { viewModel.rejectSeller(seller.uid) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SellerApprovalCard(
    seller: User,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(50.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, contentDescription = null)
                }
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (seller.name.isNotEmpty()) seller.name else "New Seller Request",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = seller.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            
            Row {
                IconButton(
                    onClick = onReject,
                    modifier = Modifier.background(MaterialTheme.colorScheme.errorContainer, CircleShape)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Reject", tint = MaterialTheme.colorScheme.onErrorContainer)
                }
                Spacer(Modifier.width(8.dp))
                IconButton(
                    onClick = onApprove,
                    modifier = Modifier.background(Color(0xFF4CAF50).copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Approve", tint = Color(0xFF2E7D32))
                }
            }
        }
    }
}
