# DocPilot - PDF Reader

DocPilot - PDF Reader is an Android-first document hub focused on reading, organizing and processing PDFs.

## Current features

- Jetpack Compose + Material 3 interface
- Home dashboard with inline document search, sidebar navigation with animated transitions, and a document library with local persistence, reliable recent-item order, search clearing and Recentes / Nome A–Z sorting
- Opening a selected PDF launches the reader immediately
- PDFs created by the processing tools are added to the library after a successful save
- Optional device-wide PDF discovery after a clear storage-access explanation and Android permission grant
- PDF opening through the Android document picker remains available as a privacy-friendly fallback
- Multi-page PDF reader with on-demand Android PdfRenderer rendering
- Live inline search in text-based PDFs as the user types, page jump, page bookmarks, saved reading position and a live reading progress indicator, pinch-to-zoom and a reflow reading mode for selectable text
- Horizontal page thumbnails, direct page selection and previous/next navigation
- Pinch-to-zoom and pan with one-tap zoom reset
- Local PDF processing powered by PdfBox-Android 2.0.27.0
- **Merge PDFs**, **split page ranges**, **reorder pages** and **image compression**
- Scanner flow using Google ML Kit Document Scanner
- OCR for images using ML Kit Text Recognition
- Convert selected images into PDF
- Password-protect a PDF
- Add a visual text signature to PDF pages
- Settings screen, starter app icon and Material 3 theme
- JVM unit tests and GitHub Actions test/build pipeline

## PDF processing and privacy

Processing happens on-device. The app uses the Android system document picker for input and output files; selected PDFs and images are not uploaded to a DocPilot - PDF Reader server.

Current tools include:

- **Merge PDFs:** combines pages from two or more source PDFs.
- **Rotate pages:** rotates every page 90° clockwise and saves a separate PDF.
- **PDF to text:** exports selectable text from a PDF to a UTF-8 TXT file; scanned PDFs need OCR first.
- **Split PDF:** extracts an inclusive page interval.
- **Rotate pages:** rotates every page clockwise by 90 degrees and writes a separate PDF.
- **PDF text export:** writes selectable text to a UTF-8 TXT file; scanned pages require OCR.
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

The 0.1.7 release workflow is prepared for a persistent production keystore. Until all four signing secrets are configured, CI builds can use the debug identity for testing, but GitHub Release publication is skipped. Do not distribute debug-signed APKs as production releases. Keep the same private signing key for every future update.

Required GitHub repository secrets (Settings → Secrets and variables → Actions):
- `SIGNING_KEYSTORE_BASE64`: the release keystore encoded as a single base64 string.
- `SIGNING_KEYSTORE_PASSWORD`: password protecting the keystore.
- `SIGNING_KEY_ALIAS`: alias of the signing key inside the keystore.
- `SIGNING_KEY_PASSWORD`: password for that key.

Create the keystore once on a trusted device with a JDK (Android users can use a trusted Termux environment with a JDK installed):

```sh
keytool -genkeypair -v -keystore docpilot-release.jks -alias docpilot-release -keyalg RSA -keysize 2048 -validity 10000
base64 -w 0 docpilot-release.jks > docpilot-release.base64
```

Use alias `docpilot-release` for `SIGNING_KEY_ALIAS`. Put the full contents of `docpilot-release.base64` into `SIGNING_KEYSTORE_BASE64`; use the passwords you entered when generating the key for the two password secrets. In GitHub, open this repository's **Settings → Secrets and variables → Actions → New repository secret** and create each secret with the exact name above. Keep both the original `.jks` file and its passwords backed up privately, and never commit either file.

The release workflow decodes the keystore into the temporary runner directory and never commits it to the repository. Configure all four secrets together, then run **DocPilot - PDF Reader | Release** from Actions to publish the signed release. Never paste the keystore, passwords or secret values into source files, Issues or chat. If the keystore is lost, future APKs signed with a different key cannot update the current installation in-place.

## Roadmap

1. Finish the first persistent-key signed release after adding the four protected GitHub secrets.
2. Better compression settings and size reporting.
3. Richer document organization, favorites, folders and history.
4. Scanner/OCR workflow refinements.
5. Additional accessibility, device testing and release checks.
