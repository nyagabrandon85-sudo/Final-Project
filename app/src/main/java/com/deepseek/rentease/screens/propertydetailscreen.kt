package com.deepseek.rentease.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.deepseek.rentease.models.Property
import com.deepseek.rentease.ui.theme.FavoriteColor
import com.deepseek.rentease.ui.theme.PriceColor
import com.deepseek.rentease.ui.theme.RentalAppTheme
import com.deepseek.rentease.viewmodels.PropertyDetailViewModel

@Composable
fun PropertyDetailScreen(
    navController: NavHostController,
    propertyId: String,
    viewModel: PropertyDetailViewModel = viewModel()
) {
    val property by viewModel.property.collectAsState()
    val isFavorite by viewModel.isFavorite.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(propertyId) {
        viewModel.loadProperty(propertyId)
    }

    PropertyDetailFullContent(
        property = property,
        isFavorite = isFavorite,
        isLoading = isLoading,
        onNavigateBack = { navController.navigateUp() },
        onToggleFavorite = { property?.let { viewModel.toggleFavorite(it) } },
        onCallClick = { phone ->
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
            context.startActivity(intent)
        },
        onBookClick = {
            // Handle booking logic
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertyDetailFullContent(
    property: Property?,
    isFavorite: Boolean,
    isLoading: Boolean,
    onNavigateBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onCallClick: (String) -> Unit,
    onBookClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Property Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    property?.let {
                        IconButton(onClick = onToggleFavorite) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (isFavorite) FavoriteColor else LocalContentColor.current
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (property == null) {
                EmptyState(message = "Property not found")
            } else {
                PropertyDetailScrollableContent(
                    property = property,
                    onCallClick = { onCallClick(property.ownerPhone) },
                    onBookClick = onBookClick
                )
            }
        }
    }
}

@Composable
fun PropertyDetailScrollableContent(
    property: Property,
    onCallClick: () -> Unit,
    onBookClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // Image Gallery
        if (property.imageUrls.isNotEmpty()) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(property.imageUrls) { imageUrl ->
                    Card(
                        modifier = Modifier.size(width = 300.dp, height = 200.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {
            // Title & Price
            Text(
                text = property.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Location",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = property.location,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Price
            Surface(
                color = PriceColor.copy(alpha = 0.1f),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = "KSh ${property.price.toInt()}/month",
                    style = MaterialTheme.typography.headlineSmall,
                    color = PriceColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Property Details Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PropertyInfoChip(icon = Icons.Default.Bed, label = "${property.bedrooms} Beds")
                PropertyInfoChip(icon = Icons.Default.Bathtub, label = "${property.bathrooms} Baths")
                PropertyInfoChip(
                    icon = Icons.Default.Home,
                    label = property.propertyType.replaceFirstChar { it.uppercase() }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Description
            Text(
                text = "Description",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = property.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Owner Info
            Text(
                text = "Contact Information",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = property.ownerName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = property.ownerPhone,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onCallClick,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Call")
                }
                Button(
                    onClick = onBookClick,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Book Viewing")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun PropertyInfoChip(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Surface(
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.small
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PropertyDetailPreview() {
    RentalAppTheme {
        PropertyDetailFullContent(
            property = Property(
                id = "1",
                title = "Modern Apartment",
                location = "Nairobi",
                price = 45000.0,
                description = "A beautiful modern apartment in the heart of the city.",
                ownerName = "John Doe",
                ownerPhone = "0712345678",
                imageUrls = emptyList()
            ),
            isFavorite = true,
            isLoading = false,
            onNavigateBack = {},
            onToggleFavorite = {},
            onCallClick = {},
            onBookClick = {}
        )
    }
}
