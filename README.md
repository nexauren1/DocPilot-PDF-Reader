# DocPilot

DocPilot is an Android-first document hub focused on reading, organizing and processing PDFs.

## Current features

- Jetpack Compose + Material 3 interface
- Home dashboard, global slide-out navigation menu and animated section transitions
- Document library with local persistence, reliable recent-item order, search clearing and Recentes / Nome A–Z sorting
- Opening a selected PDF launches the reader immediately
- PDFs created by the processing tools are added to the library after a successful save
- Optional device-wide PDF discovery after a clear storage-access explanation and Android permission grant
- PDF opening through the Android document picker remains available as a privacy-friendly fallback
- Multi-page PDF reader with on-demand Android PdfRenderer rendering
- Inline text search in text-based PDFs with matching-page navigation, a reader action menu with sharing/page jump/zoom reset, page bookmarks, saved position and a live reading progress indicator, pinch-to-zoom and reflow reading mode
- Horizontal page thumbnails, direct page selection and previous/next navigation
- Pinch-to-zoom and pan with one-tap zoom reset
- Local PDF processing powered by PdfBox-Android 2.0.27.0
- **Merge PDFs**, **split page ranges**, **reorder pages** and **image compression**
- Scanner flow using Google ML Kit Document Scanner
- OCR for images using ML Kit Text Recognition
- Export selectable text from text-based PDFs to `.txt`
- Inspect PDF filename, page count and size
- Searchable tool catalogue with category filters, distinctive gradients and animated cards
- Animated processing overlay for long-running local operations
- Convert selected images into PDF
- Password-protect a PDF
- Add a visual text signature to PDF pages
- Settings screen, starter app icon and Material 3 theme
- JVM unit tests and GitHub Actions test/build pipeline

## PDF processing and privacy

Processing happens on-device. The app uses the Android system document picker for input and output files; selected PDFs and images are not uploaded to a DocPilot server.

Current tools include:

- **Merge PDFs:** combines pages from two or more source PDFs.
- **Split PDF:** extracts an inclusive page interval.
- **Organize:** creates a new PDF using a custom page order such as `3,1,2,4`.
- **Compress:** downscales oversized raster images and recompresses them.
- **Images to PDF:** creates a PDF from selected image files.
- **Protect:** creates a password-protected PDF.
- **Visual signature:** adds user-provided text to each page. This is not a certificate-based cryptographic digital signature.
- **Scanner/OCR:** uses ML Kit for scanning and text recognition.

## Build

- JDK 17
- Gradle 9.6.0
- Android compile SDK 36
- Minimum Android API 24
- Application ID: `com.nexauren.docpilot`

The GitHub Actions pipeline runs unit tests, builds debug and release APKs, and verifies the signature.

## Signing status

The installable preview release uses the default Android debug signing identity so it can be installed for testing. **This is not the permanent production signing key**. Before a store launch, configure a persistent production keystore through protected GitHub Actions secrets; future versions must keep the same signing key to support in-place updates.

## Roadmap

1. Persisted signing configuration and store-ready signed release.
2. Better compression settings and size reporting.
3. Richer document organization, favorites, folders and history.
4. Scanner/OCR workflows refinements.
5. Additional accessibility, device testing and release checks.
