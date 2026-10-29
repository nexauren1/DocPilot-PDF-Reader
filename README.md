# PDF Reader & Tools

PDF Reader & Tools is a free Android PDF reader and document toolkit from Nexauren. It is designed around a simple flow: open a document, read or process it on the device, review the result, and save a copy.

## Features

Release candidate: **PDF Reader & Tools 0.1.16**. This update adds page-range removal, tool-category filters, session operation success/failure counters and page-count feedback during processing.

- PDF reading with page navigation, selectable-text search, saved reading position, bookmarks, zoom and light/sepia/dark appearance.
- Library with filename search, recent order, alphabetical sorting and persistent favourites.
- PDF tools including merge, page-range split and removal, page reordering, rotation, cropping, text watermarks on every page, page numbering, image-focused compression, image-to-PDF conversion, text export, metadata inspection, password protection, and visual text signatures.
- Document scanning and image OCR where the device supports the required services.
- Interface language options: Portuguese, English, French, Spanish and German.
- Optional account sign-in. Local reading and document tools do not require an account.
- No integrated ads or usage analytics in this release.

## Privacy

Selected PDFs and images are processed on the device. The app does not send document contents, extracted text, filenames or previews to PDF Reader & Tools servers. Android's document picker allows per-file access; broader storage access is optional for automatic PDF discovery. Optional sign-in is handled by the configured authentication provider.

Read the [Privacy Policy](https://nexauren1.github.io/DocPilot-PDF-Reader/privacy-policy.html).

## Build information

- Application ID: `com.nexauren.docpilot`
- Minimum Android version: Android 7.0 (API 24)
- Production APKs are built with GitHub Actions after unit tests, build and APK signature verification succeed. The launcher display name is PDF Reader & Tools; the internal application ID remains `com.nexauren.docpilot` to preserve compatibility with the existing Firebase configuration and installed builds.

Production signing is managed through protected repository settings. Do not commit a signing keystore, passwords, tokens, or other private credentials to source control.

## Known limitations

- Scanned or image-only PDFs do not provide text for reader search unless a text layer exists; image OCR is available for individual image files.
- Compression primarily optimizes embedded raster images, so text-heavy or vector-heavy files may show little size reduction.
- The visual text signature is not a certificate-backed digital signature.

## Release validation

Install the exact release APK on a physical Android device before store submission. Check short and very long PDFs, mixed page sizes, search, saved position, zoom, sign-in, permissions and every save/share flow. Passing CI does not replace real-device testing.
