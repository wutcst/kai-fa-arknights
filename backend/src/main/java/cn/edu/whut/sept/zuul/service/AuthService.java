package cn.edu.whut.sept.zuul.service;

import cn.edu.whut.sept.zuul.model.User;
import cn.edu.whut.sept.zuul.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.Optional;

/**
 * 认证服务层.
 */
@Service
public class AuthService {
    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 用户注册.
     */
    public String register(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            return "用户名不能为空";
        }
        if (password == null || password.trim().isEmpty()) {
            return "密码不能为空";
        }
        if (username.length() < 3 || username.length() > 20) {
            return "用户名长度需要在3-20个字符之间";
        }
        if (password.length() < 4 || password.length() > 20) {
            return "密码长度需要在4-20个字符之间";
        }
        if (userRepository.existsByUsername(username.trim())) {
            return "用户名已存在";
        }
        User user = new User(username.trim(), password);
        userRepository.save(user);
        return "注册成功";
    }

    /**
     * 用户登录.
     */
    public String login(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            return "用户名不能为空";
        }
        if (password == null || password.trim().isEmpty()) {
            return "密码不能为空";
        }
        Optional<User> userOpt = userRepository.findByUsername(username.trim());
        if (userOpt.isEmpty()) {
            return "用户名不存在";
        }
        User user = userOpt.get();
        if (!user.getPassword().equals(password)) {
            return "密码错误";
        }
        return "登录成功";
    }

    /**
     * 修改密码.
     */
    public String changePassword(String username, String oldPassword, String newPassword) {
        if (username == null || username.trim().isEmpty()) {
            return "用户名不能为空";
        }
        if (oldPassword == null || oldPassword.trim().isEmpty()) {
            return "旧密码不能为空";
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            return "新密码不能为空";
        }
        if (newPassword.length() < 4 || newPassword.length() > 20) {
            return "新密码长度需要在4-20个字符之间";
        }
        Optional<User> userOpt = userRepository.findByUsername(username.trim());
        if (userOpt.isEmpty()) {
            return "用户名不存在";
        }
        User user = userOpt.get();
        if (!user.getPassword().equals(oldPassword)) {
            return "旧密码错误";
        }
        user.setPassword(newPassword);
        userRepository.save(user);
        return "密码修改成功";
    }

    /**
     * 检查用户是否存在.
     */
    public boolean userExists(String username) {
        return userRepository.existsByUsername(username);
    }
}
