package com.nexauren.docpilot.ui

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class AppLanguage(val localeCode: String, val displayName: String) {
    PORTUGUESE("pt", "Português"),
    ENGLISH("en", "English"),
    SPANISH("es", "Español"),
    FRENCH("fr", "Français"),
    GERMAN("de", "Deutsch"),
}

object DocPilotLanguageManager {
    private const val PREFS = "docpilot_preferences"
    private const val KEY_LANGUAGE = "app_language"

    var currentLanguage by mutableStateOf(AppLanguage.PORTUGUESE)
        private set

    fun load(context: Context) {
        val code = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_LANGUAGE, AppLanguage.PORTUGUESE.localeCode)
        currentLanguage = AppLanguage.entries.firstOrNull { it.localeCode == code }
            ?: AppLanguage.PORTUGUESE
    }

    fun setLanguage(context: Context, language: AppLanguage) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_LANGUAGE, language.localeCode).apply()
        currentLanguage = language
    }

    fun translate(source: String, language: AppLanguage = currentLanguage): String {
        
        if (language == AppLanguage.PORTUGUESE || source.isBlank()) return source
        translations[source]?.let { return it[language.ordinal - 1] }
        return translateDynamic(source, language) ?: source
    }

    private fun translateDynamic(source: String, language: AppLanguage): String? {
        fun translatedNumber(regex: Regex, values: (String) -> Array<String>): String? {
            val match = regex.matchEntire(source) ?: return null
            return values(match.groupValues[1])[language.ordinal - 1]
        }
        translatedNumber(Regex("^(\\d+) documentos$")) { n ->
            arrayOf("$n documents", "$n documentos", "$n documents", "$n Dokumente")
        }?.let { return it }
        translatedNumber(Regex("^(\\d+) favoritos$")) { n ->
            arrayOf("$n favourites", "$n favoritos", "$n favoris", "$n Favoriten")
        }?.let { return it }
        translatedNumber(Regex("^(\\d+) resultados$")) { n ->
            arrayOf("$n results", "$n resultados", "$n résultats", "$n Ergebnisse")
        }?.let { return it }
        translatedNumber(Regex("^Página (\\d+)$")) { n ->
            arrayOf("Page $n", "Página $n", "Page $n", "Seite $n")
        }?.let { return it }

        val pageTotal = Regex("^Página (\\d+) de (\\d+)$").matchEntire(source)
        if (pageTotal != null) {
            val current = pageTotal.groupValues[1]
            val total = pageTotal.groupValues[2]
            return arrayOf("Page $current of $total", "Página $current de $total", "Page $current sur $total", "Seite $current von $total")[language.ordinal - 1]
        }
        val pdfTotal = Regex("^Este PDF tem (\\d+) páginas\\.$").matchEntire(source)
        if (pdfTotal != null) {
            val total = pdfTotal.groupValues[1]
            return arrayOf("This PDF has $total pages.", "Este PDF tiene $total páginas.", "Ce PDF contient $total pages.", "Dieses PDF hat $total Seiten.")[language.ordinal - 1]
        }
        val version = Regex("^DocPilot • versão (.+)$").matchEntire(source)
        if (version != null) {
            val value = version.groupValues[1]
            return arrayOf("DocPilot • version $value", "DocPilot • versión $value", "DocPilot • version $value", "DocPilot • Version $value")[language.ordinal - 1]
        }
        return null
    }

    private val translations: Map<String, List<String>> = listOf(
        "Início" to listOf("Home", "Inicio", "Accueil", "Startseite"),
        "Biblioteca" to listOf("Library", "Biblioteca", "Bibliothèque", "Bibliothek"),
        "Ferramentas" to listOf("Tools", "Herramientas", "Outils", "Werkzeuge"),
        "Definições" to listOf("Settings", "Ajustes", "Paramètres", "Einstellungen"),
        "Abrir menu" to listOf("Open menu", "Abrir menú", "Ouvrir le menu", "Menü öffnen"),
        "DocPilot, à tua maneira." to listOf("DocPilot, your way.", "DocPilot, a tu manera.", "DocPilot, à votre façon.", "DocPilot, ganz nach deinem Geschmack."),
        "Preferências, conta opcional e privacidade." to listOf("Preferences, optional account and privacy.", "Preferencias, cuenta opcional y privacidad.", "Préférences, compte facultatif et confidentialité.", "Einstellungen, optionales Konto und Datenschutz."),
        "Conta DocPilot (opcional)" to listOf("DocPilot account (optional)", "Cuenta DocPilot (opcional)", "Compte DocPilot (facultatif)", "DocPilot-Konto (optional)"),
        "Podes ler e tratar PDFs sem criar uma conta. A conta é opcional e serve apenas para iniciar sessão." to listOf("You can read and work with PDFs without creating an account. Signing in is optional.", "Puedes leer y trabajar con PDF sin crear una cuenta. Iniciar sesión es opcional.", "Vous pouvez lire et traiter des PDF sans créer de compte. La connexion est facultative.", "Du kannst PDFs ohne Konto lesen und bearbeiten. Die Anmeldung ist optional."),
        "Sessão iniciada" to listOf("Signed in", "Sesión iniciada", "Session ouverte", "Angemeldet"),
        "Terminar sessão" to listOf("Sign out", "Cerrar sesión", "Se déconnecter", "Abmelden"),
        "Continuar com Google" to listOf("Continue with Google", "Continuar con Google", "Continuer avec Google", "Mit Google fortfahren"),
        "ou utiliza o e-mail e a palavra-passe" to listOf("or use email and password", "o usa tu correo y contraseña", "ou utilisez votre e-mail et votre mot de passe", "oder E-Mail und Passwort verwenden"),
        "E-mail" to listOf("Email", "Correo electrónico", "E-mail", "E-Mail"),
        "Palavra-passe" to listOf("Password", "Contraseña", "Mot de passe", "Passwort"),
        "Esqueci a palavra-passe" to listOf("Forgot password", "Olvidé mi contraseña", "Mot de passe oublié", "Passwort vergessen"),
        "Biblioteca do dispositivo" to listOf("Device library", "Biblioteca del dispositivo", "Bibliothèque de l’appareil", "Gerätebibliothek"),
        "Atualizar biblioteca" to listOf("Refresh library", "Actualizar biblioteca", "Actualiser la bibliothèque", "Bibliothek aktualisieren"),
        "Configurar acesso a documentos" to listOf("Set up document access", "Configurar acceso a documentos", "Configurer l’accès aux documents", "Dokumentzugriff einrichten"),
        "Aparência" to listOf("Appearance", "Apariencia", "Apparence", "Darstellung"),
        "O tema acompanha as definições do sistema." to listOf("The theme follows system settings.", "El tema sigue la configuración del sistema.", "Le thème suit les paramètres du système.", "Das Design folgt den Systemeinstellungen."),
        "Privacidade dos documentos" to listOf("Document privacy", "Privacidad de los documentos", "Confidentialité des documents", "Dokumentenschutz"),
        "Os PDFs são processados localmente. Não precisas de uma conta para usar as ferramentas locais." to listOf("PDFs are processed on this device. No account is needed for local tools.", "Los PDF se procesan en este dispositivo. No necesitas una cuenta para usar las herramientas locales.", "Les PDF sont traités sur cet appareil. Aucun compte n’est requis pour les outils locaux.", "PDFs werden auf diesem Gerät verarbeitet. Für lokale Werkzeuge ist kein Konto nötig."),
        "Sobre o DocPilot" to listOf("About DocPilot", "Acerca de DocPilot", "À propos de DocPilot", "Über DocPilot"),
        "Funcionalidades" to listOf("Features", "Funciones", "Fonctionnalités", "Funktionen"),
        "Leitor de PDF, biblioteca de documentos, scanner, OCR e ferramentas de organização." to listOf("PDF reader, document library, scanner, OCR and organization tools.", "Lector PDF, biblioteca de documentos, escáner, OCR y herramientas de organización.", "Lecteur PDF, bibliothèque de documents, scanner, OCR et outils d’organisation.", "PDF-Reader, Dokumentbibliothek, Scanner, OCR und Organisationswerkzeuge."),
        "Idioma da aplicação" to listOf("App language", "Idioma de la aplicación", "Langue de l’application", "App-Sprache"),
        "Escolhe o idioma da interface." to listOf("Choose the interface language.", "Elige el idioma de la interfaz.", "Choisissez la langue de l’interface.", "Wähle die Sprache der Oberfläche."),
        "Alterar" to listOf("Change", "Cambiar", "Modifier", "Ändern"),
        "Fechar" to listOf("Close", "Cerrar", "Fermer", "Schließen"),
        "Política de privacidade" to listOf("Privacy policy", "Política de privacidad", "Politique de confidentialité", "Datenschutzerklärung"),
        "Ler política de privacidade" to listOf("Read privacy policy", "Leer la política de privacidad", "Lire la politique de confidentialité", "Datenschutzerklärung lesen"),
        "Aceder aos teus documentos" to listOf("Access your documents", "Accede a tus documentos", "Accéder à vos documents", "Auf deine Dokumente zugreifen"),
        "Continuar" to listOf("Continue", "Continuar", "Continuer", "Weiter"),
        "Agora não" to listOf("Not now", "Ahora no", "Pas maintenant", "Jetzt nicht"),
        "Espaço de documentos" to listOf("Document space", "Espacio de documentos", "Espace documentaire", "Dokumentenbereich"),
        "Pesquisar os teus documentos" to listOf("Search your documents", "Buscar tus documentos", "Rechercher vos documents", "Dokumente durchsuchen"),
        "Nenhum documento encontrado" to listOf("No documents found", "No se encontraron documentos", "Aucun document trouvé", "Keine Dokumente gefunden"),
        "Tenta outro nome ou parte do nome do ficheiro." to listOf("Try another name or part of the filename.", "Prueba con otro nombre o parte del nombre del archivo.", "Essayez un autre nom ou une partie du nom de fichier.", "Versuche einen anderen Namen oder einen Teil des Dateinamens."),
        "Os teus documentos, organizados." to listOf("Your documents, organized.", "Tus documentos, organizados.", "Vos documents, bien organisés.", "Deine Dokumente, organisiert."),
        "Lê, gere e transforma PDFs num espaço simples e privado." to listOf("Read, manage and transform PDFs in one simple, private space.", "Lee, gestiona y transforma PDF en un espacio sencillo y privado.", "Lisez, gérez et transformez vos PDF dans un espace simple et privé.", "Lies, verwalte und bearbeite PDFs in einem einfachen, privaten Bereich."),
        "O TEU ESPAÇO DE TRABALHO" to listOf("YOUR WORKSPACE", "TU ESPACIO DE TRABAJO", "VOTRE ESPACE DE TRAVAIL", "DEIN ARBEITSBEREICH"),
        "Tudo começa com um PDF." to listOf("It starts with a PDF.", "Todo empieza con un PDF.", "Tout commence avec un PDF.", "Alles beginnt mit einem PDF."),
        "Abre um documento para ler ou usa as ferramentas para o preparar como precisas." to listOf("Open a document to read it or use the tools to prepare it.", "Abre un documento para leerlo o usa las herramientas para prepararlo.", "Ouvrez un document pour le lire ou utilisez les outils pour le préparer.", "Öffne ein Dokument zum Lesen oder bereite es mit den Werkzeugen vor."),
        "ABERTO RECENTEMENTE" to listOf("RECENTLY OPENED", "ABIERTO RECIENTEMENTE", "OUVERT RÉCEMMENT", "KÜRZLICH GEÖFFNET"),
        "Toca para abrir o documento" to listOf("Tap to open the document", "Toca para abrir el documento", "Touchez pour ouvrir le document", "Tippe, um das Dokument zu öffnen"),
        "Abrir documento  →" to listOf("Open document  →", "Abrir documento  →", "Ouvrir le document  →", "Dokument öffnen  →"),
        "Acesso rápido" to listOf("Quick access", "Acceso rápido", "Accès rapide", "Schnellzugriff"),
        "Privacidade por defeito" to listOf("Privacy by default", "Privacidad por defecto", "Confidentialité par défaut", "Datenschutz standardmäßig"),
        "Os teus PDFs são trabalhados localmente. Não precisas de criar uma conta." to listOf("Your PDFs are processed locally. No account is required.", "Tus PDF se procesan localmente. No necesitas crear una cuenta.", "Vos PDF sont traités localement. Aucun compte n’est nécessaire.", "Deine PDFs werden lokal verarbeitet. Ein Konto ist nicht nötig."),
        "Documentos recentes" to listOf("Recent documents", "Documentos recientes", "Documents récents", "Zuletzt verwendete Dokumente"),
        "Continua de onde ficaste" to listOf("Pick up where you left off", "Continúa donde lo dejaste", "Reprenez là où vous en étiez", "Mach dort weiter, wo du aufgehört hast"),
        "Ver todos" to listOf("View all", "Ver todos", "Tout afficher", "Alle anzeigen"),
        "A tua biblioteca começa aqui" to listOf("Your library starts here", "Tu biblioteca empieza aquí", "Votre bibliothèque commence ici", "Deine Bibliothek beginnt hier"),
        "Abrir um PDF individual" to listOf("Open a single PDF", "Abrir un PDF individual", "Ouvrir un PDF individuel", "Ein einzelnes PDF öffnen"),
        "Os teus ficheiros" to listOf("Your files", "Tus archivos", "Vos fichiers", "Deine Dateien"),
        "Pesquisa, organiza e marca os PDFs importantes." to listOf("Search, organize and favorite important PDFs.", "Busca, organiza y marca los PDF importantes.", "Recherchez, organisez et ajoutez aux favoris les PDF importants.", "Suche, organisiere und markiere wichtige PDFs als Favoriten."),
        "Neste dispositivo" to listOf("On this device", "En este dispositivo", "Sur cet appareil", "Auf diesem Gerät"),
        "Pesquisar documentos" to listOf("Search documents", "Buscar documentos", "Rechercher des documents", "Dokumente suchen"),
        "Recentes" to listOf("Recent", "Recientes", "Récents", "Zuletzt"),
        "Nome A–Z" to listOf("Name A–Z", "Nombre A–Z", "Nom A–Z", "Name A–Z"),
        "Favoritos" to listOf("Favourites", "Favoritos", "Favoris", "Favoriten"),
        "Claro" to listOf("Light", "Claro", "Clair", "Hell"),
        "Sépia" to listOf("Sepia", "Sepia", "Sépia", "Sepia"),
        "Escuro" to listOf("Dark", "Oscuro", "Sombre", "Dunkel"),
        "Pesquisar neste PDF" to listOf("Search this PDF", "Buscar en este PDF", "Rechercher dans ce PDF", "Dieses PDF durchsuchen"),
        "Escreve uma palavra ou frase" to listOf("Enter a word or phrase", "Escribe una palabra o frase", "Saisissez un mot ou une phrase", "Gib ein Wort oder einen Satz ein"),
        "A procurar enquanto escreves…" to listOf("Searching as you type…", "Buscando mientras escribes…", "Recherche pendant la saisie…", "Suche während der Eingabe…"),
        "Os resultados aparecem automaticamente." to listOf("Results appear automatically.", "Los resultados aparecen automáticamente.", "Les résultats s’affichent automatiquement.", "Ergebnisse werden automatisch angezeigt."),
        "MODO DE LEITURA" to listOf("READING MODE", "MODO DE LECTURA", "MODE LECTURE", "LESEMODUS"),
        "Ir para página" to listOf("Go to page", "Ir a la página", "Aller à la page", "Zu Seite springen"),
        "Número da página" to listOf("Page number", "Número de página", "Numéro de page", "Seitennummer"),
        "Ir" to listOf("Go", "Ir", "Aller", "Los"),
        "Cancelar" to listOf("Cancel", "Cancelar", "Annuler", "Abbrechen"),
        "Não foi possível abrir este PDF." to listOf("Couldn't open this PDF.", "No se pudo abrir este PDF.", "Impossible d’ouvrir ce PDF.", "Dieses PDF konnte nicht geöffnet werden."),
        "Não foi possível pesquisar este PDF." to listOf("Couldn't search this PDF.", "No se pudo buscar en este PDF.", "Impossible de rechercher dans ce PDF.", "Dieses PDF konnte nicht durchsucht werden."),
        "Não foi possível extrair o texto deste PDF." to listOf("Couldn't extract text from this PDF.", "No se pudo extraer texto de este PDF.", "Impossible d’extraire le texte de ce PDF.", "Text konnte nicht aus diesem PDF extrahiert werden."),
        "Este PDF não contém texto extraível. Para documentos digitalizados, usa OCR." to listOf("This PDF has no extractable text. Use OCR for scanned documents.", "Este PDF no contiene texto extraíble. Usa OCR para documentos escaneados.", "Ce PDF ne contient pas de texte extractible. Utilisez l’OCR pour les documents numérisés.", "Dieses PDF enthält keinen extrahierbaren Text. Verwende OCR für Scans."),
        "Não foram encontradas correspondências. PDFs digitalizados podem precisar de OCR." to listOf("No matches found. Scanned PDFs may need OCR.", "No se encontraron coincidencias. Los PDF escaneados pueden necesitar OCR.", "Aucun résultat. Les PDF numérisés peuvent nécessiter l’OCR.", "Keine Treffer gefunden. Gescannte PDFs benötigen möglicherweise OCR."),
        "Não foi possível carregar esta página." to listOf("Couldn't load this page.", "No se pudo cargar esta página.", "Impossible de charger cette page.", "Diese Seite konnte nicht geladen werden."),
        "A biblioteca está pronta para os teus PDFs" to listOf("Your PDF library is ready", "Tu biblioteca de PDF está lista", "Votre bibliothèque PDF est prête", "Deine PDF-Bibliothek ist bereit"),
        "Ainda não tens favoritos" to listOf("You haven't added favourites yet", "Aún no tienes favoritos", "Vous n’avez pas encore de favoris", "Du hast noch keine Favoriten"),
        "Não encontrámos esse documento" to listOf("We couldn't find that document", "No encontramos ese documento", "Nous n’avons pas trouvé ce document", "Dieses Dokument wurde nicht gefunden"),
        "Quando abrires um PDF, ele ficará listado aqui." to listOf("PDFs you open will appear here.", "Los PDF que abras aparecerán aquí.", "Les PDF ouverts apparaîtront ici.", "Geöffnete PDFs erscheinen hier."),
        "Toca na estrela de um documento para o guardar nesta lista." to listOf("Tap a document's star to save it to this list.", "Toca la estrella de un documento para guardarlo aquí.", "Touchez l’étoile d’un document pour l’ajouter à cette liste.", "Tippe auf den Stern eines Dokuments, um es hier zu speichern."),
        "Experimenta pesquisar com outro nome." to listOf("Try searching for another name.", "Prueba con otro nombre.", "Essayez avec un autre nom.", "Versuche einen anderen Namen."),
        "Não foi possível concluir" to listOf("Couldn't complete the operation", "No se pudo completar la operación", "Impossible de terminer l’opération", "Vorgang konnte nicht abgeschlossen werden"),
        "OK" to listOf("OK", "Aceptar", "OK", "OK"),
        "Os teus PDFs, mais simples." to listOf("Your PDFs, made simple.", "Tus PDF, más sencillos.", "Vos PDF, en toute simplicité.", "PDFs, ganz einfach."),
        "Pesquisar ferramentas" to listOf("Search tools", "Buscar herramientas", "Rechercher des outils", "Werkzeuge suchen"),
        "Em breve" to listOf("Coming soon", "Próximamente", "Bientôt disponible", "Demnächst"),
        "Tamanho da página" to listOf("Page size", "Tamaño de página", "Taille de page", "Seitengröße"),
        "Orientação" to listOf("Orientation", "Orientación", "Orientation", "Ausrichtung"),
        "Retrato" to listOf("Portrait", "Vertical", "Portrait", "Hochformat"),
        "Paisagem" to listOf("Landscape", "Horizontal", "Paysage", "Querformat"),
        "Imagem selecionada" to listOf("Image selected", "Imagen seleccionada", "Image sélectionnée", "Bild ausgewählt"),
        "PDF selecionado" to listOf("PDF selected", "PDF seleccionado", "PDF sélectionné", "PDF ausgewählt"),
        "Remover linhas vazias" to listOf("Remove blank lines", "Eliminar líneas vacías", "Supprimer les lignes vides", "Leere Zeilen entfernen"),
        "A processar…" to listOf("Processing…", "Procesando…", "Traitement…", "Verarbeitung…"),
        "Selecionar PDF" to listOf("Select PDF", "Seleccionar PDF", "Sélectionner un PDF", "PDF auswählen"),
        "Selecionar PDFs" to listOf("Select PDFs", "Seleccionar PDF", "Sélectionner des PDF", "PDFs auswählen"),
        "Ficheiros selecionados" to listOf("Selected files", "Archivos seleccionados", "Fichiers sélectionnés", "Ausgewählte Dateien"),
        "De" to listOf("From", "Desde", "De", "Von"),
        "Até" to listOf("To", "Hasta", "À", "Bis"),
        "Nova ordem" to listOf("New order", "Nuevo orden", "Nouvel ordre", "Neue Reihenfolge"),
        "Ex.: 3,1,2,4" to listOf("E.g. 3,1,2,4", "Ej.: 3,1,2,4", "Ex. : 3,1,2,4", "Z. B. 3,1,2,4"),
        "Juntar PDFs" to listOf("Merge PDFs", "Unir PDF", "Fusionner des PDF", "PDFs zusammenführen"),
        "Dividir PDF" to listOf("Split PDF", "Dividir PDF", "Diviser un PDF", "PDF teilen"),
        "Comprimir" to listOf("Compress", "Comprimir", "Compresser", "Komprimieren"),
        "Organizar" to listOf("Organize pages", "Organizar páginas", "Organiser les pages", "Seiten organisieren"),
        "Rodar páginas" to listOf("Rotate pages", "Girar páginas", "Faire pivoter les pages", "Seiten drehen"),
        "Rodar uma página" to listOf("Rotate one page", "Girar una página", "Faire pivoter une page", "Eine Seite drehen"),
        "Recortar página" to listOf("Crop page", "Recortar página", "Rogner la page", "Seite zuschneiden"),
        "Página para PNG" to listOf("Page to PNG", "Página a PNG", "Page en PNG", "Seite als PNG"),
        "Scanner" to listOf("Scanner", "Escáner", "Scanner", "Scanner"),
        "Imagem → PDF" to listOf("Image → PDF", "Imagen → PDF", "Image → PDF", "Bild → PDF"),
        "Assinatura visual" to listOf("Visual signature", "Firma visual", "Signature visuelle", "Visuelle Signatur"),
        "Proteger" to listOf("Protect", "Proteger", "Protéger", "Schützen"),
        "OCR de imagem" to listOf("Image OCR", "OCR de imagen", "OCR d’image", "Bild-OCR"),
        "Extrair texto PDF" to listOf("Extract PDF text", "Extraer texto del PDF", "Extraire le texte du PDF", "PDF-Text extrahieren"),
        "Informações do PDF" to listOf("PDF information", "Información del PDF", "Informations sur le PDF", "PDF-Informationen"),
        "Combinar documentos" to listOf("Combine documents", "Combinar documentos", "Fusionner des documents", "Dokumente zusammenführen"),
        "Extrair páginas" to listOf("Extract pages", "Extraer páginas", "Extraire des pages", "Seiten extrahieren"),
        "Reduzir tamanho" to listOf("Reduce file size", "Reducir tamaño", "Réduire la taille", "Dateigröße reduzieren"),
        "Reordenar páginas" to listOf("Reorder pages", "Reordenar páginas", "Réorganiser les pages", "Seiten neu anordnen"),
        "Rodar todas as páginas" to listOf("Rotate all pages", "Girar todas las páginas", "Faire pivoter toutes les pages", "Alle Seiten drehen"),
        "Exportar uma página como imagem" to listOf("Export one page as an image", "Exportar una página como imagen", "Exporter une page en image", "Eine Seite als Bild exportieren"),
        "Página anterior" to listOf("Previous page", "Página anterior", "Page précédente", "Vorherige Seite"),
        "Página seguinte" to listOf("Next page", "Página siguiente", "Page suivante", "Nächste Seite"),
        "Voltar" to listOf("Back", "Volver", "Retour", "Zurück"),
        "Repor zoom" to listOf("Reset zoom", "Restablecer zoom", "Réinitialiser le zoom", "Zoom zurücksetzen"),
        "Modo de leitura" to listOf("Reading mode", "Modo de lectura", "Mode lecture", "Lesemodus"),
        "Guardar marcador" to listOf("Save bookmark", "Guardar marcador", "Enregistrer le signet", "Lesezeichen speichern"),
        "Remover marcador" to listOf("Remove bookmark", "Quitar marcador", "Supprimer le signet", "Lesezeichen entfernen"),
        "Ler política" to listOf("Read policy", "Leer la política", "Lire la politique", "Richtlinie lesen"),
        "Como os documentos e os dados da conta opcional são tratados." to listOf("How documents and optional account data are handled.", "Cómo se tratan los documentos y los datos opcionales de la cuenta.", "Comment les documents et les données du compte facultatif sont traités.", "Wie Dokumente und optionale Kontodaten verarbeitet werden."),
        "Os ficheiros PDF e as imagens são processados localmente quando usas as ferramentas do leitor. O conteúdo dos documentos não é enviado para os nossos servidores." to listOf("PDF files and images are processed on your device when you use the tools. Document contents are not sent to our servers.", "Los archivos PDF y las imágenes se procesan en tu dispositivo. El contenido no se envía a nuestros servidores.", "Les fichiers PDF et les images sont traités sur votre appareil. Le contenu des documents n’est pas envoyé à nos serveurs.", "PDF-Dateien und Bilder werden auf deinem Gerät verarbeitet. Dokumentinhalte werden nicht an unsere Server gesendet."),
        "A aplicação não inclui Google Analytics nem recolha de eventos para análise de utilização." to listOf("The app does not include usage analytics or collect analytics events.", "La aplicación no incluye análisis de uso ni recopila eventos de análisis.", "L’application n’inclut aucun outil d’analyse d’utilisation et ne collecte pas d’événements analytiques.", "Die App enthält keine Nutzungsanalyse und sammelt keine Analyseereignisse."),
        "A criação de conta é opcional. Se iniciares sessão, o fornecedor de autenticação recebe os dados necessários para criar e gerir essa conta." to listOf("Creating an account is optional. If you sign in, the authentication provider receives the information needed to create and manage the account.", "Crear una cuenta es opcional. Si inicias sesión, el proveedor de autenticación recibe los datos necesarios para crear y gestionar la cuenta.", "La création d’un compte est facultative. Si vous vous connectez, le fournisseur d’authentification reçoit les données nécessaires à la gestion du compte.", "Ein Konto ist optional. Bei der Anmeldung erhält der Authentifizierungsanbieter die für die Kontoverwaltung erforderlichen Daten."),
        "O acesso à biblioteca do dispositivo só é usado depois de o concederes nas definições do Android. Também podes abrir ficheiros individuais através do seletor de documentos." to listOf("Device-library access is used only after you grant it in Android settings. You can also open individual files through the document picker.", "El acceso a la biblioteca solo se usa después de concederlo en los ajustes de Android. También puedes abrir archivos individuales con el selector de documentos.", "L’accès à la bibliothèque n’est utilisé qu’après autorisation dans les paramètres Android. Vous pouvez aussi ouvrir des fichiers via le sélecteur de documents.", "Der Zugriff auf die Gerätebibliothek erfolgt erst nach deiner Freigabe in den Android-Einstellungen. Einzelne Dateien kannst du auch über die Dateiauswahl öffnen."),
        "Para pedidos de privacidade ou apoio, consulta a página do projeto DocPilot no GitHub. Não publiques dados pessoais em espaços públicos." to listOf("For privacy or support requests, use the DocPilot project page on GitHub. Do not post personal data publicly.", "Para solicitudes de privacidad o ayuda, visita la página del proyecto DocPilot en GitHub. No publiques datos personales.", "Pour toute demande de confidentialité ou d’assistance, consultez le projet DocPilot sur GitHub. Ne publiez pas de données personnelles.", "Bei Datenschutz- oder Supportanfragen nutze die DocPilot-Projektseite auf GitHub. Veröffentliche keine persönlichen Daten."),
        "A preparar o PDF…" to listOf("Preparing PDF…", "Preparando el PDF…", "Préparation du PDF…", "PDF wird vorbereitet…"),
        "Numerar páginas" to listOf("Number pages", "Numerar páginas", "Numéroter les pages", "Seiten nummerieren"),
        "Adicionar números de página" to listOf("Add page numbers", "Añadir números de página", "Ajouter des numéros de page", "Seitenzahlen hinzufügen"),
        "Adicionar números em todas as páginas" to listOf("Add numbers to every page", "Añadir números a todas las páginas", "Ajouter des numéros à chaque page", "Alle Seiten nummerieren"),
        "Adiciona um número discreto a cada página e cria uma nova cópia." to listOf("Adds a subtle number to each page and creates a new copy.", "Añade un número discreto a cada página y crea una copia nueva.", "Ajoute un numéro discret à chaque page et crée une nouvelle copie.", "Fügt jeder Seite eine dezente Nummer hinzu und erstellt eine neue Kopie."),
        "Numera todas as páginas sem alterar o ficheiro original." to listOf("Numbers every page without changing the original file.", "Numera todas las páginas sin cambiar el archivo original.", "Numérote toutes les pages sans modifier le fichier original.", "Nummeriert alle Seiten, ohne die Originaldatei zu ändern."),
        "A numerar páginas" to listOf("Numbering pages", "Numerando páginas", "Numérotation des pages", "Seiten werden nummeriert"),
        "Selecione um PDF." to listOf("Select a PDF.", "Selecciona un PDF.", "Sélectionnez un PDF.", "Wähle eine PDF-Datei aus."),
        "Seleciona um PDF e uma página válida." to listOf("Select a PDF and a valid page.", "Selecciona un PDF y una página válida.", "Sélectionnez un PDF et une page valide.", "Wähle eine PDF-Datei und eine gültige Seite aus."),
    ).associate { (key, values) -> key to values }
}
