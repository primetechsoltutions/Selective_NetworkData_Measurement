package com.ptsl.selective_networksdk_hostapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import com.ptsl.selective_network_sdk.NWNetworkDataMeasurement
import com.ptsl.selective_networksdk_hostapp.ui.theme.*
import org.json.JSONObject

/**
 * Support Fragment: Demonstrates the usage of the new getNetworkIdentifiers feature.
 * Provides a quick way to retrieve CID and LACID after preflight validation.
 */
class SupportFragment : Fragment() {

    private val sdk = NWNetworkDataMeasurement()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Initialize the SDK for this fragment
        sdk.init(this, "MYBL")

        return ComposeView(requireContext()).apply {
            setContent {
                FWASDKTheme {
                    SupportIdentifierScreen(
                        onBack = { parentFragmentManager.popBackStack() },
                        onGetIdentifiers = { onResult ->
                            sdk.getNetworkIdentifiers { success, status ->
                                onResult(success, status.response)
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportIdentifierScreen(
    onBack: () -> Unit,
    onGetIdentifiers: ((Boolean, String?) -> Unit) -> Unit
) {
    var isLoading by remember { mutableStateOf(false) }
    var cid by remember { mutableStateOf<String?>(null) }
    var lacid by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var rawJson by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Network Support") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BgDarkCard,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BgDark)
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Retrieve Device Identifiers",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Performs Preflight Validation and retrieves current CID and LACID for Banglalink SIM.",
                color = TextDim,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            // Result Display
            Row(modifier = Modifier.fillMaxWidth()) {
                IdentifierCard(label = "CID", value = cid ?: "---", modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(16.dp))
                IdentifierCard(label = "LACID", value = lacid ?: "---", modifier = Modifier.weight(1f))
            }

            if (message != null) {
                Spacer(modifier = Modifier.height(24.dp))
                Surface(
                    color = (if (cid != null) SuccessGreen else ErrorRed).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = message!!,
                        color = if (cid != null) SuccessGreen else ErrorRed,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(12.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = {
                    isLoading = true
                    cid = null
                    lacid = null
                    message = null
                    onGetIdentifiers { success, response ->
                        isLoading = false
                        rawJson = response
                        response?.let { jsonStr ->
                            try {
                                val json = JSONObject(jsonStr)
                                val data = json.optJSONObject("data")
                                if (data != null) {
                                    cid = data.optString("CID", "N/A")
                                    lacid = data.optString("LACID", "N/A")
                                }
                                message = json.optString("message", if (success) "Success" else "Failed")
                            } catch (e: Exception) {
                                message = "Response Parsing Error"
                            }
                        } ?: run {
                            message = "No response from SDK"
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(18.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("FETCH IDENTIFIERS", fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                }
            }

            if (rawJson != null) {
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "RAW SDK RESPONSE",
                    color = TextDim,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )
                Surface(
                    modifier = Modifier.fillMaxWidth().weight(1f).padding(top = 12.dp),
                    color = Color.Black.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = rawJson!!,
                            color = TextDim,
                            fontSize = 11.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun IdentifierCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = BgDarkCard),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, color = TextDim, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                color = if (value == "---" || value == "N/A") TextDim else Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

