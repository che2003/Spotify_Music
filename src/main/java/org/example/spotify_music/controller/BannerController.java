package org.example.spotify_music.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.List;
import org.example.spotify_music.common.Result;
import org.example.spotify_music.entity.SysBanner;
import org.example.spotify_music.mapper.SysBannerMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/banner")
public class BannerController {

    @Autowired private SysBannerMapper sysBannerMapper;

    @GetMapping("/list")
    public Result<List<SysBanner>> list(@RequestParam(defaultValue = "false") Boolean includeDisabled) {
        QueryWrapper<SysBanner> query = new QueryWrapper<>();
        if (!includeDisabled) {
            query.eq("is_enabled", 1);
        }
        query.orderByAsc("sort_order").orderByDesc("create_time");
        return Result.success(sysBannerMapper.selectList(query));
    }

    @PostMapping("/add")
    public Result<?> add(@RequestBody SysBanner banner) {
        if (banner.getIsEnabled() == null) {
            banner.setIsEnabled(true);
        }
        sysBannerMapper.insert(banner);
        return Result.success("新增成功");
    }

    @PostMapping("/update")
    public Result<?> update(@RequestBody SysBanner banner) {
        if (banner.getId() == null) {
            return Result.error("缺少主键ID");
        }
        sysBannerMapper.updateById(banner);
        return Result.success("更新成功");
    }

    @DeleteMapping("/delete/{id}")
    public Result<?> delete(@PathVariable Integer id) {
        sysBannerMapper.deleteById(id);
        return Result.success("删除成功");
    }
}
