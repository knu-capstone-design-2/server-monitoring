package kr.cs.interdata.api_backend.dto.history_dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoryFilter {
    private String date;
    private String machineType;
    private String hostName;
    private String machineName;
    private String messageType;
    private String metricName;
}