# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.1.0] - 2026-05-10

First public release.

### Added
- `BitMapper(int sizeHint)` — allocates a new bit-packed array; rounds up to the next multiple of 8 bits and rejects negative or overflowing values with `IllegalArgumentException`.
- `BitMapper(byte[] data)` — zero-copy wrap of an existing byte array.
- `getBit(int)`, `setBit(int, boolean)` — bounds-checked O(1) bit access.
- `clear()`, `setAll(boolean)` — bulk fill operations.
- `getSize()`, `getData()` — size in bits and live access to the backing array.
- `equals`, `hashCode`, `toString` — content-equality semantics; `toString` truncates after 32 bytes for large maps.
- `Serializable` with `serialVersionUID = 1L` and a documented serial form.
- Documented bit ordering (LSB-first within each byte) and thread-safety guarantees (none).
- Maven Central publication under group ID `com.jucius`.
- 100% line and branch coverage gate enforced via JaCoCo.
- GitHub Actions CI on push and pull request, plus a manual-dispatch publish workflow.

[Unreleased]: https://github.com/mjucius/bitmapper-core/compare/v1.1.0...HEAD
[1.1.0]: https://github.com/mjucius/bitmapper-core/releases/tag/v1.1.0
