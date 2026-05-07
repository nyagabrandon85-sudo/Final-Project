package com.deepseek.rentease.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.deepseek.rentease.models.Property
import com.deepseek.rentease.viewmodels.EditPropertyViewModel

@Composable
fun EditPropertyScreen(
    navController: NavHostController,
    propertyId: String,
    viewModel: EditPropertyViewModel = viewModel()
) {
    val property by viewModel.property.collectAsState()
    val updateState by viewModel.updateState.collectAsState()

    LaunchedEffect(propertyId) {
        viewModel.loadProperty(propertyId)
    }

    LaunchedEffect(updateState) {
        if (updateState is EditPropertyViewModel.UpdateState.Success) {
            navController.navigateUp()
        }
    }

    property?.let {
        EditPropertyContent(
            property = it,
            updateState = updateState,
            onNavigateBack = { navController.navigateUp() },
            onUpdate = { title, description, price, location, bedrooms, bathrooms, type ->
                viewModel.updateProperty(title, description, price, location, bedrooms, bathrooms, type)
            }
        )
    } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPropertyContent(
    property: Property,
    updateState: EditPropertyViewModel.UpdateState,
    onNavigateBack: () -> Unit,
    onUpdate: (String, String, Double, String, Int, Int, String) -> Unit
) {
    var title by remember { mutableStateOf(property.title) }
    var description by remember { mutableStateOf(property.description) }
    var price by remember { mutableStateOf(property.price.toString()) }
    var location by remember { mutableStateOf(property.location) }
    var bedrooms by remember { mutableStateOf(property.bedrooms.toString()) }
    var bathrooms by remember { mutableStateOf(property.bathrooms.toString()) }
    var selectedType by remember { mutableStateOf(property.propertyType) }

    val propertyTypes = listOf("apartment", "house", "studio", "villa")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Property") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Property Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Price per Month (KSh)") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = bedrooms,
                    onValueChange = { bedrooms = it },
                    label = { Text("Bedrooms") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = bathrooms,
                    onValueChange = { bathrooms = it },
                    label = { Text("Bathrooms") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("Property Type", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                propertyTypes.forEach { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { selectedType = type },
                        label = { Text(type.replaceFirstChar { it.uppercase() }) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (updateState is EditPropertyViewModel.UpdateState.Error) {
                Text(
                    text = updateState.message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    onUpdate(
                        title,
                        description,
                        price.toDoubleOrNull() ?: 0.0,
                        location,
                        bedrooms.toIntOrNull() ?: 0,
                        bathrooms.toIntOrNull() ?: 0,
                        selectedType
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = updateState !is EditPropertyViewModel.UpdateState.Loading
            ) {
                if (updateState is EditPropertyViewModel.UpdateState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Update Property")
                }
            }
        }
    }
}
