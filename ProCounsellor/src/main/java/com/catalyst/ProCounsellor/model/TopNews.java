package com.catalyst.ProCounsellor.model;

import com.google.cloud.Timestamp;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TopNews {
    private String newsId;
    private String descriptionParagraph;
    private String fullNews;
    private String imageUrl;
    private Timestamp date; 

    public TopNews(String newsId, String descriptionParagraph, String fullNews, String imageUrl, Timestamp date) {
        this.newsId = newsId;
        this.descriptionParagraph = descriptionParagraph;
        this.fullNews = fullNews;
        this.imageUrl = imageUrl;
        this.date = date;
    }
}
