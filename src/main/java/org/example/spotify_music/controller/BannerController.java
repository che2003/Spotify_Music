package org.example.spotify_music.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.time.LocalDateTime;
import java.util.List;
import org.example.spotify_music.common.Result;
import org.example.spotify_music.entity.SysBanner;
import org.example.spotify_music.mapper.SysBannerMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class BannerController {

    @Autowired private SysBannerMapper sysBannerMapper;

    /**
     * 前台：获取已启用的首页 Banner 列表
     */
    @GetMapping("/banner/list")
    public Result<List<SysBanner>> list() {
        QueryWrapper<SysBanner> query = new QueryWrapper<>();
        query.eq("is_enabled", 1).orderByAsc("sort_order").orderByDesc("update_time");
        return Result.success(sysBannerMapper.selectList(query));
    }

    /**
     * 管理后台：获取所有 Banner（包含未启用）
     */
    @GetMapping("/admin/banner/list")
    public Result<List<SysBanner>> adminList() {
        QueryWrapper<SysBanner> query = new QueryWrapper<>();
        query.orderByAsc("sort_order").orderByDesc("update_time");
        return Result.success(sysBannerMapper.selectList(query));
    }

    /**
     * 管理后台：新增或更新 Banner
     */
    @PostMapping("/admin/banner/save")
    public Result<?> save(@RequestBody SysBanner banner) {
        if (banner.getTitle() == null || banner.getImageUrl() == null) {
            return Result.error("标题和图片不能为空");
        }

        LocalDateTime now = LocalDateTime.now();
        if (banner.getId() == null) {
            if (banner.getIsEnabled() == null) {
                banner.setIsEnabled(true);
            }
            banner.setCreateTime(now);
            banner.setUpdateTime(now);
            sysBannerMapper.insert(banner);
        } else {
            banner.setUpdateTime(now);
            sysBannerMapper.updateById(banner);
        }
        return Result.success("保存成功");
    }

    /**
     * 管理后台：删除 Banner
     */
    @DeleteMapping("/admin/banner/delete/{id}")
    public Result<?> delete(@PathVariable Integer id) {
        if (id == null) {
            return Result.error("缺少主键ID");
        }
        sysBannerMapper.deleteById(id);
        return Result.success("删除成功");
    }
}
