package com.example.utils

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.model.PaiementEntity
import java.io.File
import java.io.FileOutputStream

object PdfReceiptExporter {

    fun generateAndShareReceipt(
        context: Context,
        tenantNom: String,
        tenantParoisse: String,
        paiement: PaiementEntity,
        membreNom: String,
        membreMatricule: String
    ): File? {
        return try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Format A4 standard (72 dpi)
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            val titlePaint = Paint().apply {
                color = Color.rgb(26, 35, 126) // #1A237E Deep Royal Blue
                textSize = 18f
                isFakeBoldText = true
            }

            val subtitlePaint = Paint().apply {
                color = Color.DKGRAY
                textSize = 12f
            }

            val headerPaint = Paint().apply {
                color = Color.rgb(26, 35, 126)
                textSize = 14f
                isFakeBoldText = true
            }

            val textPaint = Paint().apply {
                color = Color.BLACK
                textSize = 11f
            }

            val boldPaint = Paint().apply {
                color = Color.BLACK
                textSize = 11f
                isFakeBoldText = true
            }

            val highlightPaint = Paint().apply {
                color = Color.rgb(46, 125, 50) // #2E7D32 Forest Green
                textSize = 14f
                isFakeBoldText = true
            }

            val linePaint = Paint().apply {
                color = Color.LTGRAY
                strokeWidth = 1f
            }

            var y = 50f

            // En-tête Organisation
            canvas.drawText("REÇU OFFICIEL DE COTISATION", 40f, y, titlePaint)
            y += 24f
            canvas.drawText(tenantNom, 40f, y, subtitlePaint)
            y += 18f
            canvas.drawText(tenantParoisse, 40f, y, subtitlePaint)
            y += 24f
            canvas.drawLine(40f, y, 555f, y, linePaint)
            y += 30f

            // Informations Reçu
            canvas.drawText("Numéro de Reçu :", 40f, y, boldPaint)
            canvas.drawText(paiement.numeroRecu, 170f, y, headerPaint)
            y += 20f

            canvas.drawText("Date de règlement :", 40f, y, textPaint)
            canvas.drawText(DateUtils.formatDateTime(paiement.datePaiement), 170f, y, boldPaint)
            y += 20f

            canvas.drawText("Mode de paiement :", 40f, y, textPaint)
            canvas.drawText("${paiement.modePaiement.name} ${if (paiement.referenceExterne.isNotEmpty()) "(${paiement.referenceExterne})" else ""}", 170f, y, textPaint)
            y += 30f

            canvas.drawLine(40f, y, 555f, y, linePaint)
            y += 30f

            // Informations Membre
            canvas.drawText("BÉNÉFICIAIRE / MEMBRE", 40f, y, headerPaint)
            y += 20f
            canvas.drawText("Nom & Prénoms :", 40f, y, textPaint)
            canvas.drawText(membreNom, 170f, y, boldPaint)
            y += 20f
            canvas.drawText("Matricule :", 40f, y, textPaint)
            canvas.drawText(membreMatricule, 170f, y, textPaint)
            y += 30f

            canvas.drawLine(40f, y, 555f, y, linePaint)
            y += 30f

            // Détails Financiers (Règles Remise & Imputation)
            canvas.drawText("DÉTAILS COMPTABLES", 40f, y, headerPaint)
            y += 24f

            canvas.drawText("Montant réellement versé (Caisse) :", 40f, y, boldPaint)
            canvas.drawText(CurrencyFormatter.formatFcfa(paiement.montantVerse), 350f, y, highlightPaint)
            y += 24f

            if (paiement.montantRemise > 0.0) {
                val remisePaint = Paint().apply {
                    color = Color.rgb(198, 40, 40) // #C62828 Red
                    textSize = 12f
                    isFakeBoldText = true
                }
                canvas.drawText("Remise accordée :", 40f, y, textPaint)
                canvas.drawText(CurrencyFormatter.formatFcfa(paiement.montantRemise), 350f, y, remisePaint)
                y += 20f
            }

            canvas.drawText("Montant total imputé sur les cotisations :", 40f, y, boldPaint)
            canvas.drawText(CurrencyFormatter.formatFcfa(paiement.montantTotalImpute), 350f, y, boldPaint)
            y += 30f

            if (paiement.observation.isNotEmpty()) {
                canvas.drawText("Observation :", 40f, y, textPaint)
                canvas.drawText(paiement.observation, 170f, y, textPaint)
                y += 24f
            }

            y += 20f
            canvas.drawLine(40f, y, 555f, y, linePaint)
            y += 40f

            // Signature & Traçabilité
            canvas.drawText("Enregistré par : ${paiement.enregistreParNom}", 40f, y, textPaint)
            canvas.drawText("Cachet & Signature de l'Organisation", 350f, y, boldPaint)
            y += 40f
            canvas.drawText("Document certifié conforme et traçable dans le journal SaaS GESCOTI.", 40f, y + 40f, subtitlePaint)

            pdfDocument.finishPage(page)

            // Sauvegarde dans cache de l'app
            val cacheDir = File(context.cacheDir, "recus")
            if (!cacheDir.exists()) cacheDir.mkdirs()
            val file = File(cacheDir, "Recu_${paiement.numeroRecu}.pdf")
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            pdfDocument.close()

            // Déclencher le partage Android
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Reçu de cotisation ${paiement.numeroRecu}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Partager le reçu PDF").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })

            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
