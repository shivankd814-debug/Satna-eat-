package com.example.ui.screens.docs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SatnaOrangePrimary
import com.example.ui.theme.SatnaTealSecondary

@Composable
fun DeveloperDocsScreen(
    onBackClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Developer & Deployment Guide", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Satna Eats Production Setup Instructions", fontSize = 12.sp, color = Color.Gray)
                }
            }
        }

        item {
            Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("1. Firebase & Phone OTP Setup 🔥", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SatnaOrangePrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "• Step 1: Register package 'com.aistudio.satnaeats.mfdp' in Firebase Console.\n" +
                                "• Step 2: Download 'google-services.json' and place in /app root.\n" +
                                "• Step 3: Enable Phone Authentication (+91 India OTP) and Google Sign-In in Firebase Auth tab.\n" +
                                "• Step 4: Configure Cloud Firestore 10 core collections:\n" +
                                "  1. users  2. restaurants  3. deliveryPartners  4. orders  5. payments\n" +
                                "  6. reviews  7. coupons  8. notifications  9. settings  10. analytics\n" +
                                "• Step 5: Deploy `firestore.rules` for role-based security.",
                        fontSize = 12.sp
                    )
                }
            }
        }

        item {
            Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("2. Google Maps Platform Setup 🗺️", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SatnaTealSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "• Step 1: Create a Google Cloud Platform project and enable Maps SDK for Android, Places API, Geocoding API, and Distance Matrix API.\n" +
                                "• Step 2: Generate API key and restrict to Android app SHA-1 fingerprint.\n" +
                                "• Step 3: Add `MAPS_API_KEY=your_key` in AI Studio Secrets or `.env` file.",
                        fontSize = 12.sp
                    )
                }
            }
        }

        item {
            Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("3. Razorpay Payment Gateway Integration 💳", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SatnaOrangePrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "• Step 1: Sign up on Razorpay Merchant Portal (India).\n" +
                                "• Step 2: Obtain Key ID and Key Secret under API Keys section.\n" +
                                "• Step 3: Integrate Razorpay Standard Checkout SDK for instant PhonePe, Google Pay, UPI Intent, Credit/Debit cards & NetBanking.",
                        fontSize = 12.sp
                    )
                }
            }
        }

        item {
            Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("4. APK Build & Play Store Release 🚀", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SatnaTealSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "• Step 1: Generate release keystore using keytool.\n" +
                                "• Step 2: Execute `./gradlew assembleRelease` or `./gradlew bundleRelease` for Android App Bundle (AAB).\n" +
                                "• Step 3: Upload AAB to Google Play Console under Satna Eats production track.",
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
