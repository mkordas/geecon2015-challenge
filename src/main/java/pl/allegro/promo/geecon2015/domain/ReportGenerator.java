package pl.allegro.promo.geecon2015.domain;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.allegro.promo.geecon2015.domain.stats.FinancialStatisticsRepository;
import pl.allegro.promo.geecon2015.domain.transaction.TransactionRepository;
import pl.allegro.promo.geecon2015.domain.transaction.UserTransaction;
import pl.allegro.promo.geecon2015.domain.transaction.UserTransactions;
import pl.allegro.promo.geecon2015.domain.user.UserRepository;

@Component
public class ReportGenerator {

    private final FinancialStatisticsRepository financialStatisticsRepository;

    private final UserRepository userRepository;

    private final TransactionRepository transactionRepository;

    @Autowired
    public ReportGenerator(FinancialStatisticsRepository financialStatisticsRepository,
                           UserRepository userRepository,
                           TransactionRepository transactionRepository) {
        this.financialStatisticsRepository = financialStatisticsRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    public Report generate(ReportRequest request) {
        Report report = new Report();
        Stream<UUID> userIds = financialStatisticsRepository
            .listUsersWithMinimalIncome(request.getMinimalIncome(), request.getUsersToCheck())
            .getUserIds()
            .stream();

        userIds.forEach(uuid -> {
            String name = userRepository.detailsOf(uuid).getName();
            UserTransactions userTransactions = transactionRepository
                .transactionsOf(uuid);

            BigDecimal transactionsAmount = Optional.ofNullable(userTransactions).map(transactions ->
                    transactions
                        .getTransactions()
                        .stream()
                        .map(UserTransaction::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
            ).orElse(null);
            report.add(new ReportedUser(uuid, name, transactionsAmount));
        });

        return report;
    }

}
