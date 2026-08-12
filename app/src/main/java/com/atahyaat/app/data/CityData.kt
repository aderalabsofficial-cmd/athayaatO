package com.atahyaat.app.data

data class City(
    val name: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val timeZoneId: String
)

object CityData {
    val PRESETS = listOf(
        City("Makkah", "Saudi Arabia", 21.3891, 39.8579, "Asia/Riyadh"),
        City("Madinah", "Saudi Arabia", 24.5247, 39.5692, "Asia/Riyadh"),
        City("Riyadh", "Saudi Arabia", 24.7136, 46.6753, "Asia/Riyadh"),
        City("Multan", "Pakistan", 30.1575, 71.5249, "Asia/Karachi"),
        City("Lahore", "Pakistan", 31.5497, 74.3436, "Asia/Karachi"),
        City("Karachi", "Pakistan", 24.8607, 67.0011, "Asia/Karachi"),
        City("Islamabad", "Pakistan", 33.6844, 73.0479, "Asia/Karachi"),
        City("Dhaka", "Bangladesh", 23.8103, 90.4125, "Asia/Dhaka"),
        City("Istanbul", "Turkey", 41.0082, 28.9784, "Europe/Istanbul"),
        City("Cairo", "Egypt", 30.0444, 31.2357, "Africa/Cairo"),
        City("Dubai", "UAE", 25.2048, 55.2708, "Asia/Dubai"),
        City("Jakarta", "Indonesia", -6.2088, 106.8456, "Asia/Jakarta"),
        City("Kuala Lumpur", "Malaysia", 3.1390, 101.6869, "Asia/Kuala_Lumpur"),
        City("London", "United Kingdom", 51.5072, -0.1276, "Europe/London"),
        City("New York", "United States", 40.7128, -74.0060, "America/New_York"),
        City("Toronto", "Canada", 43.6532, -79.3832, "America/Toronto"),
        City("Delhi", "India", 28.6139, 77.2090, "Asia/Kolkata"),
        City("Amman", "Jordan", 31.9454, 35.9284, "Asia/Amman"),
        City("Baghdad", "Iraq", 33.3152, 44.3661, "Asia/Baghdad"),
        City("Doha", "Qatar", 25.2854, 51.5310, "Asia/Qatar")
    )
}
