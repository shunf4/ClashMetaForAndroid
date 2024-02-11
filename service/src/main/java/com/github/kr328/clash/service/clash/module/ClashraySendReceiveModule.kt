package com.github.kr328.clash.service.clash.module

import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.provider.DocumentsContract
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.github.kr328.clash.common.compat.getColorCompat
import com.github.kr328.clash.common.id.UndefinedIds
import com.github.kr328.clash.core.Clash
import com.github.kr328.clash.service.R
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.selects.select
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File


class ClashraySendReceiveModule(service: Service) : Module<Unit>(service) {
    private val brCopy = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val context = context ?: return
            val intent = intent ?: return
            val text = intent.getStringExtra("CLASHRAY_SEND_TEXT")?.let { it.ifEmpty { null } } ?: return
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("clashraySendReceivedText", text))
            Toast.makeText(context, "Copied Clashray Send Text", Toast.LENGTH_SHORT).show()
        }
    }

    private fun makeCopyIntent(text: String) = PendingIntent.getBroadcast(
        service,
        UndefinedIds.next(),
        Intent("com.github.kr328.clash.metacuberivx.CLASHRAY_SEND_TEXT").putExtra("CLASHRAY_SEND_TEXT", text),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    private fun makeNotiBuilder(data: JsonElement): NotificationCompat.Builder {
        val instantCopyText = data.jsonObject["InstantCopyText"]!!.jsonPrimitive.content
        val summary = data.jsonObject["Summary"]!!.jsonPrimitive.content
        val filePath = data.jsonObject["FilePath"]!!.jsonPrimitive.content
        // val fileUri = FileProvider.getUriForFile(service, service.applicationContext.packageName + ".clashraysend.fileprovider", File(filePath))
        val fileUri = Uri.fromFile(File(filePath))
        // val fileDirUri = FileProvider.getUriForFile(service, service.applicationContext.packageName + ".clashraysend.fileprovider", File(filePath).parentFile)
        val fileDirUri = Uri.fromFile(File(filePath).parentFile)

        var intentWrapper1: (Intent, String) -> Intent
        var intentWrapper2: (Intent, String) -> Intent
        intentWrapper1 = { i, t -> Intent.createChooser(i, t) }
        intentWrapper1 = { i, t -> i }
        intentWrapper2 = { i, t -> i }

        val openTextIntent = intentWrapper1(Intent().apply {
            setAction(Intent.ACTION_VIEW)
            setDataAndType(fileUri, "text/plain")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }, "Open with...")

        val openIntent = intentWrapper1(Intent().apply {
            setAction(Intent.ACTION_VIEW)
            val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(File(filePath).extension).let {
                if (it.isNullOrEmpty()) {
                    "*/*"
                } else {
                    it
                }
            }
            setDataAndType(fileUri, mime)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }, "Open with...")

        val viewDirIntent = intentWrapper2(Intent().apply {
            setAction(Intent.ACTION_VIEW)
            setDataAndType(fileDirUri, DocumentsContract.Document.MIME_TYPE_DIR)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }, "View directory with...")

        val openBrowserPageIntent = Intent().apply {
            setAction(Intent.ACTION_VIEW)
            setData(Uri.parse("http://send.clashray.home.arpa"))
        }


        if ((data.jsonObject["SendType"]!!.jsonPrimitive.content == "text") and instantCopyText.isNotEmpty()) {
            return NotificationCompat.Builder(service, "clashray_send_channel")
                .setSmallIcon(R.drawable.ic_logo_service)
                .setOnlyAlertOnce(true)
                .setAutoCancel(true)
                .setColor(service.getColorCompat(R.color.color_clash))
                .setContentTitle("Clashray Send Got Message")
                .setContentText(summary)
                .addAction(0, "Open File", PendingIntent.getActivity(
                    service,
                    UndefinedIds.next(),
                    openTextIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                ))
                .addAction(0, "View Dir", PendingIntent.getActivity(
                    service,
                    UndefinedIds.next(),
                    viewDirIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                ))
                .addAction(0, "Net Page", PendingIntent.getActivity(
                    service,
                    UndefinedIds.next(),
                    openBrowserPageIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                ))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(makeCopyIntent(instantCopyText))
        } else {
            return NotificationCompat.Builder(service, "clashray_send_channel")
                .setSmallIcon(R.drawable.ic_logo_service)
                .setColor(service.getColorCompat(R.color.color_clash))
                .setOnlyAlertOnce(true)
                .setAutoCancel(true)
                .setContentTitle("Clashray Send Got File")
                .setContentText(summary)
                .addAction(0, "Open File", PendingIntent.getActivity(
                    service,
                    0,
                    openIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                ))
                .addAction(0, "View Dir", PendingIntent.getActivity(
                    service,
                    0,
                    viewDirIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                ))
                .addAction(0, "Net Page", PendingIntent.getActivity(
                    service,
                    0,
                    openBrowserPageIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                ))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(PendingIntent.getActivity(
                    service,
                    0,
                    openIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                ))
        }
    }

    override suspend fun run() = coroutineScope {
        val builder = VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())

        NotificationManagerCompat.from(service)
            .createNotificationChannel(
                NotificationChannelCompat.Builder(
                    "clashray_send_channel",
                    NotificationManagerCompat.IMPORTANCE_HIGH
                ).setName("Clashray Send Notifications").build()
            )
        ContextCompat.registerReceiver(service, brCopy, IntentFilter("com.github.kr328.clash.metacuberivx.CLASHRAY_SEND_TEXT"), ContextCompat.RECEIVER_NOT_EXPORTED)
        try {
            val clashraySendReceive = Clash.subscribeClashraySend()

            while (true) {
                select<Unit> {
                    clashraySendReceive.onReceive {
                        NotificationManagerCompat.from(service).notify(UndefinedIds.next(), makeNotiBuilder(it).build())
                    }
                }
            }
        } finally {
            service.unregisterReceiver(brCopy)
        }
    }
}