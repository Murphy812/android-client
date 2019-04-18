package org.shadowrunrussia2020.android.qr

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import com.google.zxing.integration.android.IntentIntegrator

fun startScanQrActivity(parent: Activity) {
    val integrator = IntentIntegrator(parent)
    integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
    integrator.setPrompt(
        "Сканирование QR-кода. Для включения/выключения подсветки вспышкой используйте кнопки регулировки громкости.")
    integrator.setBeepEnabled(false)
    integrator.initiateScan()
}

fun maybeProcessActivityResult(parent: Activity, requestCode: Int, resultCode: Int, data: Intent?): Data? {
    val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
    if (result == null)
        return null;

    if (result.contents == null)
        return null;

    try {
        return decode(result.contents)
    } catch (e: ValidationException) {
        Toast.makeText(parent, "Неподдерживаемый QR-код", Toast.LENGTH_LONG).show()
    } catch (e: FormatException) {
        Toast.makeText(parent, "Неподдерживаемый QR-код", Toast.LENGTH_LONG).show()
    } catch (e: ExpiredException) {
        Toast.makeText(parent, "Срок действия QR-кода истек", Toast.LENGTH_LONG).show()
    }
    return null
}
