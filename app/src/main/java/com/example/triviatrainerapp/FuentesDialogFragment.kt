package com.example.triviatrainerapp

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.fragment.app.DialogFragment

class FuentesDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val tema = arguments?.getString("tema") ?: "Tema desconocido"
        val fuentes = arguments?.getStringArrayList("fuentes") ?: arrayListOf("Sin fuentes")

        // Construye HTML con links
        val mensajeHtml = buildString {
            append("<b>Tema:</b> $tema<br><br><b>Fuentes:</b><br>")
            fuentes.forEach { fuente ->
                append("• <a href=\"$fuente\">$fuente</a><br>")
            }
        }

        // Inflar el layout personalizado
        val view = requireActivity().layoutInflater.inflate(R.layout.dialog_fuentes, null)
        val textView = view.findViewById<TextView>(R.id.textViewFuentesDialog)
        textView.text = HtmlCompat.fromHtml(mensajeHtml, HtmlCompat.FROM_HTML_MODE_LEGACY)
        textView.movementMethod = LinkMovementMethod.getInstance()

        // Crear el diálogo
        return AlertDialog.Builder(requireContext())
            .setTitle("Puedes revisar los siguientes enlaces")
            .setView(view)
            .setPositiveButton("Cerrar") { dialog, _ -> dialog.dismiss() }
            .create()
    }
}
