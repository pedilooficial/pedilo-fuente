# STATE FINAL

pedido = {
  numero: null,
  tipo: null,

  categoria: null,
  subcategoria: null,
  local: null,

  local_libre: null,

  origen: null,
  destino: null,

  items: [],

  cliente: {
    nombre: "",
    telefono: "",
    direccion: "",
    referencia: ""
  },

  horario: {
    tipo: "ahora" | "programado",
    valor: null
  },

  pago: "efectivo" | "transferencia",

  que_retira: "",
  que_lleva: "",

  confirmacion: false
}
