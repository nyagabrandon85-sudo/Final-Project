package com.deepseek.rentease.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object PropertyDetail : Screen("property_detail/{propertyId}") {
        fun createRoute(propertyId: String) = "property_detail/$propertyId"
    }
    object Search : Screen("search")
    object Favorites : Screen("favorites")
    object UploadProperty : Screen("upload_property")
    object EditProperty : Screen("edit_property/{propertyId}") {
        fun createRoute(propertyId: String) = "edit_property/$propertyId"
    }
    object Bookings : Screen("bookings")
    object Profile : Screen("profile")
}