package cn.edu.whut.sept.zuul.controller;

import cn.edu.whut.sept.zuul.model.AbilityConfig;
import cn.edu.whut.sept.zuul.model.Item;
import cn.edu.whut.sept.zuul.model.UserAbility;
import cn.edu.whut.sept.zuul.service.AbilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 能力系统 REST API 控制器.
 * 提供能力配置、用户能力、升级等接口.
 */
@RestController
@RequestMapping("/api/ability")
@CrossOrigin(origins = "*")
public class AbilityController {

    @Autowired
    private AbilityService abilityService;

    /**
     * 获取所有能力配置.
     *
     * @return 所有能力配置列表
     */
    @GetMapping("/config")
    public ResponseEntity<List<AbilityConfig>> getAllConfigs() {
        return ResponseEntity.ok(abilityService.getAllAbilityConfigs());
    }

    /**
     * 获取用户的能力信息.
     *
     * @param userId 用户ID
     * @return 用户能力信息
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<UserAbility> getUserAbility(@PathVariable Long userId) {
        return ResponseEntity.ok(abilityService.getUserAbility(userId));
    }

    /**
     * 结算探索奖励.
     * 根据背包物品计算金币奖励并更新用户金币.
     *
     * @param userId    用户ID
     * @param inventory 背包物品列表
     * @return 结算结果
     */
    @PostMapping("/settle")
    public ResponseEntity<Map<String, Object>> settleExploration(
            @RequestParam Long userId,
            @RequestBody List<Item> inventory) {
        Map<String, Object> result = abilityService.settleExploration(userId, inventory);
        return ResponseEntity.ok(result);
    }

    /**
     * 升级能力.
     *
     * @param userId      用户ID
     * @param abilityCode 能力代码
     * @return 升级结果
     */
    @PostMapping("/upgrade")
    public ResponseEntity<Map<String, Object>> upgradeAbility(
            @RequestParam Long userId,
            @RequestParam String abilityCode) {
        Map<String, Object> result = abilityService.upgradeAbility(userId, abilityCode);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取用户属性统计.
     *
     * @param userId 用户ID
     * @return 用户属性统计
     */
    @GetMapping("/stats/{userId}")
    public ResponseEntity<Map<String, Integer>> getUserStats(@PathVariable Long userId) {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("maxWeight", abilityService.getMaxWeight(userId));
        stats.put("goldBonusPercent", abilityService.getGoldBonusPercent(userId));
        stats.put("moveSpeed", abilityService.getMoveSpeed(userId));
        return ResponseEntity.ok(stats);
    }
}
