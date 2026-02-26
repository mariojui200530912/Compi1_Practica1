package com.compi1.practica1

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class DiagramaView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    var nodos = listOf<MainActivity.NodoFlujo>()
        set(value) {
            field = value
            invalidate()
        }

    private val paintFondo = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val paintBorde = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 5f
        color = Color.BLACK
    }
    private val paintTexto = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 40f
    }
    private val paintLinea = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 5f
        color = Color.DKGRAY
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (nodos.isEmpty()) return

        val anchoMedio = width / 2f
        var posY = 100f
        val altoFigura = 150f
        val anchoFigura = 400f

        for ((index, nodo) in nodos.withIndex()) {
            val tipo = nodo.tipoFigura.trim().uppercase().replace(" ", "_")
            // Protección contra colores mal formateados
            paintFondo.color = try { Color.parseColor(nodo.colorFondo) } catch (e: Exception) { Color.LTGRAY }
            paintTexto.color = try { Color.parseColor(nodo.colorTexto) } catch (e: Exception) { Color.BLACK }
            paintTexto.textSize = nodo.tamano

            paintTexto.typeface = when (nodo.fuente.trim().uppercase()) {
                "TIMES" -> android.graphics.Typeface.SERIF
                "COMIC" -> android.graphics.Typeface.create("cursive", android.graphics.Typeface.NORMAL)
                "VERDANA" -> android.graphics.Typeface.SANS_SERIF
                else -> android.graphics.Typeface.DEFAULT // ARIAL por defecto
            }

            val anchoReal = if (tipo == "CIRCULO") altoFigura else anchoFigura
            val rect = RectF(
                anchoMedio - anchoReal / 2f,  // AHORA SÍ USA EL ANCHO REAL
                posY,
                anchoMedio + anchoReal / 2f,  // AHORA SÍ USA EL ANCHO REAL
                posY + altoFigura
            )

            when (tipo) {
                "ELIPSE", "CIRCULO" -> canvas.drawOval(rect, paintFondo)
                "ROMBO" -> dibujarRombo(canvas, rect, paintFondo)
                "RECTANGULO_REDONDEADO", "RECTANGULO_RED" -> canvas.drawRoundRect(rect, 30f, 30f, paintFondo)
                "PARALELOGRAMO", "PARALELO" -> dibujarParalelogramo(canvas, rect, paintFondo)
                else -> canvas.drawRect(rect, paintFondo) // RECTANGULO NORMAL
            }

            when (tipo) {
                "ELIPSE", "CIRCULO" -> canvas.drawOval(rect, paintBorde)
                "ROMBO" -> dibujarRombo(canvas, rect, paintBorde)
                "RECTANGULO_REDONDEADO", "RECTANGULO_RED" -> canvas.drawRoundRect(rect, 30f, 30f, paintBorde)
                "PARALELOGRAMO", "PARALELO" -> dibujarParalelogramo(canvas, rect, paintBorde)
                else -> canvas.drawRect(rect, paintBorde)
            }

            val textY = posY + (altoFigura / 2) - ((paintTexto.descent() + paintTexto.ascent()) / 2)
            canvas.drawText(nodo.texto, anchoMedio, textY, paintTexto)

            if (index < nodos.size - 1) {
                val inicioLineaY = posY + altoFigura
                val finLineaY = inicioLineaY + 100f
                canvas.drawLine(anchoMedio, inicioLineaY, anchoMedio, finLineaY, paintLinea)
                canvas.drawLine(anchoMedio, finLineaY, anchoMedio - 15f, finLineaY - 20f, paintLinea)
                canvas.drawLine(anchoMedio, finLineaY, anchoMedio + 15f, finLineaY - 20f, paintLinea)
            }

            posY += altoFigura + 100f
        }
    }

    private fun dibujarRombo(canvas: Canvas, rect: RectF, paint: Paint) {
        val path = Path()
        path.moveTo(rect.centerX(), rect.top)
        path.lineTo(rect.right, rect.centerY())
        path.lineTo(rect.centerX(), rect.bottom)
        path.lineTo(rect.left, rect.centerY())
        path.close()
        canvas.drawPath(path, paint)
    }

    private fun dibujarParalelogramo(canvas: Canvas, rect: RectF, paint: Paint) {
        val path = Path()
        val offset = 40f // Inclinación
        path.moveTo(rect.left + offset, rect.top)
        path.lineTo(rect.right, rect.top)
        path.lineTo(rect.right - offset, rect.bottom)
        path.lineTo(rect.left, rect.bottom)
        path.close()
        canvas.drawPath(path, paint)
    }
}