package com.deepseek.rentease.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.deepseek.rentease.models.Property
import com.deepseek.rentease.navigation.Screen
import com.deepseek.rentease.ui.theme.RentalAppTheme
import com.deepseek.rentease.viewmodels.FavoritesViewModel

@Composable
fun FavoritesScreen(
    navController: NavHostController,
    viewModel: FavoritesViewModel = viewModel()
) {
    val favorites by viewModel.favorites.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadFavorites()
    }

    FavoritesContent(
        favorites = favorites,
        isLoading = isLoading,
        onNavigateBack = { navController.navigateUp() },
        onPropertyClick = { property ->
            navController.navigate(Screen.PropertyDetail.createRoute(property.id))
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesContent(
    favorites: List<Property>,
    isLoading: Boolean,
    onNavigateBack: () -> Unit,
    onPropertyClick: (Property) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Favorites") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (favorites.isEmpty()) {
                EmptyState(message = "No favorites yet")
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(favorites) { property ->
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

@Preview(showBackground = true)
@Composable
fun FavoritesScreenPreview() {
    RentalAppTheme {
        FavoritesContent(
            favorites = emptyList(),
            isLoading = false,
            onNavigateBack = {},
            onPropertyClick = {}
        )
    }
}
