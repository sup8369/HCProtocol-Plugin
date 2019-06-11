package socket;
 
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin; 
import org.bukkit.plugin.java.JavaPlugin;

import com.google.gson.JsonObject;
import io.socket.client.IO;
import io.socket.client.Socket;
import net.minecraft.server.v1_5_R3.ServerCommand;

import org.json.JSONObject;

public class main extends JavaPlugin implements Listener {
	static Socket socket;
	public static API api = new API();
	
	
	
    public void onEnable() {
    	
    	
    	getCommand("socket").setExecutor(new CommandUtil());
        this.getServer().getPluginManager().registerEvents((Listener)this, (Plugin)this);
        
        
        
        try {
        	
            final File[] libs = new File[] {
                new File(getDataFolder(), "engine.io-client-1.0.0.jar"),
                new File(getDataFolder(), "json-20180813.jar"),
                new File(getDataFolder(), "okhttp-3.8.1.jar"),
                new File(getDataFolder(), "okio-1.13.0.jar"),
                new File(getDataFolder(), "socket.io-client-1.0.0.jar"),
                new File(getDataFolder(), "gson-2.8.5.jar") 
            };
            
            for (final File lib : libs) if (!lib.exists()) JarUtil.extractFromJar(lib.getName(), lib.getAbsolutePath());
            
            
            for (final File lib : libs) {
                if (!lib.exists()) {
                    api.serverLogger(ChatColor.RED + "해당 라이브러리를 로드중 오류가 발생하였습니다. -> " + lib.getName());
                    Bukkit.getServer().getPluginManager().disablePlugin(this);
                    return;
                }
                addClassPath(JarUtil.getJarUrl(lib));
            }
        } catch (final Exception e) {
        	api.serverLogger(ChatColor.RED +"Failed to Load Dependency");
        } 
        
        try {
			socket = IO.socket("http://localhost:3100");
			socket
			.on(Socket.EVENT_CONNECT, (Object... objects) -> { 
				/* CONN_SERVER_PASSCODE(15LEN) */
                socket.emit("CONN_SERVER_141929102318938");
            })
			.on("ConsoleLogging", (Object... objects) -> {
                api.serverLogger(ChatColor.GREEN + objects[0].toString());
            })
			.on("ServerBroadcast",(Object... objects) -> {
                api.serverBroadcast(objects[0].toString());
            })
			.on("ServerCommand",(Object... objects) -> { 
                api.serverCommandExecutor(objects[0].toString());
            })
			.on("SendMessage",(Object... objects) -> { 
                api.sendMessageToPlayer(Bukkit.getPlayer("Matchete949"), objects[0].toString());
            });
			socket.connect(); 
		} catch (URISyntaxException e) { 
			api.serverLogger(ChatColor.RED +"Failed to connect");
		} catch (final Exception e) {
        	api.serverLogger(ChatColor.RED +"Socket.IO ENABLE ERROR");
        } 
        
    }
     
    public void onDisable() {
    	try {
    		socket.disconnect();
    		socket.close();
    		api.serverLogger(ChatColor.RED + "Socket Closed!");
    	} catch (final Exception e) {
    		api.serverLogger(ChatColor.RED +"Socket.IO DISABLE ERROR");
        } 
    	
    } 
    
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
    	JsonObject preJsonObject = new JsonObject();
    	preJsonObject.addProperty("type", e.getEventName());
    	preJsonObject.addProperty("playerName", e.getPlayer().getDisplayName()); 
        JSONObject jsonObject = new JSONObject(preJsonObject.toString());
        socket.emit("EVENT_EMIT",jsonObject);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
    	JsonObject preJsonObject = new JsonObject();
    	preJsonObject.addProperty("type", e.getEventName());
    	preJsonObject.addProperty("playerName", e.getPlayer().getDisplayName()); 
        JSONObject jsonObject = new JSONObject(preJsonObject.toString());
        socket.emit("EVENT_EMIT",jsonObject);
    }

    @EventHandler
    public void onKick(PlayerKickEvent e) {
    	JsonObject preJsonObject = new JsonObject();
    	preJsonObject.addProperty("type", e.getEventName());
    	preJsonObject.addProperty("playerName", e.getPlayer().getDisplayName()); 
        JSONObject jsonObject = new JSONObject(preJsonObject.toString());
        socket.emit("EVENT_EMIT",jsonObject);
    } 
    
    @EventHandler (priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent e) { 
    	JsonObject preJsonObject = new JsonObject();
    	preJsonObject.addProperty("type", e.getEventName());
    	preJsonObject.addProperty("playerName", e.getPlayer().getDisplayName());
        preJsonObject.addProperty("playerMessage", e.getMessage());
        JSONObject jsonObject = new JSONObject(preJsonObject.toString());
        socket.emit("EVENT_EMIT",jsonObject);
    }
    
    
    private void addClassPath(final URL url) throws IOException {
        final URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        final Class<URLClassLoader> sysclass = URLClassLoader.class;
        try {
            final Method method = sysclass.getDeclaredMethod("addURL", new Class[] { URL.class });
            method.setAccessible(true);
            method.invoke(sysloader, new Object[] { url });
        } catch (final Throwable t) {
            t.printStackTrace();
            api.serverLogger(ChatColor.RED +"Error adding " + url + " to system classloader");
            throw new IOException(ChatColor.RED +"Error adding " + url + " to system classloader");
        }
    }
}

