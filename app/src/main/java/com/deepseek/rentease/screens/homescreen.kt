package com.deepseek.rentease.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.deepseek.rentease.navigation.Screen
import com.deepseek.rentease.models.Property
import com.deepseek.rentease.ui.theme.PriceColor
import com.deepseek.rentease.ui.theme.RentalAppTheme
import com.deepseek.rentease.viewmodels.HomeViewModel

@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: HomeViewModel = viewModel(),
) {
    val properties by viewModel.properties.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val userRole by viewModel.userRole.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadProperties()
        viewModel.loadUserRole()
    }

    HomeContent(
        properties = properties,
        isLoading = isLoading,
        userRole = userRole,
        onSearchClick = { navController.navigate(Screen.Search.route) },
        onProfileClick = { navController.navigate(Screen.Profile.route) },
        onFavoritesClick = { navController.navigate(Screen.Favorites.route) },
        onUploadClick = { navController.navigate(Screen.UploadProperty.route) },
        onBookingsClick = { navController.navigate(Screen.Bookings.route) },
        onPropertyClick = { property ->
            navController.navigate(Screen.PropertyDetail.createRoute(property.id))
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    properties: List<Property>,
    isLoading: Boolean,
    userRole: String?,
    onSearchClick: () -> Unit,
    onProfileClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onUploadClick: () -> Unit,
    onBookingsClick: () -> Unit,
    onPropertyClick: (Property) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("RentalHub", color = Color.Red) },
                actions = {
                    IconButton(onClick = onSearchClick) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = onProfileClick) {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = true,
                    onClick = { }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Favorite, contentDescription = "Favorites") },
                    label = { Text("Favorites") },
                    selected = false,
                    onClick = onFavoritesClick
                )
                if (userRole == "landlord") {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Add, contentDescription = "Upload") },
                        label = { Text("Upload") },
                        selected = false,
                        onClick = onUploadClick
                    )
                }
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Bookmark, contentDescription = "Bookings") },
                    label = { Text("Bookings") },
                    selected = false,
                    onClick = onBookingsClick
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (properties.isEmpty()) {
                EmptyState(message = "No properties available yet")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(properties) { property ->
                        PropertyCard(
                            property = property,
                            onClick = { onPropertyClick(property) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PropertyCard(property: Property, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            AsyncImage(
                model = property.imageUrls.firstOrNull() ?: "",
                contentDescription = property.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = property.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = property.location,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "KSh ${property.price.toInt()}/month",
                        style = MaterialTheme.typography.titleMedium,
                        color = PriceColor,
                        fontWeight = FontWeight.Bold
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Bed,
                            contentDescription = "Bedrooms",
                            modifier = Modifier.size(16.dp)
                        )
                        Text(property.bedrooms.toString(), style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Bathtub,
                            contentDescription = "Bathrooms",
                            modifier = Modifier.size(16.dp)
                        )
                        Text(property.bathrooms.toString(), style = MaterialTheme.typography.bodySmall)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                AssistChip(
                    onClick = { },
                    label = { Text(property.propertyType.replaceFirstChar { it.uppercase() }) }
                )
            }
        }
    }
}

@Composable
fun EmptyState(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    RentalAppTheme {
        HomeContent(
            properties = listOf(
                Property(
                    id = "1",
                    title = "Modern Apartment",
                    location = "Nairobi",
                    price = 45000.0,
                    imageUrls = listOf("")
                )
            ),
            isLoading = false,
            userRole = "tenant",
            onSearchClick = {},
            onProfileClick = {},
            onFavoritesClick = {},
            onUploadClick = {},
            onBookingsClick = {},
            onPropertyClick = {}
        )
    }
}
