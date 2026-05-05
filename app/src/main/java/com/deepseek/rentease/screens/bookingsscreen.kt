package com.deepseek.rentease.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.deepseek.rentease.models.Booking
import com.deepseek.rentease.ui.theme.RentalAppTheme
import com.deepseek.rentease.viewmodels.BookingsViewModel

@Composable
fun BookingsScreen(
    navController: NavHostController,
    viewModel: BookingsViewModel = viewModel()
) {
    val myBookings by viewModel.myBookings.collectAsState()
    val receivedBookings by viewModel.receivedBookings.collectAsState()
    val userRole by viewModel.userRole.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadBookings()
        viewModel.loadUserRole()
    }

    BookingsContent(
        myBookings = myBookings,
        receivedBookings = receivedBookings,
        userRole = userRole,
        isLoading = isLoading,
        onUpdateStatus = { bookingId, status -> viewModel.updateStatus(bookingId, status) },
        onNavigateBack = { navController.navigateUp() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingsContent(
    myBookings: List<Booking>,
    receivedBookings: List<Booking>,
    userRole: String?,
    isLoading: Boolean,
    onUpdateStatus: (String, String) -> Unit,
    onNavigateBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bookings") },
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
        ) {
            val tabs = if (userRole == "landlord") {
                listOf("My Requests", "Received")
            } else {
                listOf("My Requests")
            }

            if (tabs.size > 1) {
                TabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) }
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else {
                    val bookings = if (userRole == "landlord" && selectedTab == 1) {
                        receivedBookings
                    } else {
                        myBookings
                    }

                    if (bookings.isEmpty()) {
                        EmptyState(message = "No bookings found")
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(bookings) { booking ->
                                BookingCard(
                                    booking = booking,
                                    isLandlord = userRole == "landlord" && selectedTab == 1,
                                    onApprove = { onUpdateStatus(booking.id, "approved") },
                                    onReject = { onUpdateStatus(booking.id, "rejected") }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BookingCard(
    booking: Booking,
    isLandlord: Boolean,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    val statusColor = when (booking.status) {
        "approved" -> Color(0xFF4CAF50)
        "rejected" -> Color(0xFFE91E63)
        else -> Color(0xFFFF9800)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = booking.propertyTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = booking.status.replaceFirstChar { it.uppercase() },
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isLandlord) "From: ${booking.tenantName}" else "To: ${booking.landlordId}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = booking.message,
                style = MaterialTheme.typography.bodyMedium
            )

            if (isLandlord && booking.status == "pending") {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onReject,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE91E63))
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reject")
                    }
                    Button(
                        onClick = onApprove,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Approve")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BookingsScreenPreview() {
    RentalAppTheme {
        BookingsContent(
            myBookings = emptyList(),
            receivedBookings = emptyList(),
            userRole = "tenant",
            isLoading = false,
            onUpdateStatus = { _, _ -> },
            onNavigateBack = {}
        )
    }
}
