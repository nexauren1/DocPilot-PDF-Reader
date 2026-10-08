# DocPilot PDF Reader

DocPilot is an Android-first document hub focused on reading, organizing and processing PDFs.

## Current foundation

- Jetpack Compose + Material 3 interface
- Home dashboard with quick actions
- PDF import through the Android document picker
- Persistent local document library
- Search across imported documents
- Multi-page PDF reader with on-demand Android PdfRenderer rendering
- Horizontal page thumbnails with direct page selection
- Previous/next page navigation and live page counter
- Pinch-to-zoom and pan with a one-tap zoom reset
- Local PDF processing powered by PdfBox-Android 2.0.27.0
- Functional **merge**, **split**, **reorder** and **compress** workflows
- Tools workspace with output-file creation through the Android document picker
- Settings screen
- Starter brand icon and theme
- JVM unit tests for document and PDF-tool logic
- GitHub Actions pipeline for tests and debug/release APK builds

## CI validation

This branch runs the full Android unit-test, debug-APK and release-APK pipeline before the next promotion.

## PDF processing

The first functional processing layer runs on-device:

- **Juntar PDFs:** imports pages from two or more source PDFs into one output.
- **Dividir PDF:** extracts an inclusive page range into a new output.
- **Organizar:** creates a new PDF using a user-defined page order such as `3,1,2,4`.
- **Comprimir:** downscales oversized raster images and recompresses them before saving.

The UI creates the destination file with the Android system document picker. Source PDFs remain local to the device.

## Architecture

The project starts intentionally small and modular:

- `model/` contains document domain models.
- `pdf/` contains the local PDF processing engine.
- `ui/` contains theme, navigation shell, reusable components and screens.
- More tools can be added without coupling UI code directly to PDF processing internals.

## Build

The CI workflow uses JDK 17 and Gradle 9.6.0. The application targets Android API 36 with minimum Android API 24.

The release APK from CI is currently **unsigned**. Production signing will be configured later with protected credentials.

## Roadmap

1. Multi-page PDF reader with zoom, page navigation and thumbnails.
2. Stronger compression controls and size estimation.
3. Scanner/OCR workflow.
4. Image and document converters.
5. Favorites, folders, sorting and richer file management.
6. PDF protection and signing.
7. Release signing, automated versioning and store-ready build pipeline.
