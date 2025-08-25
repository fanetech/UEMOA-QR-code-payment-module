# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.0.0] - 2024-01-XX

### Added
- Initial release of UEMOA QR Code Payment Module
- Support for static QR code generation (merchant payments)
- Support for dynamic QR code generation (specific transactions)
- Support for P2P QR code generation (person-to-person transfers)
- EMVCo compliant QR code parsing and validation
- CRC16-CCITT checksum calculation and validation
- Support for all WAEMU/UEMOA countries (BF, CI, TG, SN, ML, BJ, GW, NE)
- QR code image generation (PNG/JPG format)
- Spring Boot auto-configuration
- Comprehensive unit tests
- Full documentation and examples

### Technical Features
- Java 17 compatibility
- Spring Boot 3.1.5 integration
- Lombok for cleaner code
- ZXing library for QR code generation
- Jakarta Validation for input validation
- Maven Central deployment ready

### Supported Merchant Channels
- Static onsite (100)
- Static with amount (110)
- Static with transaction ID (120)
- Static invoice (131)
- Dynamic onsite (500)
- Dynamic e-commerce web (521)
- Dynamic e-commerce app (522)
- P2P static (731)

## [0.9.0-BETA] - Internal Testing

### Added
- Beta version for internal testing
- Core QR generation functionality
- Basic parsing capabilities

---

## Version Numbering

We use Semantic Versioning:
- MAJOR version for incompatible API changes
- MINOR version for backwards-compatible functionality additions
- PATCH version for backwards-compatible bug fixes

## Support

For questions and support, please contact: support@aveplus.com
