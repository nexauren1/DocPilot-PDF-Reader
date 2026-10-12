package com.nexauren.docpilot.ui.localization

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf

private const val PREFS = "docpilot_preferences"
private const val KEY_LANGUAGE = "language"

val LocalAppLanguage = compositionLocalOf { "pt" }

object DocPilotLanguage {
    val supported = listOf(
        "pt" to "Português",
        "en" to "English",
        "fr" to "Français",
        "es" to "Español",
        "de" to "Deutsch",
    )

    fun current(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_LANGUAGE, "pt")
            ?.takeIf { code -> supported.any { it.first == code } } ?: "pt"

    fun save(context: Context, code: String) {
        if (supported.any { it.first == code }) {
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putString(KEY_LANGUAGE, code).apply()
        }
    }
}

private val translations = mapOf(
    "en" to mapOf(
        "Início" to "Home", "Biblioteca" to "Library", "Ferramentas" to "Tools", "Definições" to "Settings",
        "Leitor e gestor de PDFs" to "PDF reader and manager", "Espaço de documentos" to "Document workspace",
        "Os teus ficheiros" to "Your files", "Pesquisa, organiza e marca os PDFs importantes." to "Search, organize, and favourite important PDFs.",
        "Pesquisar documentos" to "Search documents", "Recentes" to "Recent", "Nome A–Z" to "Name A–Z",
        "Favoritos" to "Favourites", "Neste dispositivo" to "On this device", "resultados" to "results",
        "Ainda não tens favoritos" to "No favourites yet", "Não encontrámos esse documento" to "No document found",
        "A biblioteca está pronta para os teus PDFs" to "Your PDF library is ready",
        "Quando abrires um PDF, ele ficará listado aqui." to "Opened PDFs will appear here.",
        "Toca na estrela de um documento para o guardar nesta lista." to "Tap a document's star to add it to this list.",
        "Experimenta pesquisar com outro nome." to "Try searching for another name.",
        "DocPilot, à tua maneira." to "DocPilot, your way.", "Preferências, conta opcional e privacidade." to "Preferences, optional account, and privacy.",
        "Conta DocPilot (opcional)" to "DocPilot account (optional)",
        "Podes continuar a ler e tratar PDFs sem criar uma conta. O início de sessão é opcional." to "You can read and work with PDFs without creating an account. Sign-in is optional.",
        "Biblioteca do dispositivo" to "Device library", "Atualizar biblioteca" to "Refresh library",
        "Configurar acesso a documentos" to "Set up document access", "Sobre o DocPilot" to "About DocPilot",
        "Funcionalidades" to "Features", "Idioma da aplicação" to "App language",
        "Escolhe o idioma dos menus e das ferramentas compatíveis." to "Choose the language for menus and supported tools.",
        "Leitor de PDF, biblioteca de documentos, scanner, OCR e ferramentas de organização." to "PDF reader, document library, scanner, OCR, and organization tools.",
        "Ferramentas" to "Tools", "Juntar PDFs" to "Merge PDFs", "Dividir PDF" to "Split PDF",
        "Comprimir" to "Compress", "Organizar" to "Reorder pages", "Rodar páginas" to "Rotate pages",
        "Rodar uma página" to "Rotate one page", "Recortar página" to "Crop page", "Página para PNG" to "Page to PNG",
        "Scanner" to "Scanner", "Imagem → PDF" to "Image → PDF", "Assinatura visual" to "Visual signature",
        "Proteger" to "Protect PDF", "OCR de imagem" to "Image OCR", "Extrair texto PDF" to "Extract PDF text",
        "Informações do PDF" to "PDF information", "Pesquisar no PDF" to "Search in PDF", "Ir para página" to "Go to page",
        "Número da página" to "Page number", "Claro" to "Light", "Sépia" to "Sepia", "Escuro" to "Dark",
        "Voltar" to "Back", "Guardar marcador" to "Save bookmark", "Remover marcador" to "Remove bookmark",
        "Abrir menu" to "Open menu", "Idioma" to "Language",
    ),
    "fr" to mapOf(
        "Início" to "Accueil", "Biblioteca" to "Bibliothèque", "Ferramentas" to "Outils", "Definições" to "Paramètres",
        "Leitor e gestor de PDFs" to "Lecteur et gestionnaire PDF", "Espaço de documentos" to "Espace documentaire",
        "Os teus ficheiros" to "Vos fichiers", "Pesquisa, organiza e marca os PDFs importantes." to "Recherchez, organisez et ajoutez vos PDF aux favoris.",
        "Pesquisar documentos" to "Rechercher des documents", "Recentes" to "Récents", "Nome A–Z" to "Nom A–Z",
        "Favoritos" to "Favoris", "Neste dispositivo" to "Sur cet appareil", "resultados" to "résultats",
        "Ainda não tens favoritos" to "Aucun favori pour le moment", "Não encontrámos esse documento" to "Document introuvable",
        "A biblioteca está pronta para os teus PDFs" to "Votre bibliothèque PDF est prête",
        "Quando abrires um PDF, ele ficará listado aqui." to "Les PDF ouverts apparaîtront ici.",
        "Toca na estrela de um documento para o guardar nesta lista." to "Touchez l’étoile d’un document pour l’ajouter ici.",
        "Experimenta pesquisar com outro nome." to "Essayez un autre nom.",
        "DocPilot, à tua maneira." to "DocPilot, à votre façon.", "Preferências, conta opcional e privacidade." to "Préférences, compte facultatif et confidentialité.",
        "Conta DocPilot (opcional)" to "Compte DocPilot (facultatif)",
        "Podes continuar a ler e tratar PDFs sem criar uma conta. O início de sessão é opcional." to "Vous pouvez lire et traiter des PDF sans créer de compte. La connexion est facultative.",
        "Biblioteca do dispositivo" to "Bibliothèque de l’appareil", "Atualizar biblioteca" to "Actualiser la bibliothèque",
        "Configurar acesso a documentos" to "Configurer l’accès aux documents", "Sobre o DocPilot" to "À propos de DocPilot",
        "Funcionalidades" to "Fonctionnalités", "Idioma da aplicação" to "Langue de l’application",
        "Escolhe o idioma dos menus e das ferramentas compatíveis." to "Choisissez la langue des menus et des outils compatibles.",
        "Leitor de PDF, biblioteca de documentos, scanner, OCR e ferramentas de organização." to "Lecteur PDF, bibliothèque, scanner, OCR et outils d’organisation.",
        "Juntar PDFs" to "Fusionner des PDF", "Dividir PDF" to "Diviser un PDF", "Comprimir" to "Compresser",
        "Organizar" to "Réorganiser", "Rodar páginas" to "Pivoter les pages", "Rodar uma página" to "Pivoter une page",
        "Recortar página" to "Rogner une page", "Página para PNG" to "Page en PNG", "Scanner" to "Scanner",
        "Imagem → PDF" to "Image → PDF", "Assinatura visual" to "Signature visuelle", "Proteger" to "Protéger le PDF",
        "OCR de imagem" to "OCR d’image", "Extrair texto PDF" to "Extraire le texte PDF",
        "Informações do PDF" to "Informations PDF", "Pesquisar no PDF" to "Rechercher dans le PDF",
        "Ir para página" to "Aller à la page", "Número da página" to "Numéro de page",
        "Claro" to "Clair", "Sépia" to "Sépia", "Escuro" to "Sombre", "Voltar" to "Retour",
        "Guardar marcador" to "Enregistrer le signet", "Remover marcador" to "Supprimer le signet",
        "Abrir menu" to "Ouvrir le menu", "Idioma" to "Langue",
    ),
    "es" to mapOf(
        "Início" to "Inicio", "Biblioteca" to "Biblioteca", "Ferramentas" to "Herramientas", "Definições" to "Ajustes",
        "Leitor e gestor de PDFs" to "Lector y gestor de PDF", "Espaço de documentos" to "Espacio de documentos",
        "Os teus ficheiros" to "Tus archivos", "Pesquisa, organiza e marca os PDFs importantes." to "Busca, organiza y guarda tus PDF favoritos.",
        "Pesquisar documentos" to "Buscar documentos", "Recentes" to "Recientes", "Nome A–Z" to "Nombre A–Z",
        "Favoritos" to "Favoritos", "Neste dispositivo" to "En este dispositivo", "resultados" to "resultados",
        "Ainda não tens favoritos" to "Todavía no tienes favoritos", "Não encontrámos esse documento" to "No encontramos ese documento",
        "A biblioteca está pronta para os teus PDFs" to "Tu biblioteca PDF está lista",
        "Quando abrires um PDF, ele ficará listado aqui." to "Los PDF que abras aparecerán aquí.",
        "Toca na estrela de um documento para o guardar nesta lista." to "Toca la estrella de un documento para añadirlo aquí.",
        "Experimenta pesquisar com outro nome." to "Prueba con otro nombre.",
        "DocPilot, à tua maneira." to "DocPilot, a tu manera.", "Preferências, conta opcional e privacidade." to "Preferencias, cuenta opcional y privacidad.",
        "Conta DocPilot (opcional)" to "Cuenta DocPilot (opcional)",
        "Podes continuar a ler e tratar PDFs sem criar uma conta. O início de sessão é opcional." to "Puedes leer y trabajar con PDF sin crear una cuenta. Iniciar sesión es opcional.",
        "Biblioteca do dispositivo" to "Biblioteca del dispositivo", "Atualizar biblioteca" to "Actualizar biblioteca",
        "Configurar acesso a documentos" to "Configurar acceso a documentos", "Sobre o DocPilot" to "Acerca de DocPilot",
        "Funcionalidades" to "Funciones", "Idioma da aplicação" to "Idioma de la aplicación",
        "Escolhe o idioma dos menus e das ferramentas compatíveis." to "Elige el idioma de los menús y las herramientas compatibles.",
        "Leitor de PDF, biblioteca de documentos, scanner, OCR e ferramentas de organização." to "Lector PDF, biblioteca, escáner, OCR y herramientas de organización.",
        "Juntar PDFs" to "Unir PDF", "Dividir PDF" to "Dividir PDF", "Comprimir" to "Comprimir",
        "Organizar" to "Reordenar", "Rodar páginas" to "Girar páginas", "Rodar uma página" to "Girar una página",
        "Recortar página" to "Recortar página", "Página para PNG" to "Página a PNG", "Scanner" to "Escáner",
        "Imagem → PDF" to "Imagen → PDF", "Assinatura visual" to "Firma visual", "Proteger" to "Proteger PDF",
        "OCR de imagem" to "OCR de imagen", "Extrair texto PDF" to "Extraer texto PDF",
        "Informações do PDF" to "Información del PDF", "Pesquisar no PDF" to "Buscar en PDF",
        "Ir para página" to "Ir a la página", "Número da página" to "Número de página",
        "Claro" to "Claro", "Sépia" to "Sepia", "Escuro" to "Oscuro", "Voltar" to "Volver",
        "Guardar marcador" to "Guardar marcador", "Remover marcador" to "Quitar marcador",
        "Abrir menu" to "Abrir menú", "Idioma" to "Idioma",
    ),
    "de" to mapOf(
        "Início" to "Start", "Biblioteca" to "Bibliothek", "Ferramentas" to "Werkzeuge", "Definições" to "Einstellungen",
        "Leitor e gestor de PDFs" to "PDF-Reader und -Verwaltung", "Espaço de documentos" to "Dokumentenbereich",
        "Os teus ficheiros" to "Deine Dateien", "Pesquisa, organiza e marca os PDFs importantes." to "Suche, organisiere und markiere wichtige PDFs.",
        "Pesquisar documentos" to "Dokumente suchen", "Recentes" to "Zuletzt verwendet", "Nome A–Z" to "Name A–Z",
        "Favoritos" to "Favoriten", "Neste dispositivo" to "Auf diesem Gerät", "resultados" to "Ergebnisse",
        "Ainda não tens favoritos" to "Noch keine Favoriten", "Não encontrámos esse documento" to "Dokument nicht gefunden",
        "A biblioteca está pronta para os teus PDFs" to "Deine PDF-Bibliothek ist bereit",
        "Quando abrires um PDF, ele ficará listado aqui." to "Geöffnete PDFs werden hier angezeigt.",
        "Toca na estrela de um documento para o guardar nesta lista." to "Tippe auf den Stern, um ein Dokument hier zu speichern.",
        "Experimenta pesquisar com outro nome." to "Versuche einen anderen Namen.",
        "DocPilot, à tua maneira." to "DocPilot, ganz nach deinem Geschmack.", "Preferências, conta opcional e privacidade." to "Einstellungen, optionales Konto und Datenschutz.",
        "Conta DocPilot (opcional)" to "DocPilot-Konto (optional)",
        "Podes continuar a ler e tratar PDFs sem criar uma conta. O início de sessão é opcional." to "Du kannst PDFs ohne Konto lesen und bearbeiten. Die Anmeldung ist optional.",
        "Biblioteca do dispositivo" to "Gerätebibliothek", "Atualizar biblioteca" to "Bibliothek aktualisieren",
        "Configurar acesso a documentos" to "Dokumentzugriff einrichten", "Sobre o DocPilot" to "Über DocPilot",
        "Funcionalidades" to "Funktionen", "Idioma da aplicação" to "App-Sprache",
        "Escolhe o idioma dos menus e das ferramentas compatíveis." to "Wähle die Sprache für Menüs und unterstützte Werkzeuge.",
        "Leitor de PDF, biblioteca de documentos, scanner, OCR e ferramentas de organização." to "PDF-Reader, Dokumentbibliothek, Scanner, OCR und Organisationswerkzeuge.",
        "Juntar PDFs" to "PDFs zusammenführen", "Dividir PDF" to "PDF teilen", "Comprimir" to "Komprimieren",
        "Organizar" to "Seiten sortieren", "Rodar páginas" to "Seiten drehen", "Rodar uma página" to "Eine Seite drehen",
        "Recortar página" to "Seite zuschneiden", "Página para PNG" to "Seite als PNG", "Scanner" to "Scanner",
        "Imagem → PDF" to "Bild → PDF", "Assinatura visual" to "Visuelle Signatur", "Proteger" to "PDF schützen",
        "OCR de imagem" to "Bild-OCR", "Extrair texto PDF" to "PDF-Text extrahieren",
        "Informações do PDF" to "PDF-Informationen", "Pesquisar no PDF" to "Im PDF suchen",
        "Ir para página" to "Zu Seite", "Número da página" to "Seitennummer",
        "Claro" to "Hell", "Sépia" to "Sepia", "Escuro" to "Dunkel", "Voltar" to "Zurück",
        "Guardar marcador" to "Lesezeichen speichern", "Remover marcador" to "Lesezeichen entfernen",
        "Abrir menu" to "Menü öffnen", "Idioma" to "Sprache",
    ),
)

@Composable
fun localized(source: String): String {
    val language = LocalAppLanguage.current
    return translations[language]?.get(source) ?: source
}
