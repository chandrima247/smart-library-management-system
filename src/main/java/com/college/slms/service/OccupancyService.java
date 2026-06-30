package com.college.slms.service;

import com.college.slms.config.SlmsProperties;
import com.college.slms.domain.OccupancySession;
import com.college.slms.domain.User;
import com.college.slms.exception.BusinessRuleException;
import com.college.slms.repository.OccupancySessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Manages reading-hall occupancy: arrival check-in, check-out, the list of
 * current readers and live capacity figures.
 */
@Service
public class OccupancyService {

    private final OccupancySessionRepository sessionRepository;
    private final SlmsProperties.Library library;

    public OccupancyService(OccupancySessionRepository sessionRepository, SlmsProperties properties) {
        this.sessionRepository = sessionRepository;
        this.library = properties.getLibrary();
    }

    @Transactional
    public OccupancySession checkIn(User student) {
        sessionRepository.findFirstByStudentIdAndCheckOutAtIsNull(student.getId())
                .ifPresent(s -> {
                    throw new BusinessRuleException("You are already checked in to the library.");
                });
        if (currentOccupancy() >= library.getReadingHallCapacity()) {
            throw new BusinessRuleException("The reading hall is at full capacity. Please try again shortly.");
        }
        return sessionRepository.save(new OccupancySession(student, library.getName()));
    }

    @Transactional
    public void checkOut(User student) {
        OccupancySession session = sessionRepository
                .findFirstByStudentIdAndCheckOutAtIsNull(student.getId())
                .orElseThrow(() -> new BusinessRuleException("You are not currently checked in."));
        session.checkOut();
    }

    @Transactional(readOnly = true)
    public boolean isCheckedIn(Long studentId) {
        return sessionRepository.findFirstByStudentIdAndCheckOutAtIsNull(studentId).isPresent();
    }

    @Transactional(readOnly = true)
    public List<OccupancySession> currentReaders() {
        return sessionRepository.findByCheckOutAtIsNullOrderByCheckInAtDesc();
    }

    @Transactional(readOnly = true)
    public long currentOccupancy() {
        return sessionRepository.countByCheckOutAtIsNull();
    }

    public int capacity() {
        return library.getReadingHallCapacity();
    }

    public String hallName() {
        return library.getName();
    }

    public long availableSeats() {
        return Math.max(0, capacity() - currentOccupancy());
    }
}
