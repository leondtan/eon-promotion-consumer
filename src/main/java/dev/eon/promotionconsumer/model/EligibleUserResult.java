package dev.eon.promotionconsumer.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EligibleUserResult {
    private String email;
    private String name;
    private String periodType;
    private Integer periodValue;
    private Double totalDebit;
    private Double totalCredit;
}
