package cn.edu.whut.sept.zuul.controller;

import cn.edu.whut.sept.zuul.model.User;
import cn.edu.whut.sept.zuul.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 认证接口回归测试.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void testRegisterCreatesUser() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"tester\",\"password\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("注册成功"));

        mockMvc.perform(get("/api/auth/checkUsername")
                        .param("username", "tester"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exists").value(true));
    }

    @Test
    void testRegisterRejectsDuplicateUsername() throws Exception {
        userRepository.save(new User("tester", "123456"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"tester\",\"password\":\"abcdef\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("用户名已存在"));
    }

    @Test
    void testLoginWithCorrectPasswordSucceeds() throws Exception {
        userRepository.save(new User("tester", "123456"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"tester\",\"password\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("登录成功"))
                .andExpect(jsonPath("$.username").value("tester"));
    }

    @Test
    void testLoginWithWrongPasswordFails() throws Exception {
        userRepository.save(new User("tester", "123456"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"tester\",\"password\":\"wrong\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("密码错误"));
    }

    @Test
    void testChangePasswordUpdatesLoginPassword() throws Exception {
        userRepository.save(new User("tester", "123456"));

        mockMvc.perform(post("/api/auth/changePassword")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"tester\",\"oldPassword\":\"123456\",\"newPassword\":\"abcdef\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("密码修改成功"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"tester\",\"password\":\"abcdef\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testGetUserByUsernameReturnsUserId() throws Exception {
        User user = userRepository.save(new User("tester", "123456"));

        mockMvc.perform(get("/api/auth/user")
                        .param("username", "tester"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.username").value("tester"));
    }
}
