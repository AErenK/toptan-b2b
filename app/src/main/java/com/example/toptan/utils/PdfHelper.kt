package com.example.toptan.utils // Kendi paket adına göre düzenleyebilirsin

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import com.example.toptan.model.Siparis
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun createPdf(context: Context, uri: Uri, siparisler: List<Siparis>) {
    try {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Standart A4 Kağıt Boyutu
        var page = pdfDocument.startPage(pageInfo)
        var canvas: Canvas = page.canvas

        // Yazı Tipi ve Boyut Ayarları
        val baslikPaint = Paint().apply {
            textSize = 20f
            isFakeBoldText = true
            color = Color.rgb(30, 41, 59) // Koyu Lacivert (Uygulama temana uygun)
        }
        val altBaslikPaint = Paint().apply {
            textSize = 14f
            isFakeBoldText = true
            color = Color.rgb(37, 99, 235) // Mavi
        }
        val normalPaint = Paint().apply {
            textSize = 12f
            color = Color.rgb(100, 116, 139) // Gri
        }
        val cizgiPaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 1f
        }

        var yPos = 50f
        val solMargin = 40f

        // Rapor Başlığı ve Tarih
        val format = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.forLanguageTag("tr-TR"))
        val tarih = format.format(Date())

        canvas.drawText("TOPTANCI SİPARİŞ RAPORU", solMargin, yPos, baslikPaint)
        yPos += 25f
        canvas.drawText("Oluşturulma Tarihi: $tarih", solMargin, yPos, normalPaint)
        yPos += 25f
        canvas.drawLine(solMargin, yPos, 555f, yPos, cizgiPaint)
        yPos += 30f

        // Siparişleri Listeleme Döngüsü
        for (siparis in siparisler) {
            // Eğer sayfa sonuna gelirsek, yeni sayfa açıyoruz
            if (yPos > 780f) {
                pdfDocument.finishPage(page)
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                yPos = 50f
            }

            val siparisTarihi = format.format(Date(siparis.tarih))
            val formatliTutar = NumberFormat.getNumberInstance(Locale.forLanguageTag("tr-TR")).format(siparis.toplamTutar)

            // Sipariş Müşterisi ve Tarihi
            canvas.drawText("Müşteri: ${siparis.musteriEmail}", solMargin, yPos, altBaslikPaint)
            yPos += 20f
            canvas.drawText("Sipariş Tarihi: $siparisTarihi  |  Durum: ${siparis.durum}", solMargin, yPos, normalPaint)
            yPos += 20f
            // Sipariş İçeriği ve Tutarı
            canvas.drawText("İçerik: ${siparis.siparisOzeti}", solMargin, yPos, normalPaint)
            yPos += 20f
            canvas.drawText("Toplam Tutar: $formatliTutar TL", solMargin, yPos, altBaslikPaint)
            yPos += 25f

            // Siparişler arasına ayırıcı çizgi
            canvas.drawLine(solMargin, yPos, 555f, yPos, cizgiPaint)
            yPos += 30f
        }

        pdfDocument.finishPage(page)

        // PDF dosyasını kullanıcının seçtiği yere kaydet
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            pdfDocument.writeTo(outputStream)
        }
        pdfDocument.close()

        Toast.makeText(context, "PDF Başarıyla İndirilenlere Kaydedildi!", Toast.LENGTH_LONG).show()

    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "PDF oluşturulurken hata: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
    }
}