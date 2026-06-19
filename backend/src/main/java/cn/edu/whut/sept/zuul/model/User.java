package cn.edu.whut.sept.zuul.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 用户实体类.
 */
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 20)
    private String username;

    @Column(nullable = false, length = 20)
    private String password;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public User() {
    }

    /**
     * 构造函数.
     *
     * @param username 用户名
     * @param password 密码
     */
    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.createdAt = LocalDateTime.now();
    }

    /** 获取用户ID. */
    public Long getId() { return id; }

    /** 设置用户ID. */
    public void setId(Long id) { this.id = id; }

    /** 获取用户名. */
    public String getUsername() { return username; }

    /** 设置用户名. */
    public void setUsername(String username) { this.username = username; }

    /** 获取密码. */
    public String getPassword() { return password; }

    /** 设置密码. */
    public void setPassword(String password) { this.password = password; }

    /** 获取创建时间. */
    public LocalDateTime getCreatedAt() { return createdAt; }

    /** 设置创建时间. */
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
