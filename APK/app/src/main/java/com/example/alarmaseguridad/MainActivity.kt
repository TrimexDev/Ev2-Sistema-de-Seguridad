
package com.example.alarmaseguridad

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

private const val SERVER_URL = "http://10.206.250.170:5000"

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PantallaAlarma()
                }
            }
        }
    }
}

@Composable
fun PantallaAlarma() {
    var mensaje by remember {
        mutableStateOf("Sistema listo para probar")
    }

    val principal = remember { Handler(Looper.getMainLooper()) }

    fun enviarAccion(accion: String) {
        mensaje = "Enviando acción..."

        Thread {
            var resultado = "No se pudo conectar al servidor"

            try {
                val conexion = URL("$SERVER_URL/api/action")
                    .openConnection() as HttpURLConnection

                conexion.requestMethod = "POST"
                conexion.connectTimeout = 5000
                conexion.readTimeout = 5000
                conexion.doOutput = true
                conexion.setRequestProperty(
                    "Content-Type",
                    "application/json"
                )

                val datos = JSONObject()
                    .put("action", accion)
                    .toString()

                conexion.outputStream.use {
                    it.write(datos.toByteArray(Charsets.UTF_8))
                }

                if (conexion.responseCode in 200..299) {
                    resultado = "Acción enviada correctamente: $accion"
                } else {
                    resultado = "Error del servidor: ${conexion.responseCode}"
                }

                conexion.disconnect()
            } catch (e: Exception) {
                resultado = "Error de conexión: ${e.message}"
            }

            principal.post {
                mensaje = resultado
            }
        }.start()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Alarma de seguridad",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Control desde el celular",
            style = MaterialTheme.typography.bodyLarge
        )

        Text(text = mensaje)

        Button(
            onClick = { enviarAccion("arm") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Activar alarma")
        }

        OutlinedButton(
            onClick = { enviarAccion("disarm") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Desactivar alarma")
        }

        Text(
            text = "Pruebas de sensores",
            style = MaterialTheme.typography.titleLarge
        )

        Button(
            onClick = { enviarAccion("motion") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Simular movimiento infrarrojo")
        }

        OutlinedButton(
            onClick = { enviarAccion("motion_clear") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Restablecer movimiento")
        }

        Button(
            onClick = { enviarAccion("door_open") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Simular apertura magnética")
        }

        OutlinedButton(
            onClick = { enviarAccion("door_close") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Simular cierre de puerta")
        }
    }
}
