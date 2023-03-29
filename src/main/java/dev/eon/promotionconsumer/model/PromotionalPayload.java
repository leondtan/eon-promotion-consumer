package dev.eon.promotionconsumer.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PromotionalPayload {
    private String title;
    private Float balanceThreshold;
    private String periodType;
    private Integer periodValue;
}
