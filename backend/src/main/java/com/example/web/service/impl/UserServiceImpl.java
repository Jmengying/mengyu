package com.example.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.web.entity.User;
import com.example.web.mapper.UserMapper;
import com.example.web.service.UserService;
import com.example.web.util.JwtUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @PostConstruct
    public void init() {
        initAdminAccount();
        ensureBotAccount();
    }

    @Override
    public User register(String username, String password, String nickname, String email) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        if (userMapper.selectOne(wrapper) != null) {
            throw new RuntimeException("用户名已存在");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(encoder.encode(password));
        user.setNickname(nickname != null ? nickname : username);
        user.setEmail(email);
        user.setBio("这个用户很懒，什么都没写~");
        user.setRole("user");

        userMapper.insert(user);
        return user;
    }

    @Override
    public String login(String username, String password) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (!encoder.matches(password, user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        return jwtUtil.generateToken(user.getId(), user.getUsername());
    }

    @Override
    public User getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user != null) {
            user.setPassword(null);
        }
        return user;
    }

    @Override
    public User updateUser(User user) {
        User existing = userMapper.selectById(user.getId());
        if (existing == null) {
            throw new RuntimeException("用户不存在");
        }

        if (user.getNickname() != null) {
            existing.setNickname(user.getNickname());
        }
        if (user.getEmail() != null) {
            existing.setEmail(user.getEmail());
        }
        if (user.getBio() != null) {
            existing.setBio(user.getBio());
        }
        if (user.getAvatar() != null) {
            existing.setAvatar(user.getAvatar());
        }

        userMapper.updateById(existing);
        existing.setPassword(null);
        return existing;
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (!encoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }
        user.setPassword(encoder.encode(newPassword));
        userMapper.updateById(user);
    }

    @Override
    public List<User> getAllUsers() {
        List<User> users = userMapper.selectList(null);
        users.forEach(u -> u.setPassword(null));
        return users;
    }

    @Override
    public void deleteUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if ("admin".equals(user.getRole())) {
            throw new RuntimeException("不能删除管理员账号");
        }
        userMapper.deleteById(id);
    }

    @Override
    public void initAdminAccount() {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, "admin");
        if (userMapper.selectOne(wrapper) != null) {
            return;
        }

        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(encoder.encode("admin123"));
        admin.setNickname("管理员");
        admin.setBio("系统管理员");
        admin.setRole("admin");
        userMapper.insert(admin);
        System.out.println("✅ 管理员账号已创建 (admin / admin123)");
    }

    private void ensureBotAccount() {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, "moe-bot");
        if (userMapper.selectOne(wrapper) != null) return;
        User bot = new User();
        bot.setUsername("moe-bot");
        bot.setPassword(encoder.encode("bot123456"));
        bot.setNickname("萌域小助手");
        bot.setBio("每天为你带来最新的二次元新鲜事～");
        bot.setRole("admin");
        bot.setAvatar("https://cdn.nekosia.cat/images/catgirl/66aabd7ac47bb24acdcbc469.png");
        userMapper.insert(bot);
        System.out.println("✅ 机器人账号已创建 (moe-bot / bot123456)");
    }
}
