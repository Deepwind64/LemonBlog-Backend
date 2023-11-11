package com.bluemsun.lemonpojo.dto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlogUpdateDTO {
    private String title;
    private String brief;
    private String content;
    private String[] label;
    private int[] file;
}