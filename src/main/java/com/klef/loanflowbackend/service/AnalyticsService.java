package com.klef.loanflowbackend.service;

import com.klef.loanflowbackend.dto.AnalyticsDTO;
import com.klef.loanflowbackend.entity.*;
import com.klef.loanflowbackend.repository.*;
import org.springframework.stereotype.Service;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final RiskReportRepository riskReportRepository;
    private final LoanRepository loanRepository;
    private final EmiScheduleRepository emiScheduleRepository;
    private final PaymentRepository paymentRepository;

    public AnalyticsService(RiskReportRepository riskReportRepository,
                           LoanRepository loanRepository,
                           EmiScheduleRepository emiScheduleRepository,
                           PaymentRepository paymentRepository) {
        this.riskReportRepository = riskReportRepository;
        this.loanRepository = loanRepository;
        this.emiScheduleRepository = emiScheduleRepository;
        this.paymentRepository = paymentRepository;
    }

    /**
     * Get risk score distribution by bands (0-20, 21-40, 41-60, 61-80, 81-100)
     */
    public List<AnalyticsDTO.RiskBand> getRiskScoreDistribution() {
        List<RiskReport> allReports = riskReportRepository.findAll();

        long band1 = allReports.stream().filter(r -> r.getRiskScore() <= 20).count();
        long band2 = allReports.stream().filter(r -> r.getRiskScore() > 20 && r.getRiskScore() <= 40).count();
        long band3 = allReports.stream().filter(r -> r.getRiskScore() > 40 && r.getRiskScore() <= 60).count();
        long band4 = allReports.stream().filter(r -> r.getRiskScore() > 60 && r.getRiskScore() <= 80).count();
        long band5 = allReports.stream().filter(r -> r.getRiskScore() > 80).count();

        return Arrays.asList(
            new AnalyticsDTO.RiskBand("0–20", (int) band1),
            new AnalyticsDTO.RiskBand("21–40", (int) band2),
            new AnalyticsDTO.RiskBand("41–60", (int) band3),
            new AnalyticsDTO.RiskBand("61–80", (int) band4),
            new AnalyticsDTO.RiskBand("81–100", (int) band5)
        );
    }

    /**
     * Get portfolio exposure by loan amount across all loans
     */
    public List<AnalyticsDTO.PortfolioExposure> getPortfolioExposure() {
        List<Loan> allLoans = loanRepository.findAll();

        // Group by status for now, can be extended to purpose if needed
        Map<String, Long> exposureMap = new HashMap<>();
        allLoans.forEach(loan -> {
            String status = loan.getStatus().toString();
            long loanAmount = Math.round(loan.getAmount());
            long currentValue = exposureMap.getOrDefault(status, 0L);
            exposureMap.put(status, currentValue + loanAmount);
        });

        return exposureMap.entrySet().stream()
            .map(e -> new AnalyticsDTO.PortfolioExposure(e.getKey(), e.getValue()))
            .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
            .collect(Collectors.toList());
    }

    /**
     * Get quarterly loan trends data
     */
    public List<AnalyticsDTO.QuarterlyTrend> getQuarterlyTrends() {
        List<Loan> allLoans = loanRepository.findAll();

        // Create quarterly data structure (mock actual quarters based on loan creation)
        Map<String, AnalyticsDTO.QuarterlyTrend> trends = new LinkedHashMap<>();

        // Define 8 quarters starting from Q1 2024
        String[] quarters = {
            "Q1 2024", "Q2 2024", "Q3 2024", "Q4 2024",
            "Q1 2025", "Q2 2025", "Q3 2025", "Q4 2025"
        };

        for (String quarter : quarters) {
            trends.put(quarter, new AnalyticsDTO.QuarterlyTrend(
                quarter,
                0,  // loans count
                0L  // disbursed volume
            ));
        }

        // Distribute loans across quarters (simplified: each loan increments starting quarter)
        int loansPerQuarter = Math.max(1, allLoans.size() / 8);
        long totalVolume = Math.round(allLoans.stream().mapToDouble(Loan::getAmount).sum());
        long volumePerQuarter = totalVolume / 8;

        int loanIdx = 0;
        for (String quarter : quarters) {
            AnalyticsDTO.QuarterlyTrend trend = trends.get(quarter);
            trend.setLoans(loansPerQuarter + (loanIdx == quarters.length - 1 ? allLoans.size() % 8 : 0));
            trend.setVolume(volumePerQuarter);
            trends.put(quarter, trend);
            loanIdx++;
        }

        return new ArrayList<>(trends.values());
    }

    /**
     * Get payment completion rate
     */
    public Map<String, Object> getPaymentMetrics() {
        List<Payment> allPayments = paymentRepository.findAll();

        long completed = allPayments.stream()
            .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
            .count();
        long failed = allPayments.stream()
            .filter(p -> p.getStatus() == PaymentStatus.FAILED)
            .count();
        long pending = allPayments.stream()
            .filter(p -> p.getStatus() == PaymentStatus.PENDING)
            .count();

        double completionRate = allPayments.isEmpty() ? 0 : (completed * 100.0) / allPayments.size();

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalPayments", allPayments.size());
        metrics.put("completed", completed);
        metrics.put("failed", failed);
        metrics.put("pending", pending);
        metrics.put("completionRate", Math.round(completionRate * 100.0) / 100.0);

        return metrics;
    }

    /**
     * Get all analytics summary
     */
    public Map<String, Object> getAnalyticsSummary() {
        List<Loan> allLoans = loanRepository.findAll();
        List<RiskReport> allReports = riskReportRepository.findAll();

        long activeLoans = allLoans.stream()
            .filter(l -> l.getStatus() == LoanStatus.ACTIVE)
            .count();

        long totalLoanAmount = Math.round(allLoans.stream()
            .mapToDouble(Loan::getAmount)
            .sum());

        double averageRiskScore = allReports.isEmpty() ? 0 :
            allReports.stream()
                .mapToInt(RiskReport::getRiskScore)
                .average()
                .orElse(0);

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalLoans", allLoans.size());
        summary.put("activeLoans", activeLoans);
        summary.put("totalPortfolio", totalLoanAmount);
        summary.put("averageRiskScore", Math.round(averageRiskScore * 100.0) / 100.0);
        summary.put("riskBands", getRiskScoreDistribution());
        summary.put("exposure", getPortfolioExposure());
        summary.put("trends", getQuarterlyTrends());
        summary.put("paymentMetrics", getPaymentMetrics());

        return summary;
    }
}
