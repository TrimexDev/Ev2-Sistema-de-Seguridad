
from flask import Flask, jsonify, request, render_template
from datetime import datetime

app = Flask(__name__)

estado = {
    "alarma_activa": False,
    "movimiento": False,
    "puerta_abierta": False,
    "alerta": False,
    "eventos": []
}

def registrar_evento(mensaje):
    estado["eventos"].insert(0, {
        "hora": datetime.now().strftime("%H:%M:%S"),
        "mensaje": mensaje
    })
    estado["eventos"] = estado["eventos"][:20]

@app.route("/")
def inicio():
    return render_template("index.html")

@app.route("/api/status")
def obtener_estado():
    return jsonify(estado)

@app.route("/api/action", methods=["POST"])
def ejecutar_accion():
    datos = request.get_json(silent=True) or {}
    accion = datos.get("action")

    if accion == "arm":
        estado["alarma_activa"] = True
        estado["alerta"] = False
        registrar_evento("Alarma activada")
    elif accion == "disarm":
        estado["alarma_activa"] = False
        estado["alerta"] = False
        registrar_evento("Alarma desactivada")
    elif accion == "motion":
        estado["movimiento"] = True
        registrar_evento("Movimiento detectado")
        if estado["alarma_activa"]:
            estado["alerta"] = True
            registrar_evento("ALERTA: movimiento con alarma activa")
    elif accion == "motion_clear":
        estado["movimiento"] = False
        registrar_evento("Movimiento finalizado")
    elif accion == "door_open":
        estado["puerta_abierta"] = True
        registrar_evento("Puerta abierta")
        if estado["alarma_activa"]:
            estado["alerta"] = True
            registrar_evento("ALERTA: puerta abierta con alarma activa")
    elif accion == "door_close":
        estado["puerta_abierta"] = False
        registrar_evento("Puerta cerrada")
    else:
        return jsonify({"error": "Acción desconocida"}), 400

    return jsonify(estado)

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=False)
