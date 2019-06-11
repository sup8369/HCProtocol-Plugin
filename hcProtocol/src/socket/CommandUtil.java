package socket;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.JsonObject;


public class CommandUtil extends JavaPlugin implements CommandExecutor{
	public static String CONSOLE_SIGN = "*CONSOLE+___+SIGN*";
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) { 
		String playerName = null; 
		if (sender instanceof Player) {
			if(!sender.isOp()) return false;
            Player player = (Player) sender;
            playerName = player.getName();
        }
		
		if(playerName == null) playerName = CONSOLE_SIGN;
		
		Gson gson = new Gson();
		JsonObject preJsonObject = new JsonObject();
    	preJsonObject.addProperty("type", playerName == CONSOLE_SIGN ? "ConsoleCommandEvent" : "PlayerCommandEvent");
    	if(playerName != CONSOLE_SIGN) preJsonObject.addProperty("playerName", playerName);
    	preJsonObject.addProperty("args", gson.toJson(args));
        JSONObject jsonObject = new JSONObject(preJsonObject.toString());
		main.socket.emit("EVENT_EMIT", jsonObject);

        
        return true;
    }
}
