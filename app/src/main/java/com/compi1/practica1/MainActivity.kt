package com.compi1.practica1

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.StringReader

import com.compi1.interprete.Lexer
import com.compi1.interprete.AnalizadorSintactico
import com.compi1.interprete.sym

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnCompilar = findViewById<Button>(R.id.btnCompilar)
        val txtEntrada = findViewById<EditText>(R.id.txtEntrada)

        btnCompilar.setOnClickListener {
            val scrollResultados = findViewById<android.widget.ScrollView>(R.id.scrollResultados)
            val tabla = findViewById<TableLayout>(R.id.tablaReportes)
            val vistaDiagrama = findViewById<DiagramaView>(R.id.vistaDiagrama)

            scrollResultados.visibility = View.GONE // Ocultamos el panel
            tabla.removeAllViews()                  // Vaciamos la tabla
            vistaDiagrama.nodos = emptyList()       // Borramos las figuras del diagrama
            // -------------------------------------------

            val codigo = txtEntrada.text.toString()
            if (codigo.isNotEmpty()) {
                ejecutarAnalisis(codigo)
            } else {
                Toast.makeText(this, "Ingresa código para analizar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun ejecutarAnalisis(entrada: String) {
        val reader = StringReader(entrada)
        val lexer = Lexer(reader)
        val parser = AnalizadorSintactico(lexer)

        try {
            parser.parse()

            if (parser.reporteErrores.isEmpty()) {
                val operadores = parser.reporteOperadores
                val estructuras = parser.reporteEstructuras
                Toast.makeText(this, "Compilación exitosa", Toast.LENGTH_SHORT).show()
                mostrarReporteExitoso(parser)
            } else {
                Toast.makeText(this, "Se encontraron errores", Toast.LENGTH_LONG).show()
                mostrarReporteErrores(parser.reporteErrores)
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error fatal durante el análisis", Toast.LENGTH_SHORT).show()
        }
    }

    private fun mostrarReporteErrores(errores: List<Array<String>>) {
        val scrollResultados = findViewById<android.widget.ScrollView>(R.id.scrollResultados)
        val lblTitulo = findViewById<TextView>(R.id.lblTituloReporte)
        val tablaErrores = findViewById<TableLayout>(R.id.tablaReportes)
        val lblTituloEst = findViewById<TextView>(R.id.lblTituloEstructuras)
        val tablaEst = findViewById<TableLayout>(R.id.tablaEstructuras)
        val vistaDiagrama = findViewById<DiagramaView>(R.id.vistaDiagrama)

        scrollResultados.visibility = View.VISIBLE
        vistaDiagrama.visibility = View.GONE

        lblTituloEst.visibility = View.GONE
        tablaEst.visibility = View.GONE

        lblTitulo.text = "Reporte de Errores (Diagrama Bloqueado)"
        lblTitulo.setTextColor(Color.RED)

        val cabeceras = arrayOf("Lexema", "Línea", "Columna", "Tipo", "Descripción")
        llenarTabla(tablaErrores, cabeceras, errores)
    }

    private fun mostrarReporteExitoso(parser: AnalizadorSintactico) {
        val ops = parser.reporteOperadores
        val ests = parser.reporteEstructuras
        val scrollResultados = findViewById<android.widget.ScrollView>(R.id.scrollResultados)
        val lblTituloOps = findViewById<TextView>(R.id.lblTituloReporte)
        val tablaOps = findViewById<TableLayout>(R.id.tablaReportes)
        val lblTituloEst = findViewById<TextView>(R.id.lblTituloEstructuras)
        val tablaEst = findViewById<TableLayout>(R.id.tablaEstructuras)
        val vistaDiagrama = findViewById<DiagramaView>(R.id.vistaDiagrama)

        scrollResultados.visibility = View.VISIBLE
        vistaDiagrama.visibility = View.VISIBLE
        lblTituloEst.visibility = View.VISIBLE
        tablaEst.visibility = View.VISIBLE

        // Tabla 1: Operadores
        lblTituloOps.text = "Reporte de Operadores"
        lblTituloOps.setTextColor(android.graphics.Color.parseColor("#388E3C"))
        val cabecerasOps = arrayOf("Operador", "Línea", "Columna", "Ocurrencia")
        llenarTabla(tablaOps, cabecerasOps, ops)

        // Tabla 2: Estructuras de Control
        val cabecerasEst = arrayOf("Estructura", "Línea", "Condición")
        llenarTabla(tablaEst, cabecerasEst, ests)

        val totalConfigs = parser.configuraciones.size
        Toast.makeText(this, "Configuraciones guardadas: $totalConfigs", Toast.LENGTH_LONG).show()

        val nodosDinamicos = mutableListOf<NodoFlujo>()

        // Nodo Obligatorio
        nodosDinamicos.add(NodoFlujo("INICIO", "ELIPSE", "#4CAF50", "#FFFFFF", "ARIAL", 40.0F))

        val listaParser = parser.reporteDiagrama as? java.util.LinkedList<Array<String>>

        if (listaParser != null) {
            val idxDefault = parser.configuraciones["DEFAULT"] ?: "1"
            var contSi = 1
            var contMie = 1
            var contBloq = 1

            fun parsearColor(valor: String?, colorDefecto: String): String {
                if (valor == null) return colorDefecto

                try {
                    val valLimpio = valor.replace(" ", "").replace("\n", "").replace("\t", "").uppercase()

                    if (valLimpio.startsWith("H")) {
                        return "#" + valLimpio.substring(1) // Cambia la H por #
                    }
                    if (valLimpio.startsWith("#")) {
                        return valLimpio
                    }

                    if (valLimpio.length == 6 && valLimpio.all { it in "0123456789ABCDEF" }) {
                        return "#$valLimpio"
                    }

                    if (valLimpio.contains(",")) {
                        val partes = valLimpio.split(",")
                        if (partes.size == 3) {

                            fun evaluarMate(exp: String): Int {
                                if (exp.contains("+")) return exp.substringBefore("+").toInt() + exp.substringAfter("+").toInt()
                                if (exp.contains("-")) return exp.substringBefore("-").toInt() - exp.substringAfter("-").toInt()
                                if (exp.contains("*")) return exp.substringBefore("*").toInt() * exp.substringAfter("*").toInt()
                                if (exp.contains("/")) return exp.substringBefore("/").toInt() / exp.substringAfter("/").toInt()
                                return exp.toInt()
                            }

                            val r = evaluarMate(partes[0]).coerceIn(0, 255)
                            val g = evaluarMate(partes[1]).coerceIn(0, 255)
                            val b = evaluarMate(partes[2]).coerceIn(0, 255)

                            return String.format("#%02X%02X%02X", r, g, b)
                        }
                    }
                } catch (e: Exception) {
                    return colorDefecto
                }

                return colorDefecto
            }

            for (dato in listaParser) {
                val textoFigura = dato[0]
                var tipoFigura = dato[1]

                var colorFondo = "#E0E0E0"
                var colorTexto = "#000000"

                var fuenteFinal = "ARIAL" // Fuente por defecto
                var tamanoFinal = 40f     // Tamaño por defecto

                if (tipoFigura == "ROMBO_SI") {
                    val fondoConfig = parser.configuraciones["COLOR_SI_$contSi"] ?: parser.configuraciones["COLOR_SI_$idxDefault"]
                    val textoConfig = parser.configuraciones["COLOR_TXT_SI_$contSi"] ?: parser.configuraciones["COLOR_TXT_SI_$idxDefault"]
                    val figConfig = parser.configuraciones["FIG_SI_$contSi"] ?: parser.configuraciones["FIG_SI_$idxDefault"]

                    // NUEVO: Leer configuración de letras
                    val letConfig = parser.configuraciones["LET_SI_$contSi"] ?: parser.configuraciones["LET_SI_$idxDefault"]
                    val sizeConfig = parser.configuraciones["LET_SIZE_SI_$contSi"] ?: parser.configuraciones["LET_SIZE_SI_$idxDefault"]

                    colorFondo = parsearColor(fondoConfig, "#FFC107")
                    colorTexto = parsearColor(textoConfig, "#000000")
                    tipoFigura = figConfig ?: "ROMBO"

                    if (letConfig != null) fuenteFinal = letConfig
                    if (sizeConfig != null) tamanoFinal = sizeConfig.toFloatOrNull() ?: 40f

                    contSi++

                } else if (tipoFigura == "ROMBO_MIE") {
                    val fondoConfig = parser.configuraciones["COLOR_MIE_$contMie"] ?: parser.configuraciones["COLOR_MIE_$idxDefault"]
                    val textoConfig = parser.configuraciones["COLOR_TXT_MIE_$contMie"] ?: parser.configuraciones["COLOR_TXT_MIE_$idxDefault"]
                    val figConfig = parser.configuraciones["FIG_MIE_$contMie"] ?: parser.configuraciones["FIG_MIE_$idxDefault"]

                    // NUEVO: Leer configuración de letras
                    val letConfig = parser.configuraciones["LET_MIE_$contMie"] ?: parser.configuraciones["LET_MIE_$idxDefault"]
                    val sizeConfig = parser.configuraciones["LET_SIZE_MIE_$contMie"] ?: parser.configuraciones["LET_SIZE_MIE_$idxDefault"]

                    colorFondo = parsearColor(fondoConfig, "#FF9800")
                    colorTexto = parsearColor(textoConfig, "#000000")
                    tipoFigura = figConfig ?: "ROMBO"

                    if (letConfig != null) fuenteFinal = letConfig
                    if (sizeConfig != null) tamanoFinal = sizeConfig.toFloatOrNull() ?: 40f

                    contMie++

                } else {
                    val fondoConfig = parser.configuraciones["COLOR_BLOQ_$contBloq"] ?: parser.configuraciones["COLOR_BLOQ_$idxDefault"]
                    val textoConfig = parser.configuraciones["COLOR_TXT_BLOQ_$contBloq"] ?: parser.configuraciones["COLOR_TXT_BLOQ_$idxDefault"]
                    val figConfig = parser.configuraciones["FIG_BLOQ_$contBloq"] ?: parser.configuraciones["FIG_BLOQ_$idxDefault"]

                    val letConfig = parser.configuraciones["LET_BLOQ_$contBloq"] ?: parser.configuraciones["LET_BLOQ_$idxDefault"]
                    val sizeConfig = parser.configuraciones["LET_SIZE_BLOQ_$contBloq"] ?: parser.configuraciones["LET_SIZE_BLOQ_$idxDefault"]

                    colorFondo = parsearColor(fondoConfig, "#2196F3")
                    colorTexto = parsearColor(textoConfig, "#FFFFFF")
                    if (figConfig != null) tipoFigura = figConfig

                    if (letConfig != null) fuenteFinal = letConfig
                    if (sizeConfig != null) tamanoFinal = sizeConfig.toFloatOrNull() ?: 40f

                    contBloq++
                }

                nodosDinamicos.add(NodoFlujo(textoFigura, tipoFigura, colorFondo, colorTexto, fuenteFinal, tamanoFinal))
            }
        }

        nodosDinamicos.add(NodoFlujo("FIN", "ELIPSE", "#F44336", "#FFFFFF", "ARIAL", 40.0F))

        vistaDiagrama.nodos = nodosDinamicos
    }

    private fun llenarTabla(tabla: TableLayout, cabeceras: Array<String>, datos: List<Array<String>>) {
        tabla.removeAllViews()

        val filaCabecera = TableRow(this)
        filaCabecera.setBackgroundColor(Color.LTGRAY)
        for (texto in cabeceras) {
            val tv = TextView(this)
            tv.text = texto
            tv.setPadding(8, 8, 8, 8)
            tv.setTypeface(null, android.graphics.Typeface.BOLD)
            tv.layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
            filaCabecera.addView(tv)
        }
        tabla.addView(filaCabecera)

        for (filaDato in datos) {
            val fila = TableRow(this)
            for (texto in filaDato) {
                val tv = TextView(this)
                // Evitar colapsos si faltan columnas desde Java
                tv.text = texto ?: "N/A"
                tv.setPadding(8, 8, 8, 8)
                tv.layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
                fila.addView(tv)
            }
            tabla.addView(fila)
        }
    }

    data class NodoFlujo(
        val texto: String,
        val tipoFigura: String, // "ELIPSE", "RECTANGULO", "ROMBO", etc.
        val colorFondo: String, // Ej: "#FF5733"
        val colorTexto: String,  // Ej: "#000000"
        val fuente: String,     // NUEVO: Para guardar "ARIAL", "TIMES", etc.
        val tamano: Float
    )
}