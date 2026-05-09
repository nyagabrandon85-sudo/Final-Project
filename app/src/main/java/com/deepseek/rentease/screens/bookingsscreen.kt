package com.deepseek.rentease.screens
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
    val unreadCount by viewModel.unreadCount.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadBookings()
    }

    BookingsContent(
        myBookings = myBookings,
        receivedBookings = receivedBookings,
        userRole = userRole,
        isLoading = isLoading,
        unreadCount = unreadCount,
        onUpdateStatus = { bookingId, status -> viewModel.updateStatus(bookingId, status) },
        onNavigateBack = { navController.navigateUp() },
        onMarkAsViewed = { viewModel.markAllReceivedAsViewed() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingsContent(
    myBookings: List<Booking>,
    receivedBookings: List<Booking>,
    userRole: String?,
    isLoading: Boolean,
    unreadCount: Int,
    onUpdateStatus: (String, String) -> Unit,
    onNavigateBack: () -> Unit,
    onMarkAsViewed: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val context = LocalContext.current

    // Mark as viewed when clicking the Received tab
    LaunchedEffect(selectedTab) {
        if (selectedTab == 1 && userRole == "landlord") {
            onMarkAsViewed()
        }
    }

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
                // If role is null or not landlord, show only My Requests
                // but we can add a debug indicator here
                listOf("My Requests")
            }

            if (userRole == "landlord") {
                TabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { 
                                BadgedBox(badge = {
                                    if (index == 1 && unreadCount > 0) {
                                        Badge { Text(unreadCount.toString()) }
                                    }
                                }) {
                                    Text(title)
                                }
                            }
                        )
                    }
                }
            } else if (userRole == null && !isLoading) {
                // Warning for missing role
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth().padding(8.dp)
                ) {
                    Text(
                        "Warning: User role not found. Please log out and log in again.",
                        modifier = Modifier.padding(8.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
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
                    // Logic fix: 
                    // Tab 0 is ALWAYS My Requests (what I sent)
                    // Tab 1 is ALWAYS Received (what others sent to me)
                    val bookings = if (selectedTab == 1 && userRole == "landlord") {
                        receivedBookings
                    } else {
                        myBookings
                    }

                    if (bookings.isEmpty()) {
                        EmptyState(message = if (selectedTab == 0) "You haven't sent any requests yet" else "No requests received yet")
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(bookings) { booking ->
                                BookingCard(
                                    booking = booking,
                                    // isLandlord means "Am I the one who RECEIVED this?"
                                    isLandlord = selectedTab == 1,
                                    onApprove = { onUpdateStatus(booking.id, "approved") },
                                    onReject = { onUpdateStatus(booking.id, "rejected") },
                                    onCall = { phone -> makeCall(context, phone) },
                                    onMessage = { phone -> sendSMS(context, phone) },
                                    onWhatsApp = { phone -> openWhatsApp(context, phone) }
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
    onReject: () -> Unit,
    onCall: (String) -> Unit,
    onMessage: (String) -> Unit,
    onWhatsApp: (String) -> Unit
) {
    val statusColor = when (booking.status) {
        "approved" -> Color(0xFF4CAF50)
        "rejected" -> Color(0xFFE91E63)
        else -> Color(0xFFFF9800)
    }

    val contactPhone = if (isLandlord) booking.tenantPhone else booking.landlordPhone
    val contactName = if (isLandlord) booking.tenantName else "${booking.landlordName} Landlord"

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
                text = if (isLandlord) "From: ${booking.tenantName}" else "Contact: $contactName",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Message: ${booking.message}",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Communication Icons
            if (contactPhone.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onCall(contactPhone) }) {
                        Icon(Icons.Default.Phone, contentDescription = "Call", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { onMessage(contactPhone) }) {
                        Icon(Icons.Outlined.Chat, contentDescription = "Message", tint = MaterialTheme.colorScheme.secondary)
                    }
                    IconButton(onClick = { onWhatsApp(contactPhone) }) {
                        Icon(
                            imageVector = Icons.Default.Send, // Fallback for WhatsApp
                            contentDescription = "WhatsApp",
                            tint = Color(0xFF25D366)
                        )
                    }
                }
            }

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

// Helper functions for communication
private fun makeCall(context: Context, phone: String) {
    try {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Could not open dialer", Toast.LENGTH_SHORT).show()
    }
}

private fun sendSMS(context: Context, phone: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:$phone"))
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Could not open SMS app", Toast.LENGTH_SHORT).show()
    }
}

private fun openWhatsApp(context: Context, phone: String) {
    try {
        // Clean the phone number: remove all non-numeric characters
        var cleanPhone = phone.replace(Regex("[^0-9]"), "")
        
        // Handle Kenyan numbers (07xx... or 01xx...)
        if (cleanPhone.startsWith("0")) {
            cleanPhone = "254" + cleanPhone.substring(1)
        } else if (!cleanPhone.startsWith("254")) {
            // If it doesn't start with 0 or 254, assume it needs the 254 prefix
            cleanPhone = "254$cleanPhone"
        }

        val url = "https://api.whatsapp.com/send?phone=$cleanPhone"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "WhatsApp not installed or could not open", Toast.LENGTH_SHORT).show()
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
            unreadCount = 0,
            onUpdateStatus = { _, _ -> },
            onNavigateBack = {},
            onMarkAsViewed = {}
        )
    }
}
