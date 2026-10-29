package com.nexauren.docpilot.ui.localization

import android.content.Context
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text as MaterialText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit

object DocPilotLocalization {
    private const val PREFS = "docpilot_preferences"
    private const val KEY_LANGUAGE = "app_language"
    val supportedLanguages = listOf("pt", "en", "fr", "es", "de")

    fun language(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_LANGUAGE, "pt")
            .orEmpty()
            .takeIf { it in supportedLanguages } ?: "pt"

    fun setLanguage(context: Context, languageCode: String) {
        if (languageCode !in supportedLanguages) return
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_LANGUAGE, languageCode).apply()
    }

    fun translate(context: Context, text: String): String =
        translateForLanguage(language(context), text)
}

internal fun translateForLanguage(languageCode: String, text: String): String {
    val index = when (languageCode) {
        "en" -> 1
        "fr" -> 2
        "es" -> 3
        "de" -> 4
        else -> 0
    }
    phrases[text]?.let { return it[index] }
    Regex("^Documento com (\\d+) página\\(s\\)\\.$").matchEntire(text)?.let {
        val n = it.groupValues[1]
        return listOf("Documento com $n página(s).", "Document with $n page(s).", "Document de $n page(s).", "Documento con $n página(s).", "Dokument mit $n Seite(n).")[index]
    }
    Regex("^O documento tem (\\d+) página\\(s\\)\\.$").matchEntire(text)?.let {
        val n = it.groupValues[1]
        return listOf("O documento tem $n página(s).", "The document has $n page(s).", "Le document contient $n page(s).", "El documento tiene $n página(s).", "Das Dokument hat $n Seite(n).")[index]
    }
    Regex("^Páginas disponíveis: 1 a (\\d+)\\. A ordem é preenchida automaticamente ao selecionar o PDF\\.$").matchEntire(text)?.let {
        val n = it.groupValues[1]
        return listOf("Páginas disponíveis: 1 a $n. A ordem é preenchida automaticamente ao selecionar o PDF.", "Available pages: 1 to $n. The order is filled automatically when you select the PDF.", "Pages disponibles : 1 à $n. L’ordre est rempli automatiquement après sélection du PDF.", "Páginas disponibles: 1 a $n. El orden se completa automáticamente al seleccionar el PDF.", "Verfügbare Seiten: 1 bis $n. Die Reihenfolge wird beim Auswählen der PDF-Datei automatisch ausgefüllt.")[index]
    }
    if (text.startsWith("PDF selecionado: ")) {
        val name = text.removePrefix("PDF selecionado: ")
        return listOf("PDF selecionado: $name", "PDF selected: $name", "PDF sélectionné : $name", "PDF seleccionado: $name", "PDF ausgewählt: $name")[index]
    }
    Regex("^(\\d+) documentos$").matchEntire(text)?.let {
        val n = it.groupValues[1]
        return listOf("$n documentos", "$n documents", "$n documents", "$n documentos", "$n Dokumente")[index]
    }
    Regex("^(\\d+) favoritos$").matchEntire(text)?.let {
        val n = it.groupValues[1]
        return listOf("$n favoritos", "$n favourites", "$n favoris", "$n favoritos", "$n Favoriten")[index]
    }
    Regex("^(\\d+) resultados$").matchEntire(text)?.let {
        val n = it.groupValues[1]
        return listOf("$n resultados", "$n results", "$n résultats", "$n resultados", "$n Ergebnisse")[index]
    }
    Regex("^Página (\\d+) de (\\d+)$").matchEntire(text)?.let {
        val page = it.groupValues[1]
        val total = it.groupValues[2]
        return listOf("Página $page de $total", "Page $page of $total", "Page $page sur $total", "Página $page de $total", "Seite $page von $total")[index]
    }
    if (text.startsWith("DocPilot • versão ")) {
        val version = text.substringAfter("DocPilot • versão ")
        return listOf("DocPilot • versão $version", "DocPilot • version $version", "DocPilot • version $version", "DocPilot • versión $version", "DocPilot • Version $version")[index]
    }
    return text
}

