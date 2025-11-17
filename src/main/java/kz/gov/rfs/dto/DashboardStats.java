package kz.gov.rfs.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardStats {
    private Long totalUsers;
    private Long activeUsers;
    private Long totalNews;
    private Long totalServices;
    private Long totalProcurements;
}