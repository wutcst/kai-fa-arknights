package cn.edu.whut.sept.zuul;

import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

@RestController
@RequestMapping("/api/game")
@CrossOrigin(origins = "*")
public class GameController {

    private Game game;
    private Room currentRoom;

    public GameController() {
        game = new Game();
        currentRoom = game.getCurrentRoom();
    }

    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        Map<String, Object> result = new HashMap<>();
        result.put("description", currentRoom.getShortDescription());
        result.put("longDescription", currentRoom.getLongDescription());
        result.put("exits", currentRoom.getExits());
        return result;
    }

    @PostMapping("/move")
    public Map<String, Object> move(@RequestBody Map<String, String> request) {
        String direction = request.get("direction");
        Map<String, Object> result = new HashMap<>();

        Room nextRoom = currentRoom.getExit(direction);

        if (nextRoom == null) {
            result.put("success", false);
            result.put("message", "You can't go that way!");
            result.put("description", currentRoom.getShortDescription());
            result.put("longDescription", currentRoom.getLongDescription());
            result.put("exits", currentRoom.getExits());
        } else {
            currentRoom = nextRoom;
            result.put("success", true);
            result.put("message", "You go " + direction);
            result.put("description", currentRoom.getShortDescription());
            result.put("longDescription", currentRoom.getLongDescription());
            result.put("exits", currentRoom.getExits());
        }

        return result;
    }

    @GetMapping("/help")
    public Map<String, Object> getHelp() {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Available commands: go [direction], help, quit");
        result.put("directions", new String[]{"north", "south", "east", "west"});
        return result;
    }
}
