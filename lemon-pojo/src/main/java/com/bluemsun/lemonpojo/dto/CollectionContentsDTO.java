package com.bluemsun.lemonpojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollectionContentsDTO {
    private int collectionId;
    private int curPage;
}
