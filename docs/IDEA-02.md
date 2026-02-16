# IDEA-02: Receipt Creation from Photo or PDF

Date: 2026-02-16
Related Trello card: `IDEA-02 - receipt import from photo/pdf` (to be created)

## Goal

Allow users to create invoice/receipt data in the app by importing a receipt image or PDF and extracting structured fields automatically.

## Scope (MVP)

In scope:
- Import input from camera photo, gallery image, and PDF file.
- Extract raw text from image/PDF.
- Parse text into structured receipt fields (best effort).
- Pre-fill existing invoice creation form with extracted values.
- Provide mandatory review/edit step before final save.

Out of scope (MVP):
- Fully automated save without user confirmation.
- Perfect parsing for all receipt layouts.
- OCR model training/custom ML pipeline.
- Full accounting/ERP reconciliation.

## Functional Requirements

1. User can choose import source: camera, gallery, or PDF.
2. App extracts available text content from the document.
3. App tries to detect key fields:
   - shop name,
   - purchase date,
   - document number,
   - line items (name, quantity, unit price, line total),
   - totals (gross/net/tax when available).
4. App pre-fills invoice draft with extracted data.
5. User can review and correct all fields before save.
6. If extraction quality is low, app still allows manual completion without data loss.

## Proposed Technical Design

### 1) Input Acquisition

- Camera capture flow (runtime permissions, image URI handling).
- Gallery image picker.
- PDF file picker via storage access framework.

### 2) Document Processing

- Image path:
  - image normalization (rotation, crop support, contrast improvements),
  - OCR extraction.
- PDF path:
  - direct text extraction for digital PDFs,
  - OCR fallback per page for scanned PDFs.

### 3) Parsing and Field Mapping

- Rule-based parser converts raw text to domain fields.
- Field confidence scoring (`HIGH/MEDIUM/LOW`) per extracted value.
- Mapping layer converts parsed output into current invoice models:
  - `Invoice`,
  - `InvoiceItem`.

### 4) Review and Correction UI

- New confirmation screen with:
  - source preview,
  - extracted values,
  - highlighted low-confidence fields,
  - manual edit before final save.

### 5) Error Handling

- Distinguish failures: input error, OCR error, parser error.
- Show user-friendly recovery actions:
  - retake photo,
  - choose another file,
  - continue with manual form entry.

## Architecture Decision Points

1. OCR location:
   - On-device OCR: better privacy, lower running cost, possibly lower quality on difficult inputs.
   - Backend/cloud OCR: often better quality, higher integration and privacy/compliance requirements.
2. Parser strategy:
   - Start with generic rule-based parser,
   - add store-specific parser rules iteratively based on real receipts.
3. Privacy and retention:
   - Define whether document files are stored temporarily or discarded immediately after extraction.

## Risks and Mitigations

1. Highly variable receipt formats reduce parser accuracy.
   - Mitigation: confidence-based UX and mandatory user review.
2. Poor photo quality degrades OCR.
   - Mitigation: capture guidance + preprocessing + retry flow.
3. Duplicate documents imported multiple times.
   - Mitigation: optional duplicate detection using shop/date/total/hash heuristics.
4. Privacy concerns around receipt content.
   - Mitigation: explicit data handling policy and minimal retention.

## Test Strategy

### Unit Tests
- Parser extraction rules from representative sample texts.
- Mapping tests from parsed structure to `Invoice` and `InvoiceItem`.
- Confidence scoring behavior.

### Integration Tests
- End-to-end pipeline from OCR text payload to pre-filled invoice draft.
- PDF text extraction + OCR fallback behavior.

### UI Tests (androidTest)
- User imports image/PDF and reaches review screen.
- Low-confidence fields are visible and editable.
- Corrected draft can be saved successfully.

## Rollout Plan

1. Implement import entry points (camera/gallery/PDF).
2. Add OCR + PDF text extraction layer.
3. Implement parser and model mapping.
4. Add review/correction UI.
5. Validate on real sample receipts and improve rules.

## Acceptance Criteria

- User can import receipt from image or PDF.
- App extracts and pre-fills at least core fields (shop/date/total) for common receipt formats.
- User can always correct extracted data before save.
- Failures are recoverable and do not block manual entry.
- Build and tests pass.

## Recommended MVP Boundary

To reduce risk, first release should include:
1. Camera + PDF import support.
2. OCR/text extraction.
3. Core field extraction: shop, date, total, basic items.
4. Mandatory review/edit screen.

Then extend accuracy and automation in later iterations.
