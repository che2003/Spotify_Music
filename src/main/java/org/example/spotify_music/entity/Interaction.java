package org.example.spotify_music.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;

// 1. 删掉 @Data 和 @EqualsAndHashCode
@TableName("user_interaction")
public class Interaction extends BaseEntity {

    private Long userId;
    private Long songId;
    private Integer type;
    private BigDecimal rating;

    // 2. 手动补充 Getter / Setter
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getSongId() { return songId; }
    public void setSongId(Long songId) { this.songId = songId; }

    public Integer getType() { return type; }
    public void setType(Integer type) { this.type = type; }

    public BigDecimal getRating() { return rating; }
    public void setRating(BigDecimal rating) { this.rating = rating; }
}