package org.example.spotify_music.controller;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.example.spotify_music.common.Result;
import org.example.spotify_music.entity.User;
import org.example.spotify_music.mapper.SongMapper;
import org.example.spotify_music.mapper.UserMapper;
import org.example.spotify_music.vo.SongVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/recommend")
public class RecommendationController {

    @Autowired
    private SongMapper songMapper;
    @Autowired
    private UserMapper userMapper;

    // Python 服务的地址 (确保 Python uvicorn 跑在 5000 端口)
    private static final String PYTHON_API_URL = "http://localhost:5000/predict";

    private Long getCurrentUserId() {
        String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User>().eq("username", username));
        return user.getId();
    }

    @GetMapping("/daily")
    public Result<List<SongVo>> getDailyRecommend() {
        Long userId = getCurrentUserId();
        List<Long> songIds = new ArrayList<>();

        System.out.println(">>> Java: 正在请求 Python 推荐算法... UserID: " + userId);

        try {
            // 1. 构造请求参数
            JSONObject param = new JSONObject();
            param.set("user_id", userId);
            param.set("k", 10); // 推荐 10 首

            // 2. 调用 Python 接口
            String responseStr = HttpUtil.post(PYTHON_API_URL, param.toString());
            System.out.println(">>> Java: Python 响应: " + responseStr);

            // 3. 解析结果
            JSONObject jsonRes = JSONUtil.parseObj(responseStr);
            JSONArray ids = jsonRes.getJSONArray("recommendations");
            songIds = ids.toList(Long.class);

        } catch (Exception e) {
            System.err.println("⚠️ 警告：连接 Python 算法服务失败，启用兜底策略。");
            // 4. 兜底策略：如果 Python 没开，返回数据库前 10 首歌
            List<SongVo> allSongs = songMapper.selectSongVoList();
            if (allSongs.size() > 10) {
                return Result.success(allSongs.subList(0, 10));
            }
            return Result.success(allSongs);
        }

        if (songIds.isEmpty()) {
            return Result.success(new ArrayList<>());
        }

        // 5. 根据 ID 查出歌曲详情
        List<SongVo> songs = songMapper.selectSongVoListByIds(songIds);
        return Result.success(songs);
    }
}