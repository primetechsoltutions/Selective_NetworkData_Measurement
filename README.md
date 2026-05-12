# Network Measurement SDK — Integration Guide

This document provides a comprehensive guide for integrating the **Selective Network Data Measurement SDK** into host applications. The SDK is designed with a clean, stateless architecture to ensure reliable and efficient network diagnostics.

---

## 1. Installation

Add the SDK module to your project's `settings.gradle` and your app's `build.gradle` file:

```gradle
// app/build.gradle
implementation project(':network-sdk')
```

---

## 2. Architecture Overview

The SDK follows a modular, provider-based architecture:
- **Facade (`NWNetworkDataMeasurement`)**: The single entry point for the host app.
- **Orchestrator (`NetworkDataCaptureExecutor`)**: Coordinates the sequential execution of validation, testing, and data capture.
- **Providers**: Specialized components for managing environment state (`NetworkStateProvider`, `SimInfoProvider`, `DateTimeProvider`).
- **Workers**: Atomic units of work for specific tasks (`PreFlightValidator`, `PerformanceTester`, `DataCapturer`).

---

## 3. Initialization

The SDK must be initialized before triggering any measurements. Since it handles runtime permissions, it must be bound to the lifecycle of an `Activity` or `Fragment`.

### ✅ Initialization in Fragment
```kotlin
class HomeFragment : Fragment() {
    
    private val nwMeasurement = NWNetworkDataMeasurement()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize in onCreate() to register permission launchers
        nwMeasurement.init(
            fragment = this, 
            applicationName = "MyHostApp"
        )
    }
}
```

---

## 4. Triggering Measurement

Trigger the measurement process by calling `startNWMeasurement`. This method handles sequential pre-flight validation (permissions, GPS, SIM status) before executing diagnostic tests.

```kotlin
nwMeasurement.startNWMeasurement(
    msisdn = "019XXXXXXXX",
    integratedAppVersion = "1.0.0",
    sdkInitiateTimeStamp = System.currentTimeMillis().toString(),
    integratedAppEventName = "Manual_Diagnostic",
    userLatitude = 23.8103,
    userLongitude = 90.4125
) { success: Boolean, status: FWAMeasurementStatus ->
    if (success) {
        Log.d("SDK", "Measurement Success: ${status.response}")
    } else {
        Log.e("SDK", "Measurement Failed: ${status.response}")
    }
}
```

### 📋 Sequential Pre-Flight Validation
The SDK enforces a strict, fail-fast validation sequence:
1. **Phone State Permission**: Required for SIM and network identification.
2. **Location Permission & GPS**: Required for cell and signal mapping.
3. **Connectivity Rules**: Blocks Wi-Fi connections to ensure measurements happen over mobile data.
4. **Banglalink SIM/Data**: Verifies that the active data connection is via the Banglalink network.

---

## 5. Response Formats

The `status.response` is a JSON string of the `NetworkDataResponse` object.

**✅ Success Example:**
```json
{
  "status": "Success",
  "statusCode": 200,
  "message": "Measurement completed successfully.",
  "testResult": "Green",
  "data": [
    {
      "rsrp": "-85",
      "snr": "15",
      "rtt": 45.5,
      "latency": 12.3,
      "dlspeed": 25000.0,
      "ulspeed": 10000.0,
      "deviceModel": "Samsung S21"
    }
  ]
}
```

---

## 6. Troubleshooting

### "Required permissions are missing"
Ensure `init()` was called in `onCreate()`. The SDK will automatically request missing permissions during `startNWMeasurement`.

### "Wi-Fi not allowed"
The SDK is designed for cellular network diagnostics. Please disable Wi-Fi before starting a measurement.

### "Banglalink SIM required"
The SDK validates that a Banglalink SIM is present and being used for data. If the device has multiple SIMs, ensure Banglalink is set as the primary data SIM.
