package com.fsweb.twitterapi.dto.tweet;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TweetUpdateRequest {

    @NotBlank(message = "Tweet content cannot be empty")
    @Size(max = 280, message = "Tweet content cannot exceed 280 characters")
    private String content;
    // Diğer alanlar (replyToTweetId, originalTweetId, isRetweet) güncellemelerde genellikle değiştirilmez,
    // bu nedenle sadece güncellenebilecek içerik alanını dahil ettik.
}