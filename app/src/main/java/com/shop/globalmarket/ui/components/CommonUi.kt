package com.shop.globalmarket.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shop.globalmarket.data.model.OrderStatus

@Composable
fun StatusBadge(status: OrderStatus) {
    val color = when (status) {
        OrderStatus.PENDING -> Color(0xFFFFA000)
        OrderStatus.PROCESSING -> Color(0xFF1976D2)
        OrderStatus.SHIPPED -> Color(0xFF7B1FA2)
        OrderStatus.DELIVERED -> Color(0xFF388E3C)
        OrderStatus.CANCELLED -> Color(0xFFD32F2F)
    }
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = CircleShape,
        border = BorderStroke(1.dp, color)
    ) {
        Text(
            text = status.name,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}
