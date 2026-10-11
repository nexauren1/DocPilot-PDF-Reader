# DocPilot pre-release audit

## Scope

Source-level review and first release-polish pass for DocPilot Free. Candidate version: 0.1.11, versionCode 12.

## Changes in this pass

- PDF pages use their measured page dimensions before a rendered bitmap appears. This should remove vertical jumps caused by replacing a fixed-height loading placeholder with a page of another size.
- Saved reading position is restored before the current page can overwrite it.
- Search stores text-position bounds and paints a translucent yellow highlight with an underline over matching words.
- The Tools landing page has a clearer blue/violet hero and an accurate tool count.
- Added a local PDF information tool for page count, file size, first-page dimensions, an encryption indicator, and available metadata.
- Replaced the misleading “more” icon that actually opened files with a clear open-document arrow.
- Made the main app label “DocPilot” and strengthened typography hierarchy.
- Replaced stale and duplicated README entries with a clearer current feature list and known limitations.

## Current feature inventory

Reader: multi-page rendering, thumbnails, page navigation, page bookmark, saved position, progress, text search, pinch-to-zoom/pan/reset, reflow reading mode.

Free tools: merge PDF, split page range, reorder pages, rotate all pages, image-oriented compression, images-to-PDF, scanner, image OCR, PDF text export, PDF metadata inspection, password protection, visual text signature.

Account/privacy: optional Firebase email/password and Google sign-in; Analytics collection defaults off and is enabled only by explicit user choice. PDF content is processed locally.

## Must validate before store submission

- Confirm highlight alignment on portrait, landscape, cropped, rotated, and multi-column PDFs. Text geometry varies by document, so device tests are required.
- Test mixed page sizes and very large PDFs for open latency, scroll stability, memory use, thumbnail scrolling, and zoom.
- Verify saved reading position when reopening long PDFs and switching between documents.
- Run unit tests and CI builds; inspect the production signature and install the signed APK on a physical Android device.
- Test Google sign-in after confirming that correct SHA-1 and SHA-256 fingerprints are registered in Firebase. Source review cannot prove OAuth end-to-end.
- Test account create/sign-in/password reset/sign-out and Analytics opt-in/opt-out on-device.
- Test scanner availability with and without compatible Google Play services.
- Test password-protected, malformed, rotated, vector-heavy, and image-heavy PDFs, including compression and metadata behavior.
- Test save/share flows for each result type and verify output filenames.
- Review broad storage permission and each distribution channel's requirements while preserving the system document picker flow.

## Research-backed future candidates

Official Jetpack PDF APIs remain in alpha as of this research pass, so the current renderer is retained while reader behavior is stabilized. Feature ideas from established mobile readers include:

1. Export selected pages as image files.
2. Page-specific rotation/crop and drag-to-reorder thumbnails.
3. Favourites and user-created folders.
4. Annotation and highlighter tools.
5. Print/share polish, night/page-appearance settings, and OCR for multipage scanned PDFs.

These are candidates, not implemented features. Reader accuracy and reliable saving take priority over expanding the catalogue.
