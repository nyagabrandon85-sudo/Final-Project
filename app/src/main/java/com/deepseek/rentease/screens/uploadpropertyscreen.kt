package com.deepseek.rentease.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.deepseek.rentease.ui.theme.RentalAppTheme
import com.deepseek.rentease.viewmodels.UploadPropertyViewModel

@Composable
fun UploadPropertyScreen(
    navController: NavHostController,
    viewModel: UploadPropertyViewModel = viewModel()
) {
    val uploadState by viewModel.uploadState.collectAsState()

    LaunchedEffect(uploadState) {
        if (uploadState is UploadPropertyViewModel.UploadState.Success) {
            navController.navigateUp()
        }
    }

    UploadPropertyContent(
        uploadState = uploadState,
        onNavigateBack = { navController.navigateUp() },
        onUpload = { title, description, price, location, bedrooms, bathrooms, type, uris ->
            viewModel.uploadProperty(
                title = title,
                description = description,
                price = price,
                location = location,
                bedrooms = bedrooms,
                bathrooms = bathrooms,
                propertyType = type,
                imageUris = uris
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadPropertyContent(
    uploadState: UploadPropertyViewModel.UploadState,
    onNavigateBack: () -> Unit,
    onUpload: (String, String, Double, String, Int, Int, String, List<Uri>) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var bedrooms by remember { mutableStateOf("") }
    var bathrooms by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("apartment") }
    var imageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris != null) {
            imageUris = uris.take(5) // Max 5 images
        }
    }

    val propertyTypes = listOf("apartment", "house", "studio", "villa")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Upload Property") },
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
            // Image Picker
            Button(
                onClick = { imagePicker.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Select Images (${imageUris.size}/5)")
            }

            if (imageUris.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    imageUris.forEach { uri ->
                        Card(
                            modifier = Modifier.size(80.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            AsyncImage(
                                model = uri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Form Fields
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

            if (uploadState is UploadPropertyViewModel.UploadState.Error) {
                Text(
                    text = uploadState.message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    onUpload(
                        title,
                        description,
                        price.toDoubleOrNull() ?: 0.0,
                        location,
                        bedrooms.toIntOrNull() ?: 0,
                        bathrooms.toIntOrNull() ?: 0,
                        selectedType,
                        imageUris
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = uploadState !is UploadPropertyViewModel.UploadState.Loading
            ) {
                if (uploadState is UploadPropertyViewModel.UploadState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Upload Property")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UploadPropertyScreenPreview() {
    RentalAppTheme {
        UploadPropertyContent(
            uploadState = UploadPropertyViewModel.UploadState.Idle,
            onNavigateBack = {},
            onUpload = { _, _, _, _, _, _, _, _ -> }
        )
    }
}
