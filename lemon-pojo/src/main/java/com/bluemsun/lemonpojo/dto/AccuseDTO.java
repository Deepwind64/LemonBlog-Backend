package com.bluemsun.lemonpojo.dto;

import com.bluemsun.lemonpojo.Validation.EnumValue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccuseDTO {
    @EnumValue(intValues={0,1}, message="非法操作类型")
    private Integer type;
    @NotBlank
    private String content;
    @NotBlank
    private String brief;
    @Min(message = "非法ID值", value = 1L)
    private Long objectId;
}
