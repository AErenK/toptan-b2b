package com.example.toptan.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import com.example.toptan.model.Siparis
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfHelper {

    fun siparisPdfOlustur(context: Context, siparis: Siparis) {
        // 1. A4 Boyutlarında bir PDF dökümanı başlatıyoruz
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        // 2. Fırçalarımızı (Yazı tiplerini ve renkleri) hazırlıyoruz
        val cizgiPaint = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f }
        val baslikPaint = Paint().apply { textSize = 22f; isFakeBoldText = true; color = Color.rgb(37, 99, 235) } // Mavi
        val altBaslikPaint = Paint().apply { textSize = 14f; isFakeBoldText = true; color = Color.BLACK }
        val metinPaint = Paint().apply { textSize = 12f; color = Color.DKGRAY }
        val tutarPaint = Paint().apply { textSize = 18f; isFakeBoldText = true; color = Color.rgb(22, 163, 74) } // Yeşil

        var yPos = 60f
        val solMargin = 40f

        // 3. Başlık ve Tarih Bilgileri
        canvas.drawText("B2B SİPARİŞ İRSALİYESİ", solMargin, yPos, baslikPaint)
        yPos += 40f

        val tarih = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("tr", "TR")).format(Date(siparis.tarih))
        canvas.drawText("Sipariş No: ${siparis.siparisId.take(8).uppercase()}", solMargin, yPos, metinPaint)
        canvas.drawText("Tarih: $tarih", 400f, yPos, metinPaint)
        yPos += 30f

        canvas.drawLine(solMargin, yPos, 555f, yPos, cizgiPaint)
        yPos += 30f

        // 4. Müşteri (Cari) Bilgileri
        canvas.drawText("ALICI (MÜŞTERİ) BİLGİLERİ", solMargin, yPos, altBaslikPaint)
        yPos += 25f
        canvas.drawText("Firma Ünvanı: ${siparis.sirketUnvani}", solMargin, yPos, metinPaint)
        yPos += 20f
        canvas.drawText("Yetkili / Tel: ${siparis.yetkiliKisi} - ${siparis.telefon}", solMargin, yPos, metinPaint)
        yPos += 20f
        canvas.drawText("Vergi Dairesi/No: ${siparis.vergiDairesi} / ${siparis.vergiNo}", solMargin, yPos, metinPaint)
        yPos += 20f
        canvas.drawText("Teslimat Adresi: ${siparis.teslimatAdresi}", solMargin, yPos, metinPaint)
        yPos += 40f

        canvas.drawLine(solMargin, yPos, 555f, yPos, cizgiPaint)
        yPos += 30f

        // 5. Sipariş Edilen Ürünler (Sepet Özeti)
        canvas.drawText("SİPARİŞ İÇERİĞİ", solMargin, yPos, altBaslikPaint)
        yPos += 25f

        // Sipariş özetini satır satır ayırıp PDF'e basıyoruz
        val urunler = siparis.siparisOzeti.split("\n")
        for (urun in urunler) {
            if (urun.isNotBlank()) {
                canvas.drawText("- $urun", solMargin, yPos, metinPaint)
                yPos += 20f
            }
        }

        yPos += 20f
        canvas.drawLine(solMargin, yPos, 555f, yPos, cizgiPaint)
        yPos += 30f

        // 6. Genel Toplam
        val formatliTutar = NumberFormat.getNumberInstance(Locale("tr", "TR")).format(siparis.toplamTutar)
        canvas.drawText("GENEL TOPLAM: $formatliTutar TL", solMargin, yPos, tutarPaint)

        pdfDocument.finishPage(page)

        // 7. Dosyayı Telefonun "İndirilenler" Klasörüne Kaydetme
        try {
            val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val dosyaAdi = "Irsaliye_${siparis.sirketUnvani.take(5)}_${siparis.siparisId.take(4)}.pdf"
            val file = File(directory, dosyaAdi)

            pdfDocument.writeTo(FileOutputStream(file))
            Toast.makeText(context, "İrsaliye indirildi! (İndirilenler Klasörüne bakın)", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "PDF kaydedilemedi: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            pdfDocument.close()
        }
    }
}