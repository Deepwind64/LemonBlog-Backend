package com.bluemsun.lemonpojo.vo;

import com.bluemsun.lemonpojo.entity.RankVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassifiedColumnVO {
    private Integer pages;
    private Long total;
    private List<RankVO> column;
}