private val phrases: Map<String, List<String>> = mapOf(
    "Início" to listOf("Início", "Home", "Accueil", "Inicio", "Start"),
    "Biblioteca" to listOf("Biblioteca", "Library", "Bibliothèque", "Biblioteca", "Bibliothek"),
    "Ferramentas" to listOf("Ferramentas", "Tools", "Outils", "Herramientas", "Werkzeuge"),
    "Armazenamento interno" to listOf("Armazenamento interno", "Internal storage", "Stockage interne", "Almacenamiento interno", "Interner Speicher"),
    "Ficheiros recentes" to listOf("Ficheiros recentes", "Recent files", "Fichiers récents", "Archivos recientes", "Zuletzt verwendete Dateien"),
    "Documentos já usados nesta aplicação" to listOf("Documentos já usados nesta aplicação", "Files already opened in this app", "Fichiers déjà ouverts dans cette application", "Archivos ya abiertos en esta aplicación", "In dieser App bereits geöffnete Dateien"),
    "Navega pelas pastas sem sair do DocPilot" to listOf("Navega pelas pastas sem sair do DocPilot", "Browse folders without leaving DocPilot", "Parcourez les dossiers sans quitter DocPilot", "Explora carpetas sin salir de DocPilot", "Durchsuche Ordner, ohne DocPilot zu verlassen"),
    "Pasta anterior" to listOf("Pasta anterior", "Parent folder", "Dossier parent", "Carpeta superior", "Übergeordneter Ordner"),
    "Pasta" to listOf("Pasta", "Folder", "Dossier", "Carpeta", "Ordner"),
    "Esta pasta não contém ficheiros deste tipo" to listOf("Esta pasta não contém ficheiros deste tipo", "This folder has no files of this type", "Ce dossier ne contient aucun fichier de ce type", "Esta carpeta no contiene archivos de este tipo", "Dieser Ordner enthält keine Dateien dieses Typs"),
    "Todas" to listOf("Todas", "All", "Toutes", "Todas", "Alle"),
    "Não encontrámos ferramentas" to listOf(
        "Não encontrámos ferramentas",
        "No tools found",
        "Aucun outil trouvé",
        "No se encontraron herramientas",
        "Keine Werkzeuge gefunden"
    ),
    "Experimenta outra pesquisa ou remove os filtros ativos." to listOf(
        "Experimenta outra pesquisa ou remove os filtros ativos.",
        "Try another search or clear the active filters.",
        "Essayez une autre recherche ou effacez les filtres actifs.",
        "Prueba otra búsqueda o borra los filtros activos.",
        "Versuche eine andere Suche oder setze die aktiven Filter zurück."
    ),
    "Limpar filtros" to listOf(
        "Limpar filtros",
        "Clear filters",
        "Effacer les filtres",
        "Borrar filtros",
        "Filter löschen"
    ),
    "PDF WORKSPACE" to listOf("PDF WORKSPACE", "PDF WORKSPACE", "ESPACE PDF", "ESPACIO PDF", "PDF-WORKSPACE"),
    "Os teus documentos. O teu controlo." to listOf(
        "Os teus documentos. O teu controlo.",
        "Your documents. Your control.",
        "Vos documents. Votre contrôle.",
        "Tus documentos. Tú decides.",
        "Deine Dokumente. Deine Kontrolle."
    ),
    "Abre, lê e transforma PDFs num único espaço. Os documentos são processados no dispositivo." to listOf(
        "Abre, lê e transforma PDFs num único espaço. Os documentos são processados no dispositivo.",
        "Open, read and transform PDFs in one place. Documents are processed on your device.",
        "Ouvrez, lisez et transformez des PDF dans un même espace. Les documents sont traités sur votre appareil.",
        "Abre, lee y transforma PDF en un solo espacio. Los documentos se procesan en tu dispositivo.",
        "Öffne, lies und bearbeite PDFs an einem Ort. Dokumente werden auf deinem Gerät verarbeitet."
    ),
    "Abrir PDF" to listOf("Abrir PDF", "Open PDF", "Ouvrir un PDF", "Abrir PDF", "PDF öffnen"),
    "Configurar" to listOf("Configurar", "Set up", "Configurer", "Configurar", "Einrichten"),
    "Atualizar" to listOf("Atualizar", "Refresh", "Actualiser", "Actualizar", "Aktualisieren"),
    "Ativar" to listOf("Ativar", "Enable", "Activer", "Activar", "Aktivieren"),
    "Fechar" to listOf("Fechar", "Close", "Fermer", "Cerrar", "Schließen"),
    "Selecionar imagens" to listOf("Selecionar imagens", "Select images", "Sélectionner des images", "Seleccionar imágenes", "Bilder auswählen"),
    "Selecionar imagens para OCR" to listOf("Selecionar imagens para OCR", "Select images for OCR", "Sélectionner des images pour l’OCR", "Seleccionar imágenes para OCR", "Bilder für OCR auswählen"),
    "Selecionar PDF para extrair texto" to listOf("Selecionar PDF para extrair texto", "Select PDF to extract text", "Sélectionner un PDF pour extraire le texte", "Seleccionar PDF para extraer texto", "PDF zum Textextrahieren auswählen"),
    "Selecionar PDF para análise" to listOf("Selecionar PDF para análise", "Select PDF to analyze", "Sélectionner un PDF à analyser", "Seleccionar PDF para analizar", "PDF zur Analyse auswählen"),
    "Selecionar documento" to listOf("Selecionar documento", "Select document", "Sélectionner un document", "Seleccionar documento", "Dokument auswählen"),
    "Seleciona um ou mais ficheiros" to listOf("Seleciona um ou mais ficheiros", "Select one or more files", "Sélectionnez un ou plusieurs fichiers", "Selecciona uno o varios archivos", "Wähle eine oder mehrere Dateien"),
    "Seleciona um ficheiro" to listOf("Seleciona um ficheiro", "Select a file", "Sélectionnez un fichier", "Selecciona un archivo", "Wähle eine Datei"),
    "Pesquisar ficheiros" to listOf("Pesquisar ficheiros", "Search files", "Rechercher des fichiers", "Buscar archivos", "Dateien suchen"),
    "Ver todos os ficheiros" to listOf("Ver todos os ficheiros", "Browse all files", "Parcourir tous les fichiers", "Ver todos los archivos", "Alle Dateien durchsuchen"),
    "Os ficheiros já usados continuam disponíveis. Ativa o acesso para indexar PDFs e imagens do dispositivo." to listOf(
        "Os ficheiros já usados continuam disponíveis. Ativa o acesso para indexar PDFs e imagens do dispositivo.",
        "Previously used files remain available. Enable access to index PDFs and images on this device.",
        "Les fichiers déjà utilisés restent disponibles. Activez l’accès pour indexer les PDF et images de l’appareil.",
        "Los archivos usados anteriormente siguen disponibles. Activa el acceso para indexar PDF e imágenes del dispositivo.",
        "Bereits verwendete Dateien bleiben verfügbar. Aktiviere den Zugriff, um PDFs und Bilder auf diesem Gerät zu erfassen."
    ),
    "A procurar ficheiros…" to listOf("A procurar ficheiros…", "Scanning files…", "Recherche des fichiers…", "Buscando archivos…", "Dateien werden gesucht…"),
    "Nenhum ficheiro encontrado" to listOf("Nenhum ficheiro encontrado", "No files found", "Aucun fichier trouvé", "No se encontraron archivos", "Keine Dateien gefunden"),
    "A tua lista de ficheiros está vazia" to listOf("A tua lista de ficheiros está vazia", "Your file list is empty", "Votre liste de fichiers est vide", "Tu lista de archivos está vacía", "Deine Dateiliste ist leer"),
    "Não encontrámos ficheiros com esse nome" to listOf("Não encontrámos ficheiros com esse nome", "No files with that name found", "Aucun fichier portant ce nom", "No encontramos archivos con ese nombre", "Keine Dateien mit diesem Namen gefunden"),
    "Importa um PDF para a biblioteca ou ativa o acesso ao dispositivo para mostrar os ficheiros existentes." to listOf(
        "Importa um PDF para a biblioteca ou ativa o acesso ao dispositivo para mostrar os ficheiros existentes.",
        "Import a PDF into your library or enable device access to show existing files.",
        "Importez un PDF dans la bibliothèque ou activez l’accès à l’appareil pour afficher les fichiers existants.",
        "Importa un PDF a la biblioteca o activa el acceso al dispositivo para mostrar los archivos existentes.",
        "Importiere ein PDF in die Bibliothek oder aktiviere den Gerätezugriff, um vorhandene Dateien anzuzeigen."
    ),
    "Experimenta outro termo de pesquisa." to listOf("Experimenta outro termo de pesquisa.", "Try another search term.", "Essayez un autre terme de recherche.", "Prueba otro término de búsqueda.", "Versuche einen anderen Suchbegriff."),
    "Ativar acesso ao dispositivo" to listOf("Ativar acesso ao dispositivo", "Enable device access", "Activer l’accès à l’appareil", "Activar acceso al dispositivo", "Gerätezugriff aktivieren"),
    "Documento PDF" to listOf("Documento PDF", "PDF document", "Document PDF", "Documento PDF", "PDF-Dokument"),
    "Selecionado" to listOf("Selecionado", "Selected", "Sélectionné", "Seleccionado", "Ausgewählt"),
    "Usar seleção" to listOf("Usar seleção", "Use selection", "Utiliser la sélection", "Usar selección", "Auswahl verwenden"),
    "Editar" to listOf("Editar", "Edit", "Modifier", "Editar", "Bearbeiten"),
    "Converter" to listOf("Converter", "Convert", "Convertir", "Convertir", "Konvertieren"),
    "Segurança" to listOf("Segurança", "Security", "Sécurité", "Seguridad", "Sicherheit"),
    "Texto e análise" to listOf("Texto e análise", "Text & analysis", "Texte et analyse", "Texto y análisis", "Text & Analyse"),
    "Ficheiro" to listOf("Ficheiro", "File", "Fichier", "Archivo", "Datei"),
    "Ficheiros" to listOf("Ficheiros", "Files", "Fichiers", "Archivos", "Dateien"),
    "Imagem" to listOf("Imagem", "Image", "Image", "Imagen", "Bild"),
    "Imagens" to listOf("Imagens", "Images", "Images", "Imágenes", "Bilder"),
    "ficheiro de origem" to listOf("ficheiro de origem", "source file", "fichier source", "archivo de origen", "Quelldatei"),
    "ficheiros de origem" to listOf("ficheiros de origem", "source files", "fichiers sources", "archivos de origen", "Quelldateien"),
    "imagem de origem" to listOf("imagem de origem", "source image", "image source", "imagen de origen", "Quellbild"),
    "imagens de origem" to listOf("imagens de origem", "source images", "images sources", "imágenes de origen", "Quellbilder"),
    "página" to listOf("página", "page", "page", "página", "Seite"),
    "páginas" to listOf("páginas", "pages", "pages", "páginas", "Seiten"),
    "O processamento é local. O ficheiro original será preservado." to listOf(
        "O processamento é local. O ficheiro original será preservado.",
        "Processing happens on your device. The original file stays unchanged.",
        "Le traitement se fait sur l’appareil. Le fichier original reste intact.",
        "El procesamiento se realiza en el dispositivo. El archivo original se conserva.",
        "Die Verarbeitung erfolgt auf dem Gerät. Die Originaldatei bleibt unverändert."
    ),
    "O trabalho é feito localmente no dispositivo." to listOf(
        "O trabalho é feito localmente no dispositivo.",
        "Processing runs locally on your device.",
        "Le traitement s’effectue localement sur votre appareil.",
        "El proceso se ejecuta localmente en el dispositivo.",
        "Die Verarbeitung läuft lokal auf deinem Gerät."
    ),
    "Leitor de PDFs e ferramentas" to listOf(
        "Leitor de PDFs e ferramentas",
        "PDF reader and tools",
        "Lecteur PDF et outils",
        "Lector PDF y herramientas",
        "PDF-Reader und Werkzeuge"
    ),
    "Definições" to listOf("Definições", "Settings", "Paramètres", "Ajustes", "Einstellungen"),
    "Leitor e gestor de PDFs" to listOf("Leitor e gestor de PDFs", "PDF reader and manager", "Lecteur et gestionnaire PDF", "Lector y gestor de PDF", "PDF-Reader und Verwaltung"),
    "DocPilot" to listOf("DocPilot", "DocPilot", "DocPilot", "DocPilot", "DocPilot"),
    "Aceder aos teus documentos" to listOf("Aceder aos teus documentos", "Access your documents", "Accéder à vos documents", "Acceder a tus documentos", "Auf Dokumente zugreifen"),
    "O DocPilot pode procurar PDFs no armazenamento partilhado do dispositivo para criar uma biblioteca automática. Os ficheiros são lidos no teu dispositivo e não são enviados para um servidor. Podes continuar a abrir um PDF individualmente se preferires não conceder este acesso." to listOf(
        "O DocPilot pode procurar PDFs no armazenamento partilhado do dispositivo para criar uma biblioteca automática. Os ficheiros são lidos no teu dispositivo e não são enviados para um servidor. Podes continuar a abrir um PDF individualmente se preferires não conceder este acesso.",
        "DocPilot can look for PDFs in shared storage to build a library. Files are read on your device and are not sent to a server. You can still open a single PDF without granting this access.",
        "DocPilot peut rechercher des PDF dans le stockage partagé pour créer une bibliothèque. Les fichiers sont lus sur votre appareil et ne sont pas envoyés à un serveur. Vous pouvez ouvrir un PDF individuellement sans accorder cet accès.",
        "DocPilot puede buscar PDF en el almacenamiento compartido para crear una biblioteca. Los archivos se leen en tu dispositivo y no se envían a un servidor. También puedes abrir un PDF individual sin conceder este acceso.",
        "DocPilot kann im gemeinsam genutzten Speicher nach PDFs suchen. Dateien werden auf deinem Gerät gelesen und nicht an einen Server gesendet. Du kannst einzelne PDFs auch ohne diesen Zugriff öffnen."
    ),
    "Continuar" to listOf("Continuar", "Continue", "Continuer", "Continuar", "Weiter"),
    "Agora não" to listOf("Agora não", "Not now", "Pas maintenant", "Ahora no", "Jetzt nicht"),
    "Espaço de documentos" to listOf("Espaço de documentos", "Document space", "Espace documentaire", "Espacio de documentos", "Dokumentenbereich"),
    "Pesquisar os teus documentos" to listOf("Pesquisar os teus documentos", "Search your documents", "Rechercher vos documents", "Buscar tus documentos", "Dokumente suchen"),
    "Nenhum documento encontrado" to listOf("Nenhum documento encontrado", "No documents found", "Aucun document trouvé", "No se encontraron documentos", "Keine Dokumente gefunden"),
    "Tenta outro nome ou parte do nome do ficheiro." to listOf("Tenta outro nome ou parte do nome do ficheiro.", "Try another filename or part of a filename.", "Essayez un autre nom ou une partie du nom du fichier.", "Prueba otro nombre o parte del nombre del archivo.", "Versuche einen anderen Dateinamen oder einen Teil davon."),
    "Os teus documentos, organizados." to listOf("Os teus documentos, organizados.", "Your documents, organized.", "Vos documents, organisés.", "Tus documentos, organizados.", "Deine Dokumente, organisiert."),
    "Lê, gere e transforma PDFs num espaço simples e privado." to listOf("Lê, gere e transforma PDFs num espaço simples e privado.", "Read, manage and transform PDFs in one simple, private space.", "Lisez, gérez et transformez vos PDF dans un espace simple et privé.", "Lee, gestiona y transforma PDF en un espacio sencillo y privado.", "Lies, verwalte und bearbeite PDFs in einem einfachen, privaten Bereich."),
    "O TEU ESPAÇO DE TRABALHO" to listOf("O TEU ESPAÇO DE TRABALHO", "YOUR WORKSPACE", "VOTRE ESPACE DE TRAVAIL", "TU ESPACIO DE TRABAJO", "DEIN ARBEITSBEREICH"),
    "Tudo começa com um PDF." to listOf("Tudo começa com um PDF.", "Everything starts with a PDF.", "Tout commence par un PDF.", "Todo empieza con un PDF.", "Alles beginnt mit einer PDF-Datei."),
    "Abre um documento para ler ou usa as ferramentas para o preparar como precisas." to listOf("Abre um documento para ler ou usa as ferramentas para o preparar como precisas.", "Open a document to read it, or use tools to prepare it the way you need.", "Ouvrez un document pour le lire ou utilisez les outils pour le préparer.", "Abre un documento para leerlo o usa las herramientas para prepararlo.", "Öffne ein Dokument zum Lesen oder bereite es mit den Werkzeugen vor."),
    "Biblioteca" to listOf("Biblioteca", "Library", "Bibliothèque", "Biblioteca", "Bibliothek"),
    "ABERTO RECENTEMENTE" to listOf("ABERTO RECENTEMENTE", "RECENTLY OPENED", "OUVERT RÉCEMMENT", "ABIERTO RECIENTEMENTE", "KÜRZLICH GEÖFFNET"),
    "Toca para abrir o documento" to listOf("Toca para abrir o documento", "Tap to open the document", "Touchez pour ouvrir le document", "Toca para abrir el documento", "Zum Öffnen antippen"),
    "Abrir documento  →" to listOf("Abrir documento  →", "Open document  →", "Ouvrir le document  →", "Abrir documento  →", "Dokument öffnen  →"),
    "Acesso rápido" to listOf("Acesso rápido", "Quick access", "Accès rapide", "Acceso rápido", "Schnellzugriff"),
    "Privacidade por defeito" to listOf("Privacidade por defeito", "Privacy by default", "Confidentialité par défaut", "Privacidad predeterminada", "Datenschutz als Standard"),
    "Os teus PDFs são trabalhados localmente. Não precisas de criar uma conta." to listOf("Os teus PDFs são trabalhados localmente. Não precisas de criar uma conta.", "Your PDFs are processed locally. No account is required.", "Vos PDF sont traités localement. Aucun compte n’est nécessaire.", "Tus PDF se procesan localmente. No necesitas una cuenta.", "Deine PDFs werden lokal verarbeitet. Ein Konto ist nicht erforderlich."),
    "Documentos recentes" to listOf("Documentos recentes", "Recent documents", "Documents récents", "Documentos recientes", "Zuletzt verwendete Dokumente"),
    "Continua de onde ficaste" to listOf("Continua de onde ficaste", "Pick up where you left off", "Reprenez là où vous en étiez", "Continúa donde lo dejaste", "Dort weitermachen, wo du aufgehört hast"),
    "Ver todos" to listOf("Ver todos", "View all", "Tout afficher", "Ver todos", "Alle anzeigen"),
    "A tua biblioteca começa aqui" to listOf("A tua biblioteca começa aqui", "Your library starts here", "Votre bibliothèque commence ici", "Tu biblioteca empieza aquí", "Deine Bibliothek beginnt hier"),
    "Abrir um PDF individual" to listOf("Abrir um PDF individual", "Open a single PDF", "Ouvrir un PDF individuel", "Abrir un PDF individual", "Einzelne PDF öffnen"),
    "Os teus ficheiros" to listOf("Os teus ficheiros", "Your files", "Vos fichiers", "Tus archivos", "Deine Dateien"),
    "Pesquisa, organiza e marca os PDFs importantes." to listOf("Pesquisa, organiza e marca os PDFs importantes.", "Search, organize and favourite important PDFs.", "Recherchez, organisez et ajoutez vos PDF importants aux favoris.", "Busca, organiza y marca PDF importantes como favoritos.", "Suche, organisiere und favorisiere wichtige PDFs."),
    "Neste dispositivo" to listOf("Neste dispositivo", "On this device", "Sur cet appareil", "En este dispositivo", "Auf diesem Gerät"),
    "Pesquisar documentos" to listOf("Pesquisar documentos", "Search documents", "Rechercher des documents", "Buscar documentos", "Dokumente suchen"),
    "Recentes" to listOf("Recentes", "Recent", "Récents", "Recientes", "Zuletzt"),
    "Nome A–Z" to listOf("Nome A–Z", "Name A–Z", "Nom A–Z", "Nombre A–Z", "Name A–Z"),
    "Favoritos" to listOf("Favoritos", "Favourites", "Favoris", "Favoritos", "Favoriten"),
    "A biblioteca está pronta para os teus PDFs" to listOf("A biblioteca está pronta para os teus PDFs", "Your library is ready for PDFs", "Votre bibliothèque est prête pour les PDF", "Tu biblioteca está lista para tus PDF", "Deine Bibliothek ist bereit für PDFs"),
    "Quando abrires um PDF, ele ficará listado aqui." to listOf("Quando abrires um PDF, ele ficará listado aqui.", "PDFs you open will appear here.", "Les PDF ouverts apparaîtront ici.", "Los PDF que abras aparecerán aquí.", "Geöffnete PDFs erscheinen hier."),
    "Ainda não tens favoritos" to listOf("Ainda não tens favoritos", "No favourites yet", "Aucun favori pour le moment", "Todavía no tienes favoritos", "Noch keine Favoriten"),
    "Toca na estrela de um documento para o guardar nesta lista." to listOf("Toca na estrela de um documento para o guardar nesta lista.", "Tap a document's star to add it to this list.", "Touchez l’étoile d’un document pour l’ajouter à cette liste.", "Toca la estrella de un documento para añadirlo a esta lista.", "Tippe auf den Stern eines Dokuments, um es hier zu speichern."),
    "Não encontrámos esse documento" to listOf("Não encontrámos esse documento", "We couldn't find that document", "Document introuvable", "No encontramos ese documento", "Dokument nicht gefunden"),
    "Experimenta pesquisar com outro nome." to listOf("Experimenta pesquisar com outro nome.", "Try searching for another name.", "Essayez avec un autre nom.", "Prueba con otro nombre.", "Versuche einen anderen Namen."),
    "Remover dos favoritos" to listOf("Remover dos favoritos", "Remove from favourites", "Retirer des favoris", "Quitar de favoritos", "Aus Favoriten entfernen"),
    "Adicionar aos favoritos" to listOf("Adicionar aos favoritos", "Add to favourites", "Ajouter aux favoris", "Añadir a favoritos", "Zu Favoriten hinzufügen"),
    "Pesquisar neste PDF" to listOf("Pesquisar neste PDF", "Search this PDF", "Rechercher dans ce PDF", "Buscar en este PDF", "Dieses PDF durchsuchen"),
    "Escreve uma palavra ou frase" to listOf("Escreve uma palavra ou frase", "Enter a word or phrase", "Saisissez un mot ou une phrase", "Escribe una palabra o frase", "Gib ein Wort oder einen Satz ein"),
    "A procurar enquanto escreves…" to listOf("A procurar enquanto escreves…", "Searching as you type…", "Recherche pendant la saisie…", "Buscando mientras escribes…", "Suche während der Eingabe…"),
    "Os resultados aparecem automaticamente." to listOf("Os resultados aparecem automaticamente.", "Results appear automatically.", "Les résultats apparaissent automatiquement.", "Los resultados aparecen automáticamente.", "Ergebnisse erscheinen automatisch."),
    "MODO DE LEITURA" to listOf("MODO DE LEITURA", "READING MODE", "MODE LECTURE", "MODO DE LECTURA", "LESEMODUS"),
    "Ir para página" to listOf("Ir para página", "Go to page", "Aller à la page", "Ir a la página", "Zu Seite springen"),
    "Número da página" to listOf("Número da página", "Page number", "Numéro de page", "Número de página", "Seitennummer"),
    "Ir" to listOf("Ir", "Go", "Aller", "Ir", "Los"),
    "Cancelar" to listOf("Cancelar", "Cancel", "Annuler", "Cancelar", "Abbrechen"),
    "Tentar novamente" to listOf("Tentar novamente", "Try again", "Réessayer", "Volver a intentarlo", "Erneut versuchen"),
    "A abrir documento…" to listOf("A abrir documento…", "Opening document…", "Ouverture du document…", "Abriendo documento…", "Dokument wird geöffnet…"),
    "Não foi possível mostrar esta página." to listOf("Não foi possível mostrar esta página.", "This page could not be displayed.", "Impossible d’afficher cette page.", "No se pudo mostrar esta página.", "Diese Seite konnte nicht angezeigt werden."),
    "Pré-visualização" to listOf("Pré-visualização", "Preview", "Aperçu", "Vista previa", "Vorschau"),
    "A preparar pré-visualização…" to listOf("A preparar pré-visualização…", "Preparing preview…", "Préparation de l’aperçu…", "Preparando vista previa…", "Vorschau wird vorbereitet…"),
    "O resultado está pronto" to listOf("O resultado está pronto", "Your result is ready", "Le résultat est prêt", "El resultado está listo", "Das Ergebnis ist bereit"),
    "Guardar PDF" to listOf("Guardar PDF", "Save PDF", "Enregistrer le PDF", "Guardar PDF", "PDF speichern"),
    "Voltar às opções" to listOf("Voltar às opções", "Back to options", "Retour aux options", "Volver a las opciones", "Zurück zu den Optionen"),
    "Pré-visualização da imagem" to listOf("Pré-visualização da imagem", "Image preview", "Aperçu de l’image", "Vista previa de imagen", "Bildvorschau"),
    "A página foi exportada" to listOf("A página foi exportada", "The page was exported", "La page a été exportée", "La página se exportó", "Die Seite wurde exportiert"),
    "Guardar imagem PNG" to listOf("Guardar imagem PNG", "Save PNG image", "Enregistrer l’image PNG", "Guardar imagen PNG", "PNG-Bild speichern"),
    "Voltar às ferramentas" to listOf("Voltar às ferramentas", "Back to tools", "Retour aux outils", "Volver a las herramientas", "Zurück zu den Werkzeugen"),
    "Resultado do texto" to listOf("Resultado do texto", "Text result", "Résultat du texte", "Resultado de texto", "Textergebnis"),
    "Texto pronto para rever" to listOf("Texto pronto para rever", "Text ready to review", "Texte prêt à vérifier", "Texto listo para revisar", "Text zum Überprüfen bereit"),
    "Confere o conteúdo antes de o copiar ou guardar num ficheiro TXT." to listOf("Confere o conteúdo antes de o copiar ou guardar num ficheiro TXT.", "Review the content before copying it or saving it as a TXT file.", "Vérifiez le contenu avant de le copier ou de l’enregistrer en TXT.", "Revisa el contenido antes de copiarlo o guardarlo como TXT.", "Prüfe den Inhalt, bevor du ihn kopierst oder als TXT speicherst."),
    "Copiar" to listOf("Copiar", "Copy", "Copier", "Copiar", "Kopieren"),
    "Definições" to listOf("Definições", "Settings", "Paramètres", "Ajustes", "Einstellungen"),
    "DocPilot, à tua maneira." to listOf("DocPilot, à tua maneira.", "DocPilot, your way.", "DocPilot, à votre façon.", "DocPilot, a tu manera.", "DocPilot, ganz nach deinem Geschmack."),
    "Preferências, conta opcional e privacidade." to listOf("Preferências, conta opcional e privacidade.", "Preferences, optional account and privacy.", "Préférences, compte facultatif et confidentialité.", "Preferencias, cuenta opcional y privacidad.", "Einstellungen, optionales Konto und Datenschutz."),
    "Idioma da aplicação" to listOf("Idioma da aplicação", "App language", "Langue de l’application", "Idioma de la aplicación", "App-Sprache"),
    "Escolhe o idioma dos menus e das ferramentas." to listOf("Escolhe o idioma dos menus e das ferramentas.", "Choose the language for menus and tools.", "Choisissez la langue des menus et des outils.", "Elige el idioma de los menús y las herramientas.", "Wähle die Sprache für Menüs und Werkzeuge."),
    "Português" to listOf("Português", "Portuguese", "Portugais", "Portugués", "Portugiesisch"),
    "English" to listOf("English", "English", "Anglais", "Inglés", "Englisch"),
    "Français" to listOf("Français", "French", "Français", "Francés", "Französisch"),
    "Español" to listOf("Español", "Spanish", "Espagnol", "Español", "Spanisch"),
    "Deutsch" to listOf("Deutsch", "German", "Allemand", "Alemán", "Deutsch"),
    "Conta DocPilot (opcional)" to listOf("Conta DocPilot (opcional)", "Account (optional)", "Compte (facultatif)", "Cuenta (opcional)", "Konto (optional)"),
    "Podes continuar a ler e tratar PDFs sem criar uma conta. A conta é opcional e serve para iniciar sessão." to listOf("Podes continuar a ler e tratar PDFs sem criar uma conta. A conta é opcional e serve para iniciar sessão.", "You can read and process PDFs without an account. An account is optional and is used to sign in.", "Vous pouvez lire et traiter des PDF sans compte. Le compte est facultatif et sert à vous connecter.", "Puedes leer y procesar PDF sin cuenta. La cuenta es opcional y sirve para iniciar sesión.", "Du kannst PDFs ohne Konto lesen und bearbeiten. Ein Konto ist optional und dient der Anmeldung."),
    "Sessão iniciada" to listOf("Sessão iniciada", "Signed in", "Connecté", "Sesión iniciada", "Angemeldet"),
    "Terminar sessão" to listOf("Terminar sessão", "Sign out", "Se déconnecter", "Cerrar sesión", "Abmelden"),
    "Continuar com Google" to listOf("Continuar com Google", "Continue with Google", "Continuer avec Google", "Continuar con Google", "Mit Google fortfahren"),
    "ou utiliza o e-mail e a palavra-passe" to listOf("ou utiliza o e-mail e a palavra-passe", "or use email and password", "ou utilisez l’e-mail et le mot de passe", "o usa el correo y la contraseña", "oder E-Mail und Passwort verwenden"),
    "E-mail" to listOf("E-mail", "Email", "E-mail", "Correo electrónico", "E-Mail"),
    "Palavra-passe" to listOf("Palavra-passe", "Password", "Mot de passe", "Contraseña", "Passwort"),
    "Esqueci a palavra-passe" to listOf("Esqueci a palavra-passe", "Forgot password", "Mot de passe oublié", "Olvidé la contraseña", "Passwort vergessen"),
    "Biblioteca do dispositivo" to listOf("Biblioteca do dispositivo", "Device library", "Bibliothèque de l’appareil", "Biblioteca del dispositivo", "Gerätebibliothek"),
    "Aparência" to listOf("Aparência", "Appearance", "Apparence", "Apariencia", "Darstellung"),
    "O tema acompanha as definições do sistema." to listOf("O tema acompanha as definições do sistema.", "The theme follows your system settings.", "Le thème suit les paramètres du système.", "El tema sigue los ajustes del sistema.", "Das Design folgt den Systemeinstellungen."),
    "Privacidade dos documentos" to listOf("Privacidade dos documentos", "Document privacy", "Confidentialité des documents", "Privacidad de los documentos", "Dokumentenschutz"),
    "Os PDFs são processados localmente. Não precisas de uma conta para usar as ferramentas locais." to listOf("Os PDFs são processados localmente. Não precisas de uma conta para usar as ferramentas locais.", "PDFs are processed locally. No account is needed for local tools.", "Les PDF sont traités localement. Aucun compte n’est nécessaire pour les outils locaux.", "Los PDF se procesan localmente. No necesitas una cuenta para las herramientas locales.", "PDFs werden lokal verarbeitet. Für lokale Werkzeuge ist kein Konto nötig."),
    "Política de privacidade" to listOf("Política de privacidade", "Privacy policy", "Politique de confidentialité", "Política de privacidad", "Datenschutzerklärung"),
    "Consulta como os teus dados são tratados." to listOf("Consulta como os teus dados são tratados.", "Learn how your data is handled.", "Découvrez comment vos données sont traitées.", "Consulta cómo se tratan tus datos.", "Erfahre, wie deine Daten verarbeitet werden."),
    "Sobre o DocPilot" to listOf("Sobre o DocPilot", "About DocPilot", "À propos de DocPilot", "Acerca de DocPilot", "Über DocPilot"),
    "Funcionalidades" to listOf("Funcionalidades", "Features", "Fonctionnalités", "Funciones", "Funktionen"),
    "Leitor de PDF, biblioteca de documentos, scanner, OCR e ferramentas de organização." to listOf("Leitor de PDF, biblioteca de documentos, scanner, OCR e ferramentas de organização.", "PDF reader, document library, scanner, OCR and organization tools.", "Lecteur PDF, bibliothèque, scanner, OCR et outils d’organisation.", "Lector PDF, biblioteca, escáner, OCR y herramientas de organización.", "PDF-Reader, Dokumentbibliothek, Scanner, OCR und Organisationswerkzeuge."),
    "Ferramentas" to listOf("Ferramentas", "Tools", "Outils", "Herramientas", "Werkzeuge"),
    "Os teus PDFs, mais simples." to listOf("Os teus PDFs, mais simples.", "Your PDFs, made simpler.", "Vos PDF, en toute simplicité.", "Tus PDF, más sencillos.", "PDFs, einfacher gemacht."),
    "Pesquisar ferramentas" to listOf("Pesquisar ferramentas", "Search tools", "Rechercher des outils", "Buscar herramientas", "Werkzeuge suchen"),
    "Em breve" to listOf("Em breve", "Coming soon", "Bientôt", "Próximamente", "Demnächst"),
    "Tamanho da página" to listOf("Tamanho da página", "Page size", "Taille de page", "Tamaño de página", "Seitengröße"),
    "Orientação" to listOf("Orientação", "Orientation", "Orientation", "Orientación", "Ausrichtung"),
    "Retrato" to listOf("Retrato", "Portrait", "Portrait", "Vertical", "Hochformat"),
    "Paisagem" to listOf("Paisagem", "Landscape", "Paysage", "Horizontal", "Querformat"),
    "Imagem selecionada" to listOf("Imagem selecionada", "Image selected", "Image sélectionnée", "Imagen seleccionada", "Bild ausgewählt"),
    "PDF selecionado" to listOf("PDF selecionado", "PDF selected", "PDF sélectionné", "PDF seleccionado", "PDF ausgewählt"),
    "A processar…" to listOf("A processar…", "Processing…", "Traitement…", "Procesando…", "Wird verarbeitet…"),
    "Selecionar PDF" to listOf("Selecionar PDF", "Select PDF", "Sélectionner un PDF", "Seleccionar PDF", "PDF auswählen"),
    "O processamento acontece localmente no dispositivo." to listOf("O processamento acontece localmente no dispositivo.", "Processing takes place locally on your device.", "Le traitement s’effectue localement sur votre appareil.", "El procesamiento se realiza localmente en tu dispositivo.", "Die Verarbeitung erfolgt lokal auf deinem Gerät."),
    "Selecionar PDFs" to listOf("Selecionar PDFs", "Select PDFs", "Sélectionner des PDF", "Seleccionar PDF", "PDFs auswählen"),
    "Ficheiros selecionados" to listOf("Ficheiros selecionados", "Files selected", "Fichiers sélectionnés", "Archivos seleccionados", "Dateien ausgewählt"),
    "De" to listOf("De", "From", "De", "Desde", "Von"),
    "Até" to listOf("Até", "To", "À", "Hasta", "Bis"),
    "Nova ordem" to listOf("Nova ordem", "New order", "Nouvel ordre", "Nuevo orden", "Neue Reihenfolge"),
    "Ex.: 3,1,2,4" to listOf("Ex.: 3,1,2,4", "E.g. 3,1,2,4", "Ex. : 3,1,2,4", "Ej.: 3,1,2,4", "Z. B. 3,1,2,4"),
    "Rotação" to listOf("Rotação", "Rotation", "Rotation", "Rotación", "Drehung"),
    "Margem do recorte por lado" to listOf("Margem do recorte por lado", "Crop margin per side", "Marge de recadrage par côté", "Margen de recorte por lado", "Zuschnittrand pro Seite"),
    "Selecionar imagem" to listOf("Selecionar imagem", "Select image", "Sélectionner une image", "Seleccionar imagen", "Bild auswählen"),
    "Selecionar imagens" to listOf("Selecionar imagens", "Select images", "Sélectionner des images", "Seleccionar imágenes", "Bilder auswählen"),
    "Página" to listOf("Página", "Page", "Page", "Página", "Seite"),
    "A guardar…" to listOf("A guardar…", "Saving…", "Enregistrement…", "Guardando…", "Wird gespeichert…"),
    "Revê a imagem antes de a guardar. O PDF original não foi alterado." to listOf("Revê a imagem antes de a guardar. O PDF original não foi alterado.", "Review the image before saving. The original PDF has not been changed.", "Vérifiez l’image avant de l’enregistrer. Le PDF original n’a pas été modifié.", "Revisa la imagen antes de guardarla. El PDF original no se ha modificado.", "Prüfe das Bild vor dem Speichern. Die Original-PDF wurde nicht verändert."),
    "Cada imagem será ajustada à página escolhida sem alterar o ficheiro de origem." to listOf("Cada imagem será ajustada à página escolhida sem alterar o ficheiro de origem.", "Each image will be fitted to the chosen page without changing the source file.", "Chaque image sera adaptée à la page choisie sans modifier le fichier source.", "Cada imagen se ajustará a la página elegida sin modificar el archivo original.", "Jedes Bild wird an die ausgewählte Seite angepasst, ohne die Quelldatei zu ändern."),
    "Depois do reconhecimento, poderás rever o texto, copiá-lo ou guardá-lo como TXT." to listOf("Depois do reconhecimento, poderás rever o texto, copiá-lo ou guardá-lo como TXT.", "After recognition, you can review the text, copy it or save it as TXT.", "Après reconnaissance, vous pouvez relire le texte, le copier ou l’enregistrer en TXT.", "Después del reconocimiento, puedes revisar el texto, copiarlo o guardarlo como TXT.", "Nach der Erkennung kannst du den Text prüfen, kopieren oder als TXT speichern."),
    "Reconhece texto de uma ou várias imagens localmente." to listOf("Reconhece texto de uma ou várias imagens localmente.", "Recognizes text from one or multiple images on-device.", "Reconnaît localement le texte d’une ou plusieurs images.", "Reconoce texto de una o varias imágenes localmente.", "Erkennt Text aus einem oder mehreren Bildern lokal."),
    "Remover linhas vazias" to listOf("Remover linhas vazias", "Remove blank lines", "Supprimer les lignes vides", "Eliminar líneas vacías", "Leere Zeilen entfernen"),
    "Útil para limpar texto exportado de alguns documentos." to listOf("Útil para limpar texto exportado de alguns documentos.", "Useful for cleaning exported text from some documents.", "Utile pour nettoyer le texte exporté de certains documents.", "Útil para limpiar el texto exportado de algunos documentos.", "Nützlich, um exportierten Text aus manchen Dokumenten zu bereinigen."),
    "Digitalização pronta para processar" to listOf("Digitalização pronta para processar", "Scan ready to process", "Numérisation prête à traiter", "Escaneo listo para procesar", "Scan bereit zur Verarbeitung"),
    "O DocPilot vai preparar uma pré-visualização. O PDF só será guardado no destino escolhido depois de reveres o resultado." to listOf("O DocPilot vai preparar uma pré-visualização. O PDF só será guardado no destino escolhido depois de reveres o resultado.", "DocPilot will prepare a preview. The PDF is saved to your chosen destination only after you review the result.", "DocPilot va préparer un aperçu. Le PDF ne sera enregistré à l’emplacement choisi qu’après vérification du résultat.", "DocPilot preparará una vista previa. El PDF solo se guardará en el destino elegido después de revisar el resultado.", "DocPilot erstellt eine Vorschau. Die PDF wird erst nach deiner Prüfung am gewählten Speicherort gespeichert."),
    "Consulta o número de páginas, o tamanho do ficheiro, as dimensões da primeira página e os metadados. O original não será alterado." to listOf("Consulta o número de páginas, o tamanho do ficheiro, as dimensões da primeira página e os metadados. O original não será alterado.", "Check page count, file size, first-page dimensions and metadata. The original will not be changed.", "Consultez le nombre de pages, la taille du fichier, les dimensions de la première page et les métadonnées. L’original ne sera pas modifié.", "Consulta el número de páginas, el tamaño, las dimensiones de la primera página y los metadatos. El original no cambiará.", "Sieh Seitenzahl, Dateigröße, Abmessungen der ersten Seite und Metadaten nach. Das Original wird nicht verändert."),
    "O recorte altera a área visível. O conteúdo fora da área recortada não é apagado permanentemente." to listOf("O recorte altera a área visível. O conteúdo fora da área recortada não é apagado permanentemente.", "Cropping changes the visible area. Content outside the crop is not permanently deleted.", "Le recadrage modifie la zone visible. Le contenu hors cadre n’est pas supprimé définitivement.", "El recorte cambia el área visible. El contenido fuera del recorte no se elimina permanentemente.", "Der Zuschnitt verändert den sichtbaren Bereich. Inhalte außerhalb des Ausschnitts werden nicht dauerhaft gelöscht."),

    "Marca de água" to listOf("Marca de água", "Watermark", "Filigrane", "Marca de agua", "Wasserzeichen"),
    "Adicionar texto a todas as páginas" to listOf("Adicionar texto a todas as páginas", "Add text to all pages", "Ajouter du texte sur toutes les pages", "Añadir texto a todas las páginas", "Text auf allen Seiten hinzufügen"),
    "Adiciona uma marca de água a todas as páginas." to listOf("Adiciona uma marca de água a todas as páginas.", "Add a watermark to every page.", "Ajoute un filigrane sur chaque page.", "Añade una marca de agua a todas las páginas.", "Füge auf jeder Seite ein Wasserzeichen hinzu."),
    "Adiciona texto cinzento ao centro de cada página e guarda uma cópia. O ficheiro original não é alterado." to listOf("Adiciona texto cinzento ao centro de cada página e guarda uma cópia. O ficheiro original não é alterado.", "Adds grey text to the center of each page and saves a copy. The original file is unchanged.", "Ajoute un texte gris au centre de chaque page et enregistre une copie. Le fichier original reste inchangé.", "Añade texto gris en el centro de cada página y guarda una copia. El archivo original no se modifica.", "Fügt grauen Text in der Mitte jeder Seite hinzu und speichert eine Kopie. Die Originaldatei bleibt unverändert."),
    "Texto da marca de água" to listOf("Texto da marca de água", "Watermark text", "Texte du filigrane", "Texto de la marca de agua", "Wasserzeichentext"),
    "O texto é colocado ao centro de todas as páginas." to listOf("O texto é colocado ao centro de todas as páginas.", "The text is placed in the center of every page.", "Le texte est placé au centre de chaque page.", "El texto se coloca en el centro de todas las páginas.", "Der Text wird mittig auf jeder Seite platziert."),
    "A adicionar marca de água" to listOf("A adicionar marca de água", "Adding watermark", "Ajout du filigrane", "Añadiendo marca de agua", "Wasserzeichen wird hinzugefügt"),
    "Aplicar marca de água" to listOf("Aplicar marca de água", "Apply watermark", "Appliquer le filigrane", "Aplicar marca de agua", "Wasserzeichen anwenden"),
    "Escreve o texto da marca de água." to listOf("Escreve o texto da marca de água.", "Enter watermark text.", "Saisissez le texte du filigrane.", "Escribe el texto de la marca de agua.", "Gib den Wasserzeichentext ein."),
    "A marca de água pode ter até 80 caracteres." to listOf("A marca de água pode ter até 80 caracteres.", "Watermark text can contain up to 80 characters.", "Le texte du filigrane peut contenir jusqu’à 80 caractères.", "El texto de la marca de agua puede tener hasta 80 caracteres.", "Der Wasserzeichentext darf bis zu 80 Zeichen enthalten."),
    "Juntar PDFs" to listOf("Juntar PDFs", "Merge PDFs", "Fusionner des PDF", "Unir PDF", "PDFs zusammenführen"),
    "Combinar documentos" to listOf("Combinar documentos", "Combine documents", "Combiner des documents", "Combinar documentos", "Dokumente zusammenführen"),
    "Extrair páginas" to listOf("Extrair páginas", "Extract pages", "Extraire des pages", "Extraer páginas", "Seiten extrahieren"),
    "Comprimir" to listOf("Comprimir", "Compress", "Compresser", "Comprimir", "Komprimieren"),
    "Reduzir tamanho" to listOf("Reduzir tamanho", "Reduce file size", "Réduire la taille", "Reducir tamaño", "Dateigröße reduzieren"),
    "Organizar" to listOf("Organizar", "Organize", "Organiser", "Organizar", "Organisieren"),
    "Rodar todas as páginas" to listOf("Rodar todas as páginas", "Rotate all pages", "Pivoter toutes les pages", "Girar todas las páginas", "Alle Seiten drehen"),
    "Rodar uma página" to listOf("Rodar uma página", "Rotate one page", "Pivoter une page", "Girar una página", "Eine Seite drehen"),
    "Rodar só a página escolhida" to listOf("Rodar só a página escolhida", "Rotate only the selected page", "Pivoter uniquement la page choisie", "Girar solo la página elegida", "Nur die ausgewählte Seite drehen"),
    "Ajustar a área de uma página" to listOf("Ajustar a área de uma página", "Adjust a page's visible area", "Ajuster la zone visible d’une page", "Ajustar el área visible de una página", "Sichtbaren Seitenbereich anpassen"),
    "Recortar página" to listOf("Recortar página", "Crop page", "Recadrer une page", "Recortar página", "Seite zuschneiden"),
    "Numerar páginas" to listOf("Numerar páginas", "Number pages", "Numéroter les pages", "Numerar páginas", "Seiten nummerieren"),
    "Adicionar números em todas as páginas" to listOf("Adicionar números em todas as páginas", "Add numbers to every page", "Ajouter un numéro à chaque page", "Añadir números a todas las páginas", "Jede Seite nummerieren"),
    "Adiciona um número discreto a cada página e cria uma nova cópia." to listOf("Adiciona um número discreto a cada página e cria uma nova cópia.", "Adds a discreet number to each page and creates a new copy.", "Ajoute un numéro discret à chaque page et crée une nouvelle copie.", "Añade un número discreto a cada página y crea una copia nueva.", "Fügt jeder Seite eine dezente Nummer hinzu und erstellt eine neue Kopie."),
    "Numera todas as páginas sem alterar o ficheiro original." to listOf("Numera todas as páginas sem alterar o ficheiro original.", "Numbers every page without changing the original file.", "Numérote toutes les pages sans modifier le fichier original.", "Numera todas las páginas sin modificar el archivo original.", "Nummeriert alle Seiten, ohne die Originaldatei zu ändern."),
    "A numerar páginas" to listOf("A numerar páginas", "Numbering pages", "Numérotation des pages", "Numerando páginas", "Seiten werden nummeriert"),
    "Selecione um PDF." to listOf("Selecione um PDF.", "Select a PDF.", "Sélectionnez un PDF.", "Selecciona un PDF.", "Wähle eine PDF-Datei aus."),
    "Página para PNG" to listOf("Página para PNG", "Page to PNG", "Page en PNG", "Página a PNG", "Seite als PNG"),
    "Exportar uma página como imagem" to listOf("Exportar uma página como imagem", "Export one page as an image", "Exporter une page en image", "Exportar una página como imagen", "Eine Seite als Bild exportieren"),
    "Scanner" to listOf("Scanner", "Scanner", "Scanner", "Escáner", "Scanner"),
    "Digitalizar documentos" to listOf("Digitalizar documentos", "Scan documents", "Numériser des documents", "Escanear documentos", "Dokumente scannen"),
    "Imagem → PDF" to listOf("Imagem → PDF", "Image → PDF", "Image → PDF", "Imagen → PDF", "Bild → PDF"),
    "Converter imagens" to listOf("Converter imagens", "Convert images", "Convertir des images", "Convertir imágenes", "Bilder konvertieren"),
    "Adicionar assinatura" to listOf("Adicionar assinatura", "Add signature", "Ajouter une signature", "Añadir firma", "Unterschrift hinzufügen"),
    "Adicionar assinatura ao PDF" to listOf("Adicionar assinatura ao PDF", "Add a signature to the PDF", "Ajouter une signature au PDF", "Añadir una firma al PDF", "Unterschrift zum PDF hinzufügen"),
    "Proteger" to listOf("Proteger", "Protect", "Protéger", "Proteger", "Schützen"),
    "Senha e permissões" to listOf("Senha e permissões", "Password and permissions", "Mot de passe et autorisations", "Contraseña y permisos", "Passwort und Berechtigungen"),
    "Ler texto em fotos" to listOf("Ler texto em fotos", "Read text in photos", "Lire le texte des photos", "Leer texto en fotos", "Text aus Fotos lesen"),
    "Extrair texto PDF" to listOf("Extrair texto PDF", "Extract PDF text", "Extraire le texte PDF", "Extraer texto del PDF", "PDF-Text extrahieren"),
    "Exportar texto para TXT" to listOf("Exportar texto para TXT", "Export text to TXT", "Exporter le texte en TXT", "Exportar texto a TXT", "Text als TXT exportieren"),
    "Informações do PDF" to listOf("Informações do PDF", "PDF information", "Informations PDF", "Información del PDF", "PDF-Informationen"),
    "Páginas, tamanho e metadados" to listOf("Páginas, tamanho e metadados", "Pages, size and metadata", "Pages, taille et métadonnées", "Páginas, tamaño y metadatos", "Seiten, Größe und Metadaten"),
    "Combinar documentos" to listOf("Combinar documentos", "Combine documents", "Combiner des documents", "Combinar documentos", "Dokumente zusammenführen"),
    "Não foi possível concluir" to listOf("Não foi possível concluir", "Couldn't complete the action", "Impossible de terminer", "No se pudo completar", "Aktion konnte nicht abgeschlossen werden"),
    "OK" to listOf("OK", "OK", "OK", "Aceptar", "OK"),
    "Não foi possível abrir este PDF. Verifica o ficheiro e tenta novamente." to listOf("Não foi possível abrir este PDF. Verifica o ficheiro e tenta novamente.", "This PDF couldn't be opened. Check the file and try again.", "Impossible d’ouvrir ce PDF. Vérifiez le fichier et réessayez.", "No se pudo abrir el PDF. Comprueba el archivo e inténtalo de nuevo.", "PDF konnte nicht geöffnet werden. Prüfe die Datei und versuche es erneut."),
    "Não foi possível concluir o processamento." to listOf("Não foi possível concluir o processamento.", "Processing could not be completed.", "Le traitement n’a pas pu aboutir.", "No se pudo completar el procesamiento.", "Die Verarbeitung konnte nicht abgeschlossen werden."),
    "Unir PDFs" to listOf("Unir PDFs", "Merge PDFs", "Fusionner des PDF", "Unir PDF", "PDFs zusammenführen"),
    "Dividir PDF" to listOf("Dividir PDF", "Split PDF", "Diviser un PDF", "Dividir PDF", "PDF teilen"),
    "Reordenar páginas" to listOf("Reordenar páginas", "Reorder pages", "Réorganiser les pages", "Reordenar páginas", "Seiten neu ordnen"),
    "Rodar páginas" to listOf("Rodar páginas", "Rotate pages", "Faire pivoter les pages", "Girar páginas", "Seiten drehen"),
    "Comprimir PDF" to listOf("Comprimir PDF", "Compress PDF", "Compresser un PDF", "Comprimir PDF", "PDF komprimieren"),
    "Imagens para PDF" to listOf("Imagens para PDF", "Images to PDF", "Images en PDF", "Imágenes a PDF", "Bilder zu PDF"),
    "Digitalizar documento" to listOf("Digitalizar documento", "Scan document", "Numériser un document", "Escanear documento", "Dokument scannen"),
    "OCR de imagem" to listOf("OCR de imagem", "Image OCR", "OCR d’image", "OCR de imagen", "Bild-OCR"),
    "Extrair texto de várias imagens" to listOf("Extrair texto de várias imagens", "Extract text from multiple images", "Extraire le texte de plusieurs images", "Extraer texto de varias imágenes", "Text aus mehreren Bildern extrahieren"),
    "PDF para texto" to listOf("PDF para texto", "PDF to text", "PDF en texte", "PDF a texto", "PDF zu Text"),
    "Informação do PDF" to listOf("Informação do PDF", "PDF information", "Informations PDF", "Información del PDF", "PDF-Informationen"),
    "Proteger PDF" to listOf("Proteger PDF", "Protect PDF", "Protéger un PDF", "Proteger PDF", "PDF schützen"),
    "Assinatura de texto" to listOf("Assinatura de texto", "Text signature", "Signature textuelle", "Firma de texto", "Textsignatur")
)

@Composable
fun LText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = LocalTextStyle.current,
) {
    val context = LocalContext.current
    MaterialText(
        text = DocPilotLocalization.translate(context, text),
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        textAlign = textAlign,
        lineHeight = lineHeight,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        minLines = minLines,
        onTextLayout = onTextLayout,
        style = style,
    )
}

@Composable
fun LText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    inlineContent: Map<String, InlineTextContent> = mapOf(),
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = LocalTextStyle.current,
) {
    MaterialText(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        textAlign = textAlign,
        lineHeight = lineHeight,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        minLines = minLines,
        inlineContent = inlineContent,
        onTextLayout = onTextLayout,
        style = style,
    )
}
