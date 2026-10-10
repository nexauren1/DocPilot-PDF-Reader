# DocPilot

DocPilot is an Android-first document hub focused on reading, organizing and processing PDFs.

## Current features

- Jetpack Compose + Material 3 interface
- Modern blue-violet Material 3 design system, redesigned dashboard and library
- Folder-first PDF discovery via the Android system folder picker, including recursive subfolder scanning
- Persistent URI permissions for the folder the user selected, without blanket storage permission
- Searchable local library with reliable recent-item order
- Opening a selected PDF launches the reader immediately
- PDFs created by the processing tools are added to the library after a successful save
- Individual PDF opening remains available as a fallback
- Multi-page PDF reader with on-demand Android PdfRenderer rendering
- Horizontal page thumbnails, direct page selection, page-jump dialog and previous/next navigation
- Full-text PDF search with page result excerpts
- Per-document bookmarks persisted across sessions
- Pinch-to-zoom, pan, bitmap cache, one-tap zoom reset and PDF sharing
- Local PDF processing powered by PdfBox-Android 2.0.27.0
- **Merge PDFs**, **split page ranges**, **reorder pages**, **rotate pages**, **add watermarks**, **add page numbers** and **image compression**
- Scanner flow using Google ML Kit Document Scanner
- OCR for images using ML Kit Text Recognition
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

## Next improvements

1. Add PDF highlights and freehand annotations.
2. Improve compression profiles and report before/after file size.
3. Add favorites, reading history and saved page position.
4. Add image export and richer OCR results.
5. Finish accessibility review, physical-device testing and permanent production signing.
