package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.UserRole
import com.example.ui.theme.*

@Composable
fun VegNonVegBadge(isVeg: Boolean, modifier: Modifier = Modifier) {
    val borderColor = if (isVeg) SatnaVegGreen else SatnaNonVegRed
    Box(
        modifier = modifier
            .size(16.dp)
            .border(1.5.dp, borderColor, RoundedCornerShape(3.dp))
            .padding(2.5.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(borderColor)
        )
    }
}

@Composable
fun RatingChip(rating: Double, totalRatings: Int? = null, modifier: Modifier = Modifier) {
    Surface(
        color = SatnaVegGreen,
        shape = RoundedCornerShape(6.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$rating",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(2.dp))
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(12.dp)
            )
            if (totalRatings != null) {
                Text(
                    text = " ($totalRatings+)",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun TopBarLocationHeader(
    currentAddress: String,
    currentRole: UserRole,
    language: String,
    onAddressClick: () -> Unit,
    onRoleClick: () -> Unit,
    onLanguageToggle: () -> Unit,
    onSearchClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Location info
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onAddressClick() }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Satna",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = currentAddress,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }

                // Language & Portal Switcher Badges
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Language Switcher
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onLanguageToggle() }
                    ) {
                        Text(
                            text = if (language == "EN") "English" else "हिंदी",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Role Badge
                    Surface(
                        color = SatnaTealSecondary,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onRoleClick() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val roleText = when (currentRole) {
                                UserRole.CUSTOMER -> "Customer"
                                UserRole.RESTAURANT_OWNER -> "Restaurant"
                                UserRole.RIDER -> "Delivery Rider"
                                UserRole.ADMIN -> "Super Admin"
                            }
                            Text(
                                text = roleText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "Switch Portal",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SatnaMapCanvas(
    restaurantName: String,
    customerAddress: String,
    riderName: String,
    statusText: String,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val pulseAnim by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(8.dp)
        ) {
            val routeColor = MaterialTheme.colorScheme.primary
            val mapBg = MaterialTheme.colorScheme.surface

            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height

                // Draw map grid background
                drawRect(color = mapBg)

                // Road network mock
                val path = Path().apply {
                    moveTo(width * 0.15f, height * 0.3f)
                    cubicTo(
                        width * 0.35f, height * 0.2f,
                        width * 0.5f, height * 0.8f,
                        width * 0.85f, height * 0.7f
                    )
                }

                // Road background line
                drawPath(
                    path = path,
                    color = Color.Gray.copy(alpha = 0.3f),
                    style = Stroke(width = 14.dp.toPx())
                )

                // Active delivery route line
                drawPath(
                    path = path,
                    color = routeColor,
                    style = Stroke(
                        width = 6.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 10f), 0f)
                    )
                )

                // Restaurant location (start point)
                val restX = width * 0.15f
                val restY = height * 0.3f
                drawCircle(color = SatnaTealSecondary, radius = 14.dp.toPx(), center = Offset(restX, restY))
                drawCircle(color = Color.White, radius = 6.dp.toPx(), center = Offset(restX, restY))

                // Customer location (end point)
                val custX = width * 0.85f
                val custY = height * 0.7f
                drawCircle(color = SatnaVegGreen, radius = 14.dp.toPx(), center = Offset(custX, custY))
                drawCircle(color = Color.White, radius = 6.dp.toPx(), center = Offset(custX, custY))

                // Rider moving along route (mid point)
                val riderX = width * 0.52f
                val riderY = height * 0.55f
                drawCircle(color = routeColor.copy(alpha = pulseAnim * 0.4f), radius = 24.dp.toPx(), center = Offset(riderX, riderY))
                drawCircle(color = routeColor, radius = 12.dp.toPx(), center = Offset(riderX, riderY))
                drawCircle(color = Color.White, radius = 4.dp.toPx(), center = Offset(riderX, riderY))
            }

            // Map Overlays / Labels
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("📍 $restaurantName", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text("🏠 $customerAddress", color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp)
            }

            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.TwoWheeler,
                        contentDescription = "Rider",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$riderName • $statusText",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}
