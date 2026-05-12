package com.ptsl.selective_networksdk_hostapp.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ptsl.selective_networksdk_hostapp.model.RecentTest
import com.ptsl.selective_networksdk_hostapp.ui.theme.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SpeedTestScreen(
    onBack: () -> Unit,
    onStartAssessment: ((Boolean, String?) -> Unit) -> Unit
) {
    var isLoading by remember { mutableStateOf(false) }
    var showResult by remember { mutableStateOf(false) }
    var resultJson by remember { mutableStateOf("") }
    var mainSpeed by remember { mutableStateOf("0.0") }
    var testRes by remember { mutableStateOf("") }
    val recentTests = remember { mutableStateListOf<RecentTest>() }

    Box(modifier = Modifier.fillMaxSize().background(BgDark)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Network Speed Test",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Hero Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = BgDarkCard)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    Column {
                     Row {
                         Column {
                             Text(text = "Ready for scan", color = TextDim, fontSize = 14.sp)
                             Text(text = "Your Download Speed : ", color = TextDim, fontSize = 14.sp)
                         }
                         Spacer(modifier = Modifier.width(16.dp))
                         Card(
                             colors = CardDefaults.cardColors(containerColor = BgDarkCard),
                             shape = RoundedCornerShape(16.dp)
                         ){
                             Text(text = "Test Result: ${testRes}", color = TextDim, fontSize = 14.sp)
                         }
                     }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = mainSpeed,
                                color = PrimaryBlue,
                                fontSize = 64.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Mbps",
                                color = TextDim,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }
                    }

                    Button(
                        onClick = {
                            isLoading = true
                            showResult = false
                            onStartAssessment { success, response ->
                                isLoading = false
                                response?.let { jsonStr ->
                                    resultJson = jsonStr
                                    showResult = true
                                    try {
                                        val json = JSONObject(jsonStr)
                                        val data = json.optJSONObject("data")
                                        val speedPair = data?.optJSONObject("speedPair")
                                        val networkData = data?.optJSONObject("networkData")

                                        val dl = (speedPair?.optDouble("dlSpeedKbps") ?: 0.0) / 1000.0
                                        val ul = (speedPair?.optDouble("ulSpeedKbps") ?: 0.0) / 1000.0
                                        val rsrp = networkData?.optInt("RSRP") ?: 0
                                        val status = json.optString("status", "Failed")
                                       val testResult = json.optString("testResult", "Unknown")
                                        mainSpeed = String.format(Locale.getDefault(), "%.1f", dl)
                                        testRes=testResult

                                        val newTest = RecentTest(
                                            timestamp = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date()),
                                            downloadSpeed = dl,
                                            uploadSpeed = ul,
                                            rsrp = rsrp,
                                            status = status,
                                            testResult = testResult
                                        )
                                        recentTests.add(0, newTest)
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            }
                        },
                        modifier = Modifier.align(Alignment.BottomEnd),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Run Diagnostic")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Recent Tests Label
            Text(
                text = "Recent Tests",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Recent Tests List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(recentTests) { test ->
                    RecentTestItem(test)
                }
            }
        }

        // Loading Overlay
        AnimatedVisibility(
            visible = isLoading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LoadingOverlay),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryBlue)
            }
        }

        // Result Card Overlay
        AnimatedVisibility(
            visible = showResult,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BgDark.copy(alpha = 0.9f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Top
                ) {
                    // Close button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Measurement Results",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Button(
                            onClick = { showResult = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.7f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.size(40.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("✕", color = Color.White, fontSize = 16.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Results as cards
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        item {
                            ResultBasicInfoCard(resultJson)
                        }
                        
                        val dataList = extractMeasurementData(resultJson)
                        items(dataList.size) { index ->
                            MeasurementDataCard(dataList[index], index + 1)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecentTestItem(test: RecentTest) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = test.timestamp, color = TextDim, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    Text(
                        text = "${String.format(Locale.getDefault(), "%.1f", test.downloadSpeed)} Mbps",
                        color = PrimaryBlue,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "${String.format(Locale.getDefault(), "%.1f", test.uploadSpeed)} Mbps",
                        color = AccentPurple,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(text = "RSRP: ${test.rsrp} dBm", color = TextDim, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            if (test.status.equals("Success", true)) SuccessGreen else ErrorRed,
                            CircleShape
                        )
                )
            }
        }
    }
}

// Helper function to extract measurement data from JSON
fun extractMeasurementData(jsonString: String): List<Map<String, String>> {
    val dataList = mutableListOf<Map<String, String>>()
    try {
        val json = JSONObject(jsonString)
        val dataArray = json.optJSONArray("data")
        dataArray?.let {
            for (i in 0 until it.length()) {
                val dataObject = it.optJSONObject(i)
                dataObject?.let { obj ->
                    val dataMap = mutableMapOf<String, String>()
                    val keys = obj.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        val value = obj.opt(key)
                        dataMap[key] = value?.toString() ?: "N/A"
                    }
                    dataList.add(dataMap)
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return dataList
}

// Helper function to extract basic info from JSON
fun extractBasicInfo(jsonString: String): Map<String, String> {
    val result = mutableMapOf<String, String>()
    try {
        val json = JSONObject(jsonString)
        result["status"] = json.optString("status", "N/A")
        result["statusCode"] = json.optInt("statusCode", -1).toString()
        result["message"] = json.optString("message", "N/A")
        result["testResult"] = json.optString("testResult", "N/A")
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return result
}

// Composable for basic info card
@Composable
fun ResultBasicInfoCard(jsonString: String) {
    val basicInfo = extractBasicInfo(jsonString)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BgDarkCard)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Basic Information",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            InfoRow("Status", basicInfo["status"] ?: "N/A", PrimaryBlue)
            Spacer(modifier = Modifier.height(8.dp))
            InfoRow("Status Code", basicInfo["statusCode"] ?: "N/A", PrimaryBlue)
            Spacer(modifier = Modifier.height(8.dp))
            InfoRow("Message", basicInfo["message"] ?: "N/A", TextDim)
            Spacer(modifier = Modifier.height(8.dp))
            val testResult = basicInfo["testResult"] ?: "N/A"
            InfoRow("Test Result", testResult, if (testResult == "Success") SuccessGreen else ErrorRed)
        }
    }
}

// Composable for each measurement data card
@Composable
fun MeasurementDataCard(dataMap: Map<String, String>, index: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BgDarkCard)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Network Data #$index",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Box(
                    modifier = Modifier
                        .background(PrimaryBlue, RoundedCornerShape(8.dp))
                        .padding(4.dp, 2.dp)
                ) {
                    Text(
                        text = dataMap["type"] ?: "Unknown",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Display key data fields in a grid-like format
            val priorityFields = listOf(
                "type", "date", "time", "data",
                "dlspeed", "ulspeed", "latency", "rtt",
                "rssi", "rsrp", "rsrq", "snr",
                "mcc", "mnc", "cid", "band",
                "deviceModel", "deviceOsVersion",
                "totalDownloadVolume", "totalUploadVolume"
            )

            val displayData = dataMap.filter { it.key in priorityFields }
                .toList()
                .sortedBy { priorityFields.indexOf(it.first).takeIf { idx -> idx >= 0 } ?: Int.MAX_VALUE }

            displayData.forEach { (key, value) ->
                DataFieldRow(key, value)
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Show additional fields if any
            val additionalFields = dataMap.filter { it.key !in priorityFields }
            if (additionalFields.isNotEmpty()) {
                Divider(
                    color = Color.White.copy(alpha = 0.1f),
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Text(
                    text = "Additional Info",
                    color = TextDim,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                additionalFields.forEach { (key, value) ->
                    DataFieldRow(key, value)
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

// Helper composable for displaying key-value pairs
@Composable
fun InfoRow(label: String, value: String, valueColor: Color = TextDim) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = TextDim, fontSize = 12.sp)
        Text(
            text = value,
            color = valueColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f, fill = false),
            textAlign = TextAlign.End
        )
    }
}

// Helper composable for displaying data fields
@Composable
fun DataFieldRow(key: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = formatFieldName(key),
            color = TextDim,
            fontSize = 11.sp,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .weight(1f)
                .wrapContentWidth(Alignment.End),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// Helper function to format field names (convert camelCase to Title Case)
fun formatFieldName(fieldName: String): String {
    return fieldName
        .replace(Regex("([A-Z])"), " $1")
        .replace(Regex("^\\s"), "")
        .split(" ")
        .joinToString(" ") { word ->
            word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        }
}
