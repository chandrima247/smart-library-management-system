package com.college.slms.domain;

import com.college.slms.domain.enums.CopyStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * A single physical, lendable item of a {@link Book}, identified by a unique
 * accession number. Barcode is optional (nullable) per the specification.
 */
@Entity
@Table(name = "book_copies", indexes = {
        @Index(name = "ux_copy_accession", columnList = "accession_number", unique = true),
        @Index(name = "ix_copy_barcode", columnList = "barcode"),
        @Index(name = "ix_copy_status", columnList = "status")
})
public class BookCopy extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    /** Library-assigned accession number — identifies this specific copy. */
    @Column(name = "accession_number", nullable = false, unique = true, length = 40)
    private String accessionNumber;

    /** Optional barcode; may be null when the copy is not yet barcoded. */
    @Column(length = 60)
    private String barcode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CopyStatus status = CopyStatus.AVAILABLE;

    /** Whether the copy is restricted to in-library reading only (reference). */
    @Column(name = "reference_only", nullable = false)
    private boolean referenceOnly = false;

    @Column(length = 60)
    private String condition;

    protected BookCopy() {
        // for JPA
    }

    public BookCopy(Book book, String accessionNumber) {
        this.book = book;
        this.accessionNumber = accessionNumber;
    }

    public boolean isAvailable() {
        return status == CopyStatus.AVAILABLE;
    }

    // --- getters / setters ---

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public String getAccessionNumber() {
        return accessionNumber;
    }

    public void setAccessionNumber(String accessionNumber) {
        this.accessionNumber = accessionNumber;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public CopyStatus getStatus() {
        return status;
    }

    public void setStatus(CopyStatus status) {
        this.status = status;
    }

    public boolean isReferenceOnly() {
        return referenceOnly;
    }

    public void setReferenceOnly(boolean referenceOnly) {
        this.referenceOnly = referenceOnly;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }
}
