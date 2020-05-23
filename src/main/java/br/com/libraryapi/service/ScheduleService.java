package br.com.libraryapi.service;

import br.com.libraryapi.model.Loan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScheduleService {

    private static final String CRON_LATE_LOANS = "0 0 0 1/1 * ?";

    @Value("${application.mail.lateLoans.message}")
    private String message;

    @Autowired
    private LoanService loanService;

    private EmailService emailService;

    @Scheduled(cron = CRON_LATE_LOANS)
    public void sendMailToLateLoans() {
        List<Loan> allLateLoans = loanService.getAllLateLoans();
        List<String> mailsList = allLateLoans.stream().map(loan -> loan.getCustomerEmail()).collect(Collectors.toList());

        emailService.sendMails(mailsList, message);
    }
}
