package org.example.spotify_music.config;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.spotify_music.common.JwtUtils;
import org.example.spotify_music.entity.User;
import org.example.spotify_music.mapper.UserMapper;
import org.example.spotify_music.service.UserRoleService; // 【新增】
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority; // 引入权限类
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired private JwtUtils jwtUtils;
    @Autowired private UserMapper userMapper; // 查用户
    @Autowired private UserRoleService userRoleService; // 查角色 【新增】

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            try {
                username = jwtUtils.extractUsername(token);
            } catch (Exception e) {
                System.out.println("Token 解析失败或已过期");
            }
        }

        // 如果解析到用户名，并且 SecurityContext 中没有认证信息
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));

            if (user != null) {
                // 1. 查出用户的角色 Key (如 "admin", "musician")
                List<String> roleKeys = userRoleService.getRoleKeysByUserId(user.getId());

                // 2. 转换为 Spring Security 要求的权限格式 (ROLE_ADMIN, ROLE_MUSICIAN)
                List<SimpleGrantedAuthority> authorities = roleKeys.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                        .collect(Collectors.toList());

                // 3. 构造 Authentication Token 并放入 Context
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        username, null, authorities
                );

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}