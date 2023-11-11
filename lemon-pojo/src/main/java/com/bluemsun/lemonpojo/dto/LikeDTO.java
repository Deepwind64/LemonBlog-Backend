package com.bluemsun.lemonpojo.dto;

import com.bluemsun.lemonpojo.Validation.EnumValue;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LikeDTO {
    @EnumValue(intValues={0,1}, message="非法操作类型")
    private Integer type;
}
