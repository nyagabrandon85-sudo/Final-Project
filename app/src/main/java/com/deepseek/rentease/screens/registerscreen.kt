package com.deepseek.rentease.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.deepseek.rentease.navigation.Screen
import com.deepseek.rentease.ui.theme.PrimaryColor
import androidx.compose.ui.tooling.preview.Preview
import com.deepseek.rentease.ui.theme.RentalAppTheme
import com.deepseek.rentease.viewmodels.AuthViewModel

@Composable
fun RegisterScreen(
    navController: NavHostController,
    viewModel: AuthViewModel = viewModel()
) {
    val registerState by viewModel.registerState.collectAsState()

    LaunchedEffect(registerState) {
        if (registerState is AuthViewModel.AuthState.Success) {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Register.route) { inclusive = true }
            }
        }
    }

    RegisterContent(
        registerState = registerState,
        onRegister = { email, password, name, phone, role ->
            viewModel.register(email, password, name, phone, role)
        },
        onNavigateBack = { navController.navigateUp() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterContent(
    registerState: AuthViewModel.AuthState,
    onRegister: (String, String, String, String, String) -> Unit,
    onNavigateBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("tenant") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Create Account",
            style = MaterialTheme.typography.headlineLarge,
            color = Color.Red
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Full Name", fontWeight = FontWeight.Bold, color = Color.White) },
            textStyle = TextStyle(fontWeight = FontWeight.Bold, color = Color.White),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Red,
                unfocusedBorderColor = Color.White,
                focusedLabelColor = Color.Red,
                unfocusedLabelColor = Color.White,
                cursorColor = Color.Red
            ),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email", fontWeight = FontWeight.Bold, color = Color.White) },
            textStyle = TextStyle(fontWeight = FontWeight.Bold, color = Color.White),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Red,
                unfocusedBorderColor = Color.White,
                focusedLabelColor = Color.Red,
                unfocusedLabelColor = Color.White,
                cursorColor = Color.Red
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = { 
                // Only allow numbers and prevent users from deleting the + prefix if you want to force it
                if (it.all { char -> char.isDigit() || char == '+' }) {
                    phone = it
                }
            },
            label = { Text("Phone Number (e.g. 0712...)", fontWeight = FontWeight.Bold, color = Color.White) },
            placeholder = { Text("0712345678", color = Color.Gray) },
            textStyle = TextStyle(fontWeight = FontWeight.Bold, color = Color.White),
            prefix = { Text("+254 ", color = Color.Red) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Red,
                unfocusedBorderColor = Color.White,
                focusedLabelColor = Color.Red,
                unfocusedLabelColor = Color.White,
                cursorColor = Color.Red
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password", fontWeight = FontWeight.Bold, color = Color.White) },
            textStyle = TextStyle(fontWeight = FontWeight.Bold, color = Color.White),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Red,
                unfocusedBorderColor = Color.White,
                focusedLabelColor = Color.Red,
                unfocusedLabelColor = Color.White,
                cursorColor = Color.Red
            ),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("I am a:", style = MaterialTheme.typography.titleMedium, color = Color.White)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FilterChip(
                selected = selectedRole == "tenant",
                onClick = { selectedRole = "tenant" },
                label = { Text("Tenant", color = if (selectedRole == "tenant") Color.Black else Color.White) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color.Red,
                    containerColor = Color.Transparent,
                    labelColor = Color.White,
                    selectedLabelColor = Color.Black
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedRole == "tenant",
                    borderColor = Color.White,
                    selectedBorderColor = Color.Red
                )
            )
            FilterChip(
                selected = selectedRole == "landlord",
                onClick = { selectedRole = "landlord" },
                label = { Text("Landlord", color = if (selectedRole == "landlord") Color.Black else Color.White) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color.Red,
                    containerColor = Color.Transparent,
                    labelColor = Color.White,
                    selectedLabelColor = Color.Black
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedRole == "landlord",
                    borderColor = Color.White,
                    selectedBorderColor = Color.Red
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (registerState is AuthViewModel.AuthState.Error) {
            Text(
                text = registerState.message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = { onRegister(email, password, name, phone, selectedRole) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = registerState !is AuthViewModel.AuthState.Loading,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
        ) {
            if (registerState is AuthViewModel.AuthState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Register")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onNavigateBack) {
            Text("Already have an account? Sign In", color = Color.White)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    RentalAppTheme {
        RegisterContent(
            registerState = AuthViewModel.AuthState.Idle,
            onRegister = { _, _, _, _, _ -> },
            onNavigateBack = {}
        )
    }
}
