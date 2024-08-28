package com.myproject.eventtrackingservice;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventDto {

    @NotNull
    @Min(value = 0, message = "Timestamp must be a positive integer.")
    private long timestamp;

    @Min(value = 0, message = "x must be between 0 and 1.")
    @Max(value = 1, message = "x must be between 0 and 1.")
    @Digits(integer = 1, fraction = 10, message = "x must have up to 10 decimal places.")
    private double x;

    @Min(value = 1073741823, message = "y must be greater than or equal to 1073741823.")
    @Max(value = 2147483647, message = "y must be less than or equal to 2147483647.")
    private int y;

}
