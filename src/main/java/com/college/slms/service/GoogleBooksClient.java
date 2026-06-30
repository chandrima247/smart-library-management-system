package com.college.slms.service;

import com.college.slms.config.SlmsProperties;
import com.college.slms.service.dto.BookMetadata;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.util.StringJoiner;

/**
 * Fetches book metadata from the Google Books API by ISBN.
 *
 * <p>The lookup is best-effort: any failure (network error, timeout, missing
 * volume, disabled integration) results in an {@link BookMetadata#unresolved}
 * result so the caller can still create the catalogue record manually. This
 * satisfies the "fallback gracefully" requirement.</p>
 */
@Component
public class GoogleBooksClient {

    private static final Logger log = LoggerFactory.getLogger(GoogleBooksClient.class);
    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    private final WebClient webClient;
    private final SlmsProperties.GoogleBooks config;

    public GoogleBooksClient(WebClient.Builder builder, SlmsProperties properties) {
        this.config = properties.getGoogleBooks();
        this.webClient = builder.baseUrl(config.getBaseUrl()).build();
    }

    /**
     * Resolve metadata for the given ISBN. Never throws — returns an unresolved
     * result if anything goes wrong.
     */
    public BookMetadata lookupByIsbn(String isbn) {
        String normalized = normalize(isbn);
        if (!config.isEnabled() || normalized.isBlank()) {
            return BookMetadata.unresolved(normalized);
        }
        try {
            String uri = UriComponentsBuilder.fromPath("/volumes")
                    .queryParam("q", "isbn:" + normalized)
                    .queryParamIfPresent("key", java.util.Optional.ofNullable(
                            config.getApiKey() == null || config.getApiKey().isBlank() ? null : config.getApiKey()))
                    .build()
                    .toUriString();

            JsonNode root = webClient.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .timeout(TIMEOUT)
                    .block();

            if (root == null || root.path("totalItems").asInt(0) == 0) {
                log.info("Google Books returned no volume for ISBN {}", normalized);
                return BookMetadata.unresolved(normalized);
            }
            JsonNode info = root.path("items").get(0).path("volumeInfo");
            return map(normalized, info);
        } catch (Exception ex) {
            log.warn("Google Books lookup failed for ISBN {} ({}); falling back", normalized, ex.getMessage());
            return BookMetadata.unresolved(normalized);
        }
    }

    private BookMetadata map(String isbn, JsonNode info) {
        return new BookMetadata(
                isbn,
                text(info, "title"),
                joinAuthors(info.path("authors")),
                text(info, "publisher"),
                text(info, "publishedDate"),
                firstText(info.path("categories")),
                text(info, "language"),
                info.has("pageCount") ? info.get("pageCount").asInt() : null,
                coverUrl(info.path("imageLinks")),
                text(info, "description"),
                true
        );
    }

    private static String normalize(String isbn) {
        return isbn == null ? "" : isbn.replaceAll("[^0-9Xx]", "").trim();
    }

    private static String text(JsonNode node, String field) {
        JsonNode v = node.path(field);
        return v.isMissingNode() || v.isNull() ? null : v.asText();
    }

    private static String firstText(JsonNode array) {
        return array.isArray() && !array.isEmpty() ? array.get(0).asText() : null;
    }

    private static String joinAuthors(JsonNode authors) {
        if (!authors.isArray() || authors.isEmpty()) {
            return null;
        }
        StringJoiner joiner = new StringJoiner(", ");
        authors.forEach(a -> joiner.add(a.asText()));
        return joiner.toString();
    }

    private static String coverUrl(JsonNode imageLinks) {
        if (imageLinks.isMissingNode()) {
            return null;
        }
        for (String key : new String[]{"thumbnail", "smallThumbnail"}) {
            JsonNode link = imageLinks.path(key);
            if (!link.isMissingNode()) {
                // Prefer https to avoid mixed-content warnings.
                return link.asText().replaceFirst("^http://", "https://");
            }
        }
        return null;
    }
}
