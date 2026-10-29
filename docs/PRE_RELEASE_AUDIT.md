# DocPilot pre-release audit

## Candidate
DocPilot — PDF Reader & Tools 0.1.16 (versionCode 17), a proposed candidate subject to CI and real-device validation.

## Changes in this release candidate

- Added five selectable interface languages: Portuguese, English, French, Spanish and German.
- Removed the Analytics consent card and the Analytics SDK from the app.
- Removed internal authentication-provider wording from user-facing copy.
- Improved reader startup so page count and first-page layout become available before full page-geometry measurement finishes.
- Added a memory-bounded page bitmap cache, reduced the main-page render target width and added retry/error states instead of indefinite spinners when page rendering fails.
- Added a public multilingual privacy-policy page.
- Kept document processing on-device and account sign-in optional.
- Added category filters and live counts to the tools catalogue.
- Added explicit source-file/image and detected-page counters to processing panels.
- Added OCR for multiple selected images, combining recognized text into one reviewable TXT result.

## Existing capabilities

Reader: multi-page rendering, page navigation, selectable-text search, zoom/pan, saved position, bookmarks and light/sepia/dark appearance.

Tools: PDF merge/split/reorder/rotate/crop/watermark/page numbering, image-to-PDF conversion, image OCR, document scanning, PDF text export, metadata inspection, image-focused compression, password protection, page-image export and visual text signature.

Library: filename search, recent order, alphabetical sorting and persistent favourites.

## Required before store submission

- CI unit tests, debug build, production release build and APK signature verification must pass.
- Install the exact signed APK on a physical Android device.
- Test short and very long PDFs, scanned and password-protected files, mixed page sizes, thumbnails, search, saved position and page-render retries.
- Verify account creation, sign-in, sign-out and password reset on-device.
- Review storage access and ensure each save/share flow produces the expected file.
- Confirm the hosted privacy policy is publicly reachable before placing the URL in a store listing.
