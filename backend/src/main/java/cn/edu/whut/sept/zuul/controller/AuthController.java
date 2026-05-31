package cn.edu.whut.sept.zuul.controller;

import cn.edu.whut.sept.zuul.model.User;
import cn.edu.whut.sept.zuul.service.AuthService;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.Optional;

/**
 * 认证 REST API 控制器.
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 用户注册.
     *
     * @param request 包含用户名和密码的请求
     * @return 注册结果
     */
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");
        String result = authService.register(username, password);
        return Map.of(
            "success", result.equals("注册成功"),
            "message", result
        );
    }

    /**
     * 用户登录.
     *
     * @param request 包含用户名和密码的请求
     * @return 登录结果
     */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");
        String result = authService.login(username, password);
        return Map.of(
            "success", result.equals("登录成功"),
            "message", result,
            "username", username
        );
    }

    /**
     * 修改密码.
     *
     * @param request 包含用户名、旧密码和新密码的请求
     * @return 修改结果
     */
    @PostMapping("/changePassword")
    public Map<String, Object> changePassword(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");
        String result = authService.changePassword(username, oldPassword, newPassword);
        return Map.of(
            "success", result.equals("密码修改成功"),
            "message", result
        );
    }

    /**
     * 检查用户名是否存在.
     *
     * @param username 用户名
     * @return 检查结果
     */
    @GetMapping("/checkUsername")
    public Map<String, Object> checkUsername(@RequestParam String username) {
        boolean exists = authService.userExists(username);
        return Map.of(
            "exists", exists
        );
    }

    /**
     * 根据用户名获取用户信息.
     *
     * @param username 用户名
     * @return 用户信息
     */
    @GetMapping("/user")
    public Map<String, Object> getUserByUsername(@RequestParam String username) {
        Optional<User> userOpt = authService.findByUsername(username);
        if (userOpt.isEmpty()) {
            return Map.of("success", false, "message", "用户不存在");
        }
        User user = userOpt.get();
        return Map.of(
            "success", true,
            "id", user.getId(),
            "username", user.getUsername()
        );
    }
}
