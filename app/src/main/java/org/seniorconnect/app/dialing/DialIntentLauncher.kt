package org.seniorconnect.app.dialing

import android.app.Activity
import android.content.Intent
import android.net.Uri

class DialIntentLauncher {
    fun openDialer(activity: Activity, phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phoneNumber, null))
        activity.startActivity(intent)
    }
}
