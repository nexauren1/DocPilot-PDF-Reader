# DocPilot

DocPilot is a free, Android-first PDF reader and document toolkit from Nexauren. Reading and the current document tools do not require a paid plan or an account. The project prioritizes reliable reading, predictable page layout, on-device processing, and clear save flows over adding tool buttons that do not work.

## Product principles

- **Free first:** the reader and current document tools stay available without a subscription.
- **Private by default:** PDF files are opened and processed on the device; document contents and filenames are not uploaded to a DocPilot server.
- **Account optional:** Optional account sign-in supports email/password and Google sign-in from Settings, but reading does not require an account.
- **Five interface languages:** Portuguese, English, French, Spanish, and German are available for translated interface labels.
- **Clear result flow:** choose a document, configure a tool, process a copy, review the result, and save it.

## Reader

- Multi-page PDF viewing powered by Android's `PdfRenderer`
- Text search for selectable PDFs, with matching words highlighted and underlined
- Horizontal page thumbnails, direct page selection, previous/next navigation, and jump-to-page
- Saved reading position, a page bookmark, reading progress, pinch-to-zoom, pan, and zoom reset
- Reflow reading mode for selectable text
- Persistent light, sepia, and dark reading appearances
- Page placeholders use each page's measured aspect ratio to reduce scroll jumps while rendering
- Scanned or image-only PDFs without a text layer cannot currently be searched in the reader. Image OCR is available for individual image files; whole-PDF OCR remains future work.

## Library

- Search PDFs by filename and sort by recent activity or name
- Persistent favourites stored locally on the device, with a one-tap favourites filter
- Opening a document brings it to the top of the recent list

## Free document tools

- Merge multiple PDFs
- Extract an inclusive page range
- Reorder every page using a sequence such as `3,1,2,4`; the sequence must include each page exactly once. Use Split PDF when you need only a subset
- Rotate all pages 90 degrees clockwise
- Rotate one selected page by 90°, 180°, or 270°
- Crop the visible area of one selected page using adjustable margins
- Export a selected page as a PNG image with preview and a save dialog
- Compress PDFs by downscaling oversized embedded raster images
- Convert multiple images to a PDF with paper-size and orientation options
- Scan documents using Google ML Kit Document Scanner
- Recognize text in images with ML Kit OCR
- Export selectable PDF text to TXT, with an option to remove blank lines
- Inspect PDF page count, file size, first-page dimensions, encryption indicator, and available metadata
- Protect a PDF with a password
- Add a visual text signature to pages

The visual signature is not a certificate-backed digital signature. Compression primarily optimizes embedded images, so PDFs dominated by text or vector content may show little size reduction.

## Privacy and storage

The public privacy policy is published from [`docs/privacy-policy.html`](docs/privacy-policy.html) and is linked from the app's Settings screen. After enabling GitHub Pages with the Actions source, the public URL is `https://nexauren1.github.io/DocPilot-PDF-Reader/privacy-policy.html`.

The app uses Android's system document picker as a per-file access option. Users may also grant wider storage access to discover PDFs throughout the device. Selected PDFs and images are processed locally by DocPilot and are not uploaded to a DocPilot document server. Optional account sign-in is handled by the authentication provider. Usage analytics are not included.

## Build and release

- JDK 17, Gradle 9.6.0, Android compile SDK 36, minimum Android API 24
- Application ID: `com.nexauren.docpilot`
- GitHub Actions runs JVM tests and builds the debug APK; it validates a production-signed release when signing secrets are present.

Production builds are signed in the automated release pipeline using protected repository configuration. Signing material is kept outside the source code and must never be committed or shared publicly.

## Candidates for a future iteration

After real-device validation, useful additions to evaluate include persistent highlights and underlines saved into the PDF, sticky-note annotations, fillable AcroForm fields and safe export of completed forms, visual page drag-and-drop ordering, user-created folders, and print/share improvements. These are roadmap candidates, not features advertised as available today. Reader accuracy and reliable saving take priority over adding more tools.
