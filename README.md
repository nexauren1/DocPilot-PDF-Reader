# DocPilot — PDF Reader & Tools

**DocPilot — PDF Reader & Tools** is a free Android PDF reader and document toolkit from Nexauren. Open documents in an in-app library, read and navigate long PDFs, then process and review a new copy using focused tools. The interface uses Portuguese, English, French, Spanish and German, with a consistent navy-and-teal visual system.

## Features

- PDF reader with long-document scrolling, page navigation, selectable-text search, saved position, bookmarks, zoom, a thumbnail strip and light/sepia/dark appearance. PDFs selected from content providers are copied into private cache as a seekable read source so providers that return non-seekable streams do not leave pages stuck loading; the original remains untouched.
- Library and built-in file selector for PDFs and images, with filename search, recent order, alphabetical sorting, multi-selection where supported and persistent favourites.
- PDF tools including merge, page-range split, page reordering, rotation, cropping, text watermarks on every page, page numbering, image-focused compression, image-to-PDF conversion, text export, metadata inspection, password protection, and visual text signatures. Tool categories include live counts, and processing panels show the actual number of selected source files/images and known PDF pages.
- Document scanning and batch OCR for multiple images in one export where the device supports the required services.
- Home-screen widget with localized shortcuts that open Organize, Convert, Security or Text & Analysis tools directly.
- Interface language options: Portuguese, English, French, Spanish and German.
- Optional account sign-in. Local reading and document tools do not require an account.
- No integrated ads or usage analytics in this release.

## Privacy

Selected PDFs and images are processed on the device. The app does not send document contents, extracted text, filenames or previews to DocPilot servers. The built-in selector lists previously opened documents without a fresh permission request. To index and browse the full device storage inside DocPilot, Android may require the app's all-files access permission; that permission is optional, but the selector cannot enumerate every folder without it. Optional sign-in is handled by the configured authentication provider.

Read the [Privacy Policy](https://nexauren1.github.io/DocPilot-PDF-Reader/privacy-policy.html).

## Build information

- Application ID: `com.nexauren.docpilot`
- Minimum Android version: Android 7.0 (API 24)
- Production APKs are built with GitHub Actions after unit tests, build and APK signature verification succeed.

Production signing is managed through protected repository settings. Do not commit a signing keystore, passwords, tokens, or other private credentials to source control.

## Known limitations

- Scanned or image-only PDFs do not provide text for reader search unless a text layer exists; image OCR is available for individual image files.
- Compression primarily optimizes embedded raster images, so text-heavy or vector-heavy files may show little size reduction.
- The visual text signature is not a certificate-backed digital signature.

## Release validation

Install the exact release APK on a physical Android device before store submission. Check short and very long PDFs, mixed page sizes, search, saved position, zoom, sign-in, permissions and every save/share flow. Passing CI does not replace real-device testing.
