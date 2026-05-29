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

@RestController
@RequestMapping("/api/ability")
@CrossOrigin(origins = "*")
public class AbilityController {

    @Autowired
    private AbilityService abilityService;

    @GetMapping("/config")
    public ResponseEntity<List<AbilityConfig>> getAllConfigs() {
        return ResponseEntity.ok(abilityService.getAllAbilityConfigs());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<UserAbility> getUserAbility(@PathVariable Long userId) {
        return ResponseEntity.ok(abilityService.getUserAbility(userId));
    }

    @PostMapping("/settle")
    public ResponseEntity<Map<String, Object>> settleExploration(
            @RequestParam Long userId,
            @RequestBody List<Item> inventory) {
        Map<String, Object> result = abilityService.settleExploration(userId, inventory);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/upgrade")
    public ResponseEntity<Map<String, Object>> upgradeAbility(
            @RequestParam Long userId,
            @RequestParam String abilityCode) {
        Map<String, Object> result = abilityService.upgradeAbility(userId, abilityCode);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/stats/{userId}")
    public ResponseEntity<Map<String, Integer>> getUserStats(@PathVariable Long userId) {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("maxWeight", abilityService.getMaxWeight(userId));
        stats.put("goldBonusPercent", abilityService.getGoldBonusPercent(userId));
        stats.put("moveSpeed", abilityService.getMoveSpeed(userId));
        return ResponseEntity.ok(stats);
    }
}
