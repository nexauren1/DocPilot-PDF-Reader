package com.nexauren.docpilot.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.nexauren.docpilot.MainActivity
import com.nexauren.docpilot.R
import com.nexauren.docpilot.ui.localization.DocPilotLocalization

/**
 * Compact home-screen shortcuts for the most common PDF workflows.
 * Each chip opens the in-app tool catalogue with the matching category selected.
 */
class PdfToolsWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        appWidgetIds.forEach { appWidgetId ->
            appWidgetManager.updateAppWidget(appWidgetId, buildRemoteViews(context))
        }
    }

    companion object {
        fun refreshAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, PdfToolsWidgetProvider::class.java)
            val ids = manager.getAppWidgetIds(component)
            if (ids.isNotEmpty()) {
                PdfToolsWidgetProvider().onUpdate(context, manager, ids)
            }
        }

        private fun buildRemoteViews(context: Context): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.widget_pdf_tools)
            val language = DocPilotLocalization.language(context)

            views.setTextViewText(R.id.widget_title, "DocPilot")
            views.setTextViewText(
                R.id.widget_subtitle,
                localized(language, "PDF Reader & Tools", "PDF Reader & Tools", "Lecteur PDF et outils", "Lector PDF y herramientas", "PDF-Reader und Werkzeuge"),
            )
            views.setTextViewText(
                R.id.widget_manage,
                localized(language, "Organizar", "Organize", "Organiser", "Organizar", "Organisieren"),
            )
            views.setTextViewText(
                R.id.widget_convert,
                localized(language, "Converter", "Convert", "Convertir", "Convertir", "Konvertieren"),
            )
            views.setTextViewText(
                R.id.widget_security,
                localized(language, "Segurança", "Security", "Sécurité", "Seguridad", "Sicherheit"),
            )
            views.setTextViewText(
                R.id.widget_analyze,
                localized(language, "Texto e análise", "Text & analysis", "Texte et analyse", "Texto y análisis", "Text & Analyse"),
            )
            views.setTextViewText(
                R.id.widget_all_tools,
                localized(language, "Todas as ferramentas  ›", "All tools  ›", "Tous les outils  ›", "Todas las herramientas  ›", "Alle Werkzeuge  ›"),
            )
            views.setContentDescription(
                R.id.widget_all_tools,
                localized(language, "Abrir todas as ferramentas PDF", "Open all PDF tools", "Ouvrir tous les outils PDF", "Abrir todas las herramientas PDF", "Alle PDF-Werkzeuge öffnen"),
            )

            views.setOnClickPendingIntent(R.id.widget_title, openTools(context, null))
            views.setOnClickPendingIntent(R.id.widget_subtitle, openTools(context, null))
            views.setOnClickPendingIntent(R.id.widget_manage, openTools(context, "MANAGE"))
            views.setOnClickPendingIntent(R.id.widget_convert, openTools(context, "CONVERT"))
            views.setOnClickPendingIntent(R.id.widget_security, openTools(context, "SECURITY"))
            views.setOnClickPendingIntent(R.id.widget_analyze, openTools(context, "ANALYZE"))
            views.setOnClickPendingIntent(R.id.widget_all_tools, openTools(context, null))
            return views
        }

        private fun openTools(context: Context, category: String?): PendingIntent {
            val intent = Intent(context, MainActivity::class.java).apply {
                putExtra(MainActivity.EXTRA_WIDGET_DESTINATION, "tools")
                if (category == null) {
                    removeExtra(MainActivity.EXTRA_WIDGET_CATEGORY)
                } else {
                    putExtra(MainActivity.EXTRA_WIDGET_CATEGORY, category)
                }
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            val requestCode = category?.hashCode() ?: 1000
            return PendingIntent.getActivity(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }

        private fun localized(
            language: String,
            portuguese: String,
            english: String,
            french: String,
            spanish: String,
            german: String,
        ): String = when (language) {
            "en" -> english
            "fr" -> french
            "es" -> spanish
            "de" -> german
            else -> portuguese
        }
    }
}
