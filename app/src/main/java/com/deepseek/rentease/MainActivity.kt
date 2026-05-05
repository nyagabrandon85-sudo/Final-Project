package com.deepseek.rentease

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

import androidx.navigation.compose.rememberNavController
import com.deepseek.rentease.navigation.NavGraph
import com.deepseek.rentease.navigation.Screen
import com.deepseek.rentease.ui.theme.RentalAppTheme
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Test Firebase connection
        val db = FirebaseFirestore.getInstance()
        db.collection("test").document("Connection")
            .set(mapOf("status" to "Connected", "timestamp" to System.currentTimeMillis()))
            .addOnSuccessListener{
                android.util.Log.d("FIREBASE", "Connected to Firestore !")
            }
            .addOnFailureListener {e ->
                android.util.Log.e("FIREBASE", "Failed: ${e.message}")
            }
        setContent {
            RentalAppTheme {
                val navController = rememberNavController()
                NavGraph(
                    navController=navController,
                    startDestination = Screen.Splash.route
                )
            }
        }
    }
}