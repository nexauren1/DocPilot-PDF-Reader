# DocPilot PDF Reader

DocPilot is an Android-first document hub focused on reading, organizing and processing PDFs.

## Current foundation

- Jetpack Compose + Material 3 interface
- Home dashboard with quick actions
- PDF import through the Android document picker
- Persistent local document library
- Search across imported documents
- First-page PDF rendering with Android `PdfRenderer`
- Tools hub for merge, split, compress, protect, organize, scanner, image-to-PDF, signature and OCR
- Settings screen
- Starter brand icon and theme
- JVM unit test
- GitHub Actions pipeline for tests and debug/release APK builds

## Architecture

The project starts intentionally small and modular:

- `model/` contains document domain models.
- `ui/` contains theme, navigation shell, reusable components and screens.
- PDF processing will be moved into dedicated processing modules as tools become functional.

## Build

The CI workflow uses JDK 17 and Gradle 9.6.0. The application targets Android API 36 with minimum Android API 24.

A release APK from CI is currently **unsigned**. Production signing will be configured later with protected credentials.

## Roadmap

1. Multi-page PDF reader with zoom, page navigation and thumbnails.
2. Real PDF processing engine for merge, split, reorder and compression.
3. Scanner/OCR workflow.
4. Image and document converters.
5. Favorites, folders, sorting and richer file management.
6. Release signing, automated versioning and store-ready build pipeline.
