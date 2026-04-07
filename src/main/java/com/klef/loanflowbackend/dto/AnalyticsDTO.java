package com.klef.loanflowbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class AnalyticsDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RiskBand {
        private String band;
        private int count;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PortfolioExposure {
        private String name;
        private long value;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuarterlyTrend {
        private String quarter;
        private int loans;
        private long volume;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentMetrics {
        private int totalPayments;
        private long completed;
        private long failed;
        private long pending;
        private double completionRate;
    }
}
