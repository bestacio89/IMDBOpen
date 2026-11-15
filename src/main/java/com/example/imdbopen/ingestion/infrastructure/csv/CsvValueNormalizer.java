package com.example.imdbopen.ingestion.infrastructure.csv;

final class CsvValueNormalizer {
    private CsvValueNormalizer() {
    }

    static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() || "N/A".equalsIgnoreCase(normalized) ? null : normalized;
    }
}
