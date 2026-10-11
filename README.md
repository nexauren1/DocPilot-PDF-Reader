# DocPilot

DocPilot is a free, Android-first PDF reader and document toolkit from Nexauren. Reading and the current document tools do not require a paid plan or an account. The project prioritizes reliable reading, predictable page layout, on-device processing, and clear save flows over adding tool buttons that do not work.

## Product principles

- **Free first:** the reader and current document tools stay available without a subscription.
- **Private by default:** PDF files are opened and processed on the device; document contents and filenames are not sent to DocPilot analytics.
- **Account optional:** Firebase Authentication supports email/password and Google sign-in from Settings, but reading does not require an account.
- **Analytics by consent:** Google Analytics collection is off until the user enables it in Settings.
- **Clear result flow:** choose a document, configure a tool, process a copy, review the result, and save it.

## Reader

- Multi-page PDF viewing powered by Android's `PdfRenderer`
- Text search for selectable PDFs, with matching words highlighted and underlined
- Horizontal page thumbnails, direct page selection, previous/next navigation, and jump-to-page
- Saved reading position, a page bookmark, reading progress, pinch-to-zoom, pan, and zoom reset
- Reflow reading mode for selectable text
- Page placeholders use each page's measured aspect ratio to reduce scroll jumps while rendering
- Scanned or image-only PDFs without a text layer cannot currently be searched in the reader. Image OCR is available for individual image files; whole-PDF OCR remains future work.

## Free document tools

- Merge multiple PDFs
- Extract an inclusive page range
- Reorder every page using a sequence such as `3,1,2,4`; the sequence must include each page exactly once. Use Split PDF when you need only a subset
- Rotate all pages 90 degrees clockwise
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

The app uses Android's system document picker as a private, per-file access option. Users may also grant wider storage access to discover PDFs throughout the device. Selected PDFs and images are not uploaded to a DocPilot server. Firebase is used for optional authentication, and analytics collection is limited to cases where the user explicitly opts in.

## Build and release

- JDK 17, Gradle 9.6.0, Android compile SDK 36, minimum Android API 24
- Application ID: `com.nexauren.docpilot`
- GitHub Actions runs JVM tests and builds the debug APK; it validates a production-signed release when signing secrets are present.

Every production release must use these four GitHub Actions secrets:

- `SIGNING_KEYSTORE_BASE64`
- `SIGNING_KEYSTORE_PASSWORD`
- `SIGNING_KEY_ALIAS`
- `SIGNING_KEY_PASSWORD`

The release workflow blocks publishing if the four secrets are missing or incomplete. Keep the persistent signing key and its passwords backed up privately. Never commit the keystore or expose secret values in source code, Issues, or chat.

## Candidates for a future iteration

After real-device validation, useful additions to evaluate include exporting selected pages as image files, per-page rotate/crop and visual page reordering, favourites and user-created folders, annotations/highlighting, more complete print/share controls, and an explicit reader appearance setting. These are roadmap candidates, not features advertised as available today. Reader accuracy and reliable saving take priority over adding more tools.
