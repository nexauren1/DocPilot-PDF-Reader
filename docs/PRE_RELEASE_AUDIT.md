# DocPilot pre-release audit

## Candidate
DocPilot — PDF Reader & Tools 0.1.18 (versionCode 19), a proposed candidate subject to CI and real-device validation.

## Changes in this release candidate

- Added five selectable interface languages: Portuguese, English, French, Spanish and German.
- Removed the Analytics consent card and the Analytics SDK from the app.
- Removed internal authentication-provider wording from user-facing copy.
- Improved reader startup so page count and first-page layout become available before full page-geometry measurement finishes.
- Added a memory-bounded page bitmap cache, reduced page render memory pressure and added bounded loading states, actionable timeout messages and retry controls. A renderer timeout no longer waits indefinitely on a second job join.
- Added a public multilingual privacy-policy page.
- Kept document processing on-device and account sign-in optional.
- Added category filters and live counts to the tools catalogue.
- Added explicit source-file/image and detected-page counters to processing panels.
- Added OCR for multiple selected images, combining recognized text into one reviewable TXT result.
- Added a localized Android home-screen widget with shortcuts that open a specific tool category.
- Rebuilt the Home screen with direct Open PDF and Tools actions, a separate device-library action, less decorative card treatment and more recent-document rows.
- Replaced file-selection activity launchers throughout PDF editing, security, text extraction, metadata, OCR and image-to-PDF workflows with an in-app searchable picker; the picker now navigates folders and selects local files without launching another file-manager app. Full-device browsing still requires Android's optional all-files access.
- Reworked navigation with a four-destination bottom bar, smoother transitions, larger library scroll area, and moved secondary reader actions into an overflow menu to keep the toolbar usable on narrow phones.
- Rebuilt the light/dark design system around indigo and teal accents, redesigned the launcher icon, upgraded the Home hero and Quick Action cards, and replaced the compact one-row tool list with a spacious two-column tool-card grid.
- Added a seekable, app-private cached source for PdfRenderer to accommodate content providers that supply non-seekable streams; page rendering now exposes retry/error feedback instead of only an endless spinner.

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
