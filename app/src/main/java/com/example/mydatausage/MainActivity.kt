package com.example.mydatausage

import android.app.AppOpsManager
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.telephony.TelephonyManager
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mydatausage.models.AppData
import com.example.mydatausage.ui.theme.MyDataUsageTheme
import com.example.mydatausage.viewmodels.MainViewModel
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.google.android.material.snackbar.Snackbar

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MyDataUsageTheme {

                val networkStatsManager: NetworkStatsManager =
                    applicationContext.getSystemService(NETWORK_STATS_SERVICE) as NetworkStatsManager

                val list = viewModel.getAllInstalledAppsData(packageManager, networkStatsManager, getSubscriberID())
                val display = remember { mutableStateOf(false) }

                if(display.value) {

                    if(!checkForPermission(this)) {
                        startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                        return@MyDataUsageTheme
                    }

                    DisplayWifiDataUsage(list = list.filter { !it.isSystemApp })
                } else {

                    Button(onClick = { display.value = !display.value }) {
                        Text(text = "Fetch Data usage of Apps via WIFI")
                    }
                }
            }
        }
    }

    /**
     * Checks if data usage permission is granted
     */
    private fun checkForPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode =
            appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
        return mode == AppOpsManager.MODE_ALLOWED
    }

    /**
     * Link describing the warning
     *
     * 1. https://stackoverflow.com/questions/47691310/why-is-using-getstring-to-get-device-identifiers-not-recommended
     */
    private fun getSubscriberID(): String? = if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) try {
        (getSystemService(TELEPHONY_SERVICE) as TelephonyManager).subscriberId
    } catch (e: Exception) {
        e.printStackTrace()
        null
    } else null

}

@Composable
fun DisplayWifiDataUsage(list: List<AppData>) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
        List(list)
    }
}

@Composable
fun List(
    list: List<AppData>
) {
    LazyColumn {
        items(items = list) { item ->
            AlbumItem(item)
        }
    }
}

@Composable
fun AlbumItem(appData: AppData){
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
    ){
        Column {
            Image(painter = rememberDrawablePainter(drawable = appData.icon), contentDescription = appData.applicationName,
                modifier = Modifier.size(64.dp),
                contentScale = ContentScale.Crop
            )
        }
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
            Text(
                text = appData.applicationName,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = appData.packageName,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = appData.sent,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = appData.received,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = appData.total,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = appData.uid.toString(),
                fontWeight = FontWeight.Normal,
                fontSize = 10.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = appData.isSystemApp.toString(),
                fontWeight = FontWeight.Normal,
                fontSize = 10.sp,
                color = Color.Gray
            )
        }
    }
}