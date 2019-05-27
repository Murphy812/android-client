package org.shadowrunrussia2020.android

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.android.synthetic.main.activity_show_qr.*
import org.shadowrunrussia2020.android.qr.Data
import org.shadowrunrussia2020.android.qr.encode

fun startShowQrActivity(parent: Activity, data: Data) {
    var intent = Intent(parent, ShowQrActivity::class.java)
    intent.putExtra("QR_CONTENT", encode(data))
    parent.startActivity(intent)
}

class ShowQrActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_qr)

        val barcodeEncoder = BarcodeEncoder()
        val bitmap = barcodeEncoder.encodeBitmap(intent.getStringExtra("QR_CONTENT"), BarcodeFormat.QR_CODE, 400, 400)
        qrCodeImage.setImageBitmap(bitmap)
    }
}
