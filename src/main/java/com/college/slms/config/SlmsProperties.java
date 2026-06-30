package com.college.slms.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

/**
 * Strongly-typed binding for the {@code slms.*} application settings declared in
 * {@code application.properties}. Centralising business rules here keeps magic
 * numbers out of the service layer and makes them configurable per environment.
 */
@ConfigurationProperties(prefix = "slms")
public class SlmsProperties {

    private final Circulation circulation = new Circulation();
    private final Library library = new Library();
    private final GoogleBooks googleBooks = new GoogleBooks();

    public Circulation getCirculation() {
        return circulation;
    }

    public Library getLibrary() {
        return library;
    }

    public GoogleBooks getGoogleBooks() {
        return googleBooks;
    }

    /** Loan / fine rules. */
    public static class Circulation {
        /** Loan period (in days) for a HOME borrow. */
        private int homeLoanDays = 14;
        /** Maximum number of concurrent active loans per student. */
        private int maxActiveLoans = 5;
        /** Fine charged per overdue day. */
        private BigDecimal finePerDay = new BigDecimal("2.00");

        public int getHomeLoanDays() {
            return homeLoanDays;
        }

        public void setHomeLoanDays(int homeLoanDays) {
            this.homeLoanDays = homeLoanDays;
        }

        public int getMaxActiveLoans() {
            return maxActiveLoans;
        }

        public void setMaxActiveLoans(int maxActiveLoans) {
            this.maxActiveLoans = maxActiveLoans;
        }

        public BigDecimal getFinePerDay() {
            return finePerDay;
        }

        public void setFinePerDay(BigDecimal finePerDay) {
            this.finePerDay = finePerDay;
        }
    }

    /** Physical library / occupancy settings. */
    public static class Library {
        private int readingHallCapacity = 100;
        private String name = "Main Reading Hall";

        public int getReadingHallCapacity() {
            return readingHallCapacity;
        }

        public void setReadingHallCapacity(int readingHallCapacity) {
            this.readingHallCapacity = readingHallCapacity;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    /** Google Books metadata lookup settings. */
    public static class GoogleBooks {
        private String baseUrl = "https://www.googleapis.com/books/v1";
        private boolean enabled = true;
        private String apiKey = "";

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }
    }
}
