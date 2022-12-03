package com.example.mydatausage.viewmodels

import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.RemoteException
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.example.mydatausage.models.AppData
import com.example.mydatausage.models.Duration
import java.text.ParseException
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

class MainViewModel: ViewModel() {

    @RequiresApi(Build.VERSION_CODES.O)
    fun getAllInstalledAppsData(packageManager: PackageManager, networkStatsManager: NetworkStatsManager, subscriberId: String?): List<AppData> = packageManager.getInstalledApplications(
        PackageManager.GET_META_DATA).map { app ->

        val (start, end) = today()

        val (sent, received, total) = returnFormattedData(app.uid, start, end, networkStatsManager, subscriberId)

        AppData(
            packageManager.getApplicationLabel(app).toString(),
            app.packageName,
            app.uid,
            (app.flags and ApplicationInfo.FLAG_SYSTEM) == 1,
            packageManager.getApplicationIcon(app.packageName),
            sent,
            received,
            total
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun today(): Duration = Duration(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(), ZonedDateTime.now().toInstant().toEpochMilli())

    @RequiresApi(Build.VERSION_CODES.M)
    fun returnFormattedData(
        uid: Int,
        startTime: Long,
        endTime: Long,
        networkStatsManager: NetworkStatsManager,
        subscriberId: String?
    ): Array<String> {
        val (sent, received, total) = getAppWifiDataUsage(
            uid,
            startTime,
            endTime,
            networkStatsManager,
            subscriberId
        )
        return formatData(sent, received)
    }

    /**
     * Formats the data
     */
    private fun formatData(sent: Long, received: Long): Array<String> {
        val totalBytes = (sent + received) / 1024f
        val sentBytes = sent / 1024f
        val receivedBytes = received / 1024f

        val totalMB = totalBytes / 1024f

        val totalGB: Float
        val sentGB: Float
        val receivedGB: Float

        val sentMB: Float = sentBytes / 1024f
        val receivedMB: Float = receivedBytes / 1024f

        val sentData: String
        val receivedData: String
        val totalData: String
        if (totalMB > 1024) {
            totalGB = totalMB / 1024f
            totalData = String.format("%.2f", totalGB) + " GB"
        } else {
            totalData = String.format("%.2f", totalMB) + " MB"
        }

        if (sentMB > 1024) {
            sentGB = sentMB / 1024f
            sentData = String.format("%.2f", sentGB) + " GB"
        } else {
            sentData = String.format("%.2f", sentMB) + " MB"
        }
        if (receivedMB > 1024) {
            receivedGB = receivedMB / 1024f
            receivedData = String.format("%.2f", receivedGB) + " GB"
        } else {
            receivedData = String.format("%.2f", receivedMB) + " MB"
        }

        return arrayOf(sentData, receivedData, totalData)
    }

    @Throws(RemoteException::class, ParseException::class)
    @RequiresApi(Build.VERSION_CODES.M)
    private fun getAppWifiDataUsage(uid: Int, startTime: Long, endTime: Long, networkStatsManager: NetworkStatsManager, subscriberId: String?): Array<Long> {

        var sent = 0L
        var received = 0L

        val networkStats: NetworkStats = networkStatsManager.querySummary(
            ConnectivityManager.TYPE_WIFI,
            subscriberId,
            startTime,
            endTime
        )

        do {
            val bucket = NetworkStats.Bucket()
            networkStats.getNextBucket(bucket)
            if (bucket.uid == uid) {
                sent += bucket.txBytes
                received += bucket.rxBytes
            }
        } while (networkStats.hasNextBucket())

        val total: Long = sent + received
        networkStats.close()
        Log.d("SOHAIL BRO", "$uid $sent, $received, ${total}")
        return arrayOf(sent, received, total)
    }

}