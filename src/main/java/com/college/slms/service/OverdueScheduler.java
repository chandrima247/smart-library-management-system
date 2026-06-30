package com.college.slms.service;

import com.college.slms.domain.Loan;
import com.college.slms.domain.enums.LoanStatus;
import com.college.slms.domain.enums.NotificationType;
import com.college.slms.repository.LoanRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Daily maintenance job: flags overdue loans and sends due-date reminders. Runs
 * shortly after midnight and is also safe to trigger manually in tests.
 */
@Component
public class OverdueScheduler {

    private static final Logger log = LoggerFactory.getLogger(OverdueScheduler.class);

    private final LoanRepository loanRepository;
    private final NotificationService notificationService;

    public OverdueScheduler(LoanRepository loanRepository, NotificationService notificationService) {
        this.loanRepository = loanRepository;
        this.notificationService = notificationService;
    }

    /** Every day at 00:05 server time. */
    @Scheduled(cron = "0 5 0 * * *")
    @Transactional
    public void run() {
        markOverdue();
        sendDueReminders();
    }

    int markOverdue() {
        LocalDate today = LocalDate.now();
        List<Loan> overdue = loanRepository.findOverdueAsOf(today);
        for (Loan loan : overdue) {
            loan.setStatus(LoanStatus.OVERDUE);
            notificationService.notify(loan.getStudent(), NotificationType.DUE_REMINDER,
                    "Book overdue",
                    "\"%s\" was due on %s and is now overdue. Please return it to avoid further fines."
                            .formatted(loan.getCopy().getBook().getTitle(), loan.getDueDate()),
                    "/student/history");
        }
        if (!overdue.isEmpty()) {
            log.info("Marked {} loan(s) overdue", overdue.size());
        }
        return overdue.size();
    }

    int sendDueReminders() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        List<Loan> dueSoon = loanRepository.findDueOn(tomorrow);
        for (Loan loan : dueSoon) {
            notificationService.notify(loan.getStudent(), NotificationType.DUE_REMINDER,
                    "Due tomorrow",
                    "Reminder: \"%s\" is due tomorrow (%s)."
                            .formatted(loan.getCopy().getBook().getTitle(), loan.getDueDate()),
                    "/student/history");
        }
        return dueSoon.size();
    }
}
