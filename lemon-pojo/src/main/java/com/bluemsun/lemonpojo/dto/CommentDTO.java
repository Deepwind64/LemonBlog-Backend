package com.bluemsun.lemonpojo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentDTO {
    @NotBlank
    private String content;
    @Min(message = "非法ID值", value = 1L)
    private Long parentCommentId;
    @Min(message = "非法ID值", value = 1L)
    private Long replyCommentId;

}
