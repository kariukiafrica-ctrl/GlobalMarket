package com.shop.globalmarket.ui.categories

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shop.globalmarket.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    onCategoryClick: (String) -> Unit
) {
    val categories = listOf(
        CategoryItem("Electronics", Icons.Default.ElectricBolt),
        CategoryItem("Fashion", Icons.Default.Checkroom),
        CategoryItem("Home & Kitchen", Icons.Default.Kitchen),
        CategoryItem("Computing", Icons.Default.Computer),
        CategoryItem("Accessories", Icons.Default.Watch),
        CategoryItem("Sports", Icons.Default.SportsBasketball),
        CategoryItem("Beauty", Icons.Default.Face),
        CategoryItem("Food & Drinks", Icons.Default.Fastfood),
        CategoryItem("Business", Icons.Default.BusinessCenter),
        CategoryItem("Home & Office", Icons.Default.Chair)
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Categories", fontWeight = FontWeight.Bold) })
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier.padding(padding),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(categories) { category ->
                CategoryCard(category, onCategoryClick)
            }
        }
    }
}

data class CategoryItem(val name: String, val icon: ImageVector)

@Composable
fun CategoryCard(category: CategoryItem, onClick: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable { onClick(category.name) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = category.name,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = category.name,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
