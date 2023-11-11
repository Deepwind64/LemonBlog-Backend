package com.bluemsun.lemonpojo.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author deepwind
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlogContentDTO {
    @NotBlank
    private String title;
    private String brief;
    private String content;
    private String[] label;
    @Min(message = "非法ID值", value = 1L)
    private Long userId;
    private int[] file;

}
