package com.jobbed.analytics;

import com.jobbed.analytics.dto.AnalyticsDtos.CompanyPerformance;
import com.jobbed.analytics.dto.AnalyticsDtos.OverviewResponse;
import com.jobbed.analytics.dto.AnalyticsDtos.SourcePerformance;
import com.jobbed.analytics.dto.AnalyticsDtos.StatusCount;
import com.jobbed.analytics.dto.AnalyticsDtos.SuccessRates;
import com.jobbed.analytics.dto.AnalyticsDtos.TimeSeries;
import com.jobbed.security.SecurityUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics")
@Tag(name = "Analytics", description = "Kennzahlen und Auswertungen")
public class AnalyticsController {

    private final AnalyticsService service;

    public AnalyticsController(AnalyticsService service) {
        this.service = service;
    }

    @GetMapping("/overview")
    public OverviewResponse overview() {
        return service.overview(SecurityUtils.currentUserId());
    }

    @GetMapping("/status-distribution")
    public List<StatusCount> statusDistribution() {
        return service.statusDistribution(SecurityUtils.currentUserId());
    }

    @GetMapping("/applications-over-time")
    public TimeSeries applicationsOverTime(@RequestParam(defaultValue = "month") String granularity) {
        return service.applicationsOverTime(SecurityUtils.currentUserId(), granularity);
    }

    @GetMapping("/success-rate")
    public SuccessRates successRate() {
        return service.successRate(SecurityUtils.currentUserId());
    }

    @GetMapping("/source-performance")
    public List<SourcePerformance> sourcePerformance() {
        return service.sourcePerformance(SecurityUtils.currentUserId());
    }

    @GetMapping("/company-performance")
    public List<CompanyPerformance> companyPerformance() {
        return service.companyPerformance(SecurityUtils.currentUserId());
    }
}
