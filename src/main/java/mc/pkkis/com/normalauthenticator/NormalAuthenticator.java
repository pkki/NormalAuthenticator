package mc.pkkis.com.normalauthenticator;

import mc.pkkis.com.normalauthenticator.database.Database;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;


public final class NormalAuthenticator extends JavaPlugin implements Listener {
    private static JavaPlugin plugin;
public static String key = "none";
public static int keytime = 60;
public static int keylength = 5;
public static String type = "time";
public static boolean typeo = true;
public static boolean arn;
public static boolean arnjoin;
public static int arntime;
public static String[] startcommands;
public static String[] notstartcommands;

//message
    public static String startmessage;
    public static String stopmessage;
    public static String Authenticationrequestnotification;
    public static String Authenticationsuccessmessage;
    public static String Authenticationsucccessmessageglobal;
    public static String Authenticationfailuremessage;
    public static String Alreadyverifiled;

    public static Database pointsDatabase;
    public static String wrongcommand;


    @Override
    public void onEnable() {;
        getServer().getPluginManager().registerEvents(this, this);
        plugin = this;
        try {
            // Ensure the plugin's data folder exists
            if (!getDataFolder().exists()) {
                getDataFolder().mkdirs();
            }

            pointsDatabase = new Database(getDataFolder().getAbsolutePath() + "/apply.db");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Failed to connect to database! " + e.getMessage());
            Bukkit.getPluginManager().disablePlugin(this);
        }
        checkDir("plugins/NormalAuthenticator/files/");
        // Plugin startup logic
        saveDefaultConfig();
        // config.ymlを読み込みます。
        FileConfiguration config = getConfig();
        keytime = config.getInt("key-change-time");
        keylength = config.getInt("key-length");
        type = config.getString("key-type");
        arn = config.getBoolean("arn");
        arnjoin = config.getBoolean("arn-join-notification");
        arntime = config.getInt("arn-time");
        List<String> co1 = config.getStringList("authentication-command");
        List<String> co2 = config.getStringList("not-authentication-command");
        startcommands = (String[]) co1.toArray(new String[co1.size()]);
        notstartcommands = (String[]) co2.toArray(new String[co2.size()]);
        startmessage = config.getString("startmessage");
        stopmessage = config.getString("stopmessage");
        Authenticationrequestnotification = config.getString("Authentication-request-notification");
        Authenticationsuccessmessage = config.getString("Authentication-success-message");
        Authenticationsucccessmessageglobal = config.getString("Authentication-success-message-global");
        Authenticationfailuremessage = config.getString("Authentication-failure-message");
        Alreadyverifiled = config.getString("Already-verified");
        wrongcommand = config.getString("wrong-command");
        settingtime(keytime);
        if(Objects.equals(type, "time")){
            Timer.countdown();

            typeo = true;
        }else{
            typeo = false;
        }
        super.onEnable();

        File filesFolder = new File(plugin.getDataFolder(), "files");
        File keyFile = new File(filesFolder, "key.html");
        File styleFile = new File(filesFolder, "style.css");
        if (!filesFolder.exists()) {
            filesFolder.mkdirs();
        }

        if (!keyFile.exists()) {
            plugin.saveResource("files/key.html", false);
        }

        if (!styleFile.exists()) {
            plugin.saveResource("files/style.css", false);
        }
        Bukkit.getLogger().info(ChatColor.translateAlternateColorCodes('§', startmessage));
    }

    public Database getdate(){
        return this.pointsDatabase;
    }
    public static JavaPlugin getPlugin() {return plugin;}
    @Override
    public void onDisable() {
        // Plugin shutdown logic
        try {
            pointsDatabase.closeConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Bukkit.getLogger().info(ChatColor.translateAlternateColorCodes('§', stopmessage));

    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent a) {
        String ok = "false";
        Player player = a.getPlayer();
        try {
            ok = pointsDatabase.getPlayerPoints(player);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        if(Objects.equals(ok, "true")) {


        }else{
            player.sendMessage(ChatColor.translateAlternateColorCodes('§', PlaceholderAPI.setPlaceholders(player,Authenticationrequestnotification)));
            for(int counting = 0; notstartcommands.length-1 >= counting;counting++){
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), PlaceholderAPI.setPlaceholders(player,notstartcommands[counting]));
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (cmd.getName().equalsIgnoreCase("NormalAuthenticator")) { //親コマンドの判定
            Player player = (Player)sender;
            if(args.length == 0){
                sender.sendMessage(ChatColor.translateAlternateColorCodes('§', PlaceholderAPI.setPlaceholders(player,Authenticationrequestnotification)));
                return false;
            }else{
                if(Objects.equals(args[0], "unlock")) {
                        if (Objects.equals(args[1], key)) {
                            String ok = "false";
                            try {
                                ok = pointsDatabase.getPlayerPoints(player);
                            } catch (SQLException e) {
                                sender.sendMessage("データベースエラー発生");
                                throw new RuntimeException(e);
                            }
                            if (Objects.equals(ok, "false")) {
                                try {
                                    pointsDatabase.updatePlayerPoints(player, "true");
                                    for (int counting = 0; startcommands.length - 1 >= counting; counting++) {
                                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), PlaceholderAPI.setPlaceholders(player,startcommands[counting]));
                                    }
                                    sender.sendMessage(ChatColor.translateAlternateColorCodes('§', PlaceholderAPI.setPlaceholders(player, Authenticationsuccessmessage)));
                                    Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('§', PlaceholderAPI.setPlaceholders(player, Authenticationsucccessmessageglobal)));
                                } catch (SQLException e) {
                                    sender.sendMessage("Error add player in sqlites");
                                    sender.sendMessage("認証情報をデータベースに書き込むのに失敗しました。");
                                    throw new RuntimeException(e);
                                }
                            } else {
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('§', PlaceholderAPI.setPlaceholders(player, Alreadyverifiled)));
                            }
                        } else {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('§', PlaceholderAPI.setPlaceholders(player, Authenticationfailuremessage)));
                        }
                }
                if(Objects.equals(args[0], "status")){
                    String ok = "false";
                    try {
                        ok = pointsDatabase.getPlayerPoints(player);
                    } catch (SQLException e) {
                        sender.sendMessage("データベースエラー発生");
                        throw new RuntimeException(e);
                    }
                    if(Objects.equals(ok, "true")){
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('§', PlaceholderAPI.setPlaceholders(player,Alreadyverifiled)));
                    }else{
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('§', PlaceholderAPI.setPlaceholders(player,Authenticationrequestnotification)));
                    }
                }
                if((Objects.equals(args[0], "status")) || (Objects.equals(args[0], "unlock"))){
                }else{
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('§', PlaceholderAPI.setPlaceholders(player,wrongcommand)));
                }
            }

        }
        if (cmd.getName().equalsIgnoreCase("adminNormalAuthenticator")) { //親コマンドの判定
            if(args.length == 0){
                sender.sendMessage("/adminNormalAuthenticator(/adminunlock) generate");
                return false;
            }else{
                if(Objects.equals(args[0], "generate")){
                    keystart();
                }
                if(Objects.equals(args[0],"unlock")){
                    String ok = "false";
                    Player player = Bukkit.getPlayer(args[1]);
                    try {
                        ok = pointsDatabase.getPlayerPoints(player);
                    } catch (SQLException e) {
                        sender.sendMessage("データベースエラー発生");
                        throw new RuntimeException(e);
                    }
                    if (Objects.equals(ok, "false")) {
                        try {
                            pointsDatabase.updatePlayerPoints(player, "true");
                            for (int counting = 0; startcommands.length - 1 >= counting; counting++) {
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), PlaceholderAPI.setPlaceholders(player,startcommands[counting]));
                            }
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('§', PlaceholderAPI.setPlaceholders(player, Authenticationsuccessmessage)));
                            Bukkit.broadcastMessage(player.getName() + ChatColor.translateAlternateColorCodes('§', PlaceholderAPI.setPlaceholders(player, Authenticationsucccessmessageglobal)));
                        } catch (SQLException e) {
                            sender.sendMessage("Error add player in sqlites");
                            sender.sendMessage("認証情報をデータベースに書き込むのに失敗しました。");
                            throw new RuntimeException(e);
                        }
                    } else {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('§', PlaceholderAPI.setPlaceholders(player, Alreadyverifiled)));
                    }
                }
                if(Objects.equals(args[0],"lock")){
                    String ok = "false";
                    Player player = Bukkit.getPlayer(args[1]);
                    try {
                        ok = pointsDatabase.getPlayerPoints(player);
                    } catch (SQLException e) {
                        sender.sendMessage("データベースエラー発生");
                        throw new RuntimeException(e);
                    }
                    if (Objects.equals(ok, "true")) {
                        try {
                            pointsDatabase.updatePlayerPoints(player, "false");
                            for (int counting = 0; notstartcommands.length - 1 >= counting; counting++) {
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), PlaceholderAPI.setPlaceholders(player,notstartcommands[counting]));
                            }
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('§', PlaceholderAPI.setPlaceholders(player, "You are looked by "+sender.getName())));
                        } catch (SQLException e) {
                            sender.sendMessage("Error add player in sqlites");
                            sender.sendMessage("認証情報をデータベースに書き込むのに失敗しました。");
                            throw new RuntimeException(e);
                        }
                    } else {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('§', PlaceholderAPI.setPlaceholders(player, "You didn't unlock.")));
                    }
                }
                if(Objects.equals(args[0], "reload")){
                    reloadConfig();
                    FileConfiguration config = getConfig();
                    keytime = config.getInt("key-change-time");
                    keylength = config.getInt("key-length");
                    type = config.getString("key-type");
                    arn = config.getBoolean("arn");
                    arnjoin = config.getBoolean("arn-join-notification");
                    arntime = config.getInt("arn-time");
                    List<String> co1 = config.getStringList("authentication-command");
                    List<String> co2 = config.getStringList("not-authentication-command");
                    startcommands = (String[]) co1.toArray(new String[co1.size()]);
                    notstartcommands = (String[]) co2.toArray(new String[co2.size()]);
                    startmessage = config.getString("startmessage");
                    stopmessage = config.getString("stopmessage");
                    Authenticationrequestnotification = config.getString("Authentication-request-notification");
                    Authenticationsuccessmessage = config.getString("Authentication-success-message");
                    Authenticationsucccessmessageglobal = config.getString("Authentication-success-message-global");
                    Authenticationfailuremessage = config.getString("Authentication-failure-message");
                    Alreadyverifiled = config.getString("Already-verified");
                    wrongcommand = config.getString("wrong-command");
                    settingtime(keytime);

                    Bukkit.getLogger().info("Reload complete");
                    sender.sendMessage("Reload complete");
                }
            }
        }
        return false;
    }

public static void keystart(){
        try {
            // FileWriterクラスのオブジェクトを生成する
            FileWriter file = new FileWriter("plugins/NormalAuthenticator/files/key.txt");
            // PrintWriterクラスのオブジェクトを生成する
            PrintWriter pw = new PrintWriter(new BufferedWriter(file));
            key =AlphaNumericStringGenerator.getRandomString(keylength);
            //ファイルに書き込む
            pw.println(key);

            //ファイルを閉じる
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void time(int time){
        try {
            // FileWriterクラスのオブジェクトを生成する
            FileWriter file = new FileWriter("plugins/NormalAuthenticator/files/time.txt");
            // PrintWriterクラスのオブジェクトを生成する
            PrintWriter pw = new PrintWriter(new BufferedWriter(file));
            //ファイルに書き込む
            pw.println(time);

            //ファイルを閉じる
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void settingtime(int time){
        try {
            // FileWriterクラスのオブジェクトを生成する
            FileWriter file = new FileWriter("plugins/NormalAuthenticator/files/defaulttime.txt");
            // PrintWriterクラスのオブジェクトを生成する
            PrintWriter pw = new PrintWriter(new BufferedWriter(file));
            //ファイルに書き込む
            pw.println(time);

            //ファイルを閉じる
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public boolean checkDir(String path) {
        File stopDir = new File(path);
        // ディレクトリ存在チェック
        if (!stopDir.exists()) {
            stopDir.mkdirs();
            return false;
        } else {
            return true;
        }
    }
    public static void noti(){
        String[] playerlist = (String[]) Bukkit.getOnlinePlayers().toArray(new String[Bukkit.getOnlinePlayers().size()]);
        for(int i =0;i == playerlist.length;i++){
            String ok = "false";
            try {
                ok = pointsDatabase.getPlayerPoints(Bukkit.getServer().getPlayer(playerlist[i]));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            if(Objects.equals(ok, "false")){
                Bukkit.getServer().getPlayer(playerlist[i]).sendMessage(ChatColor.translateAlternateColorCodes('§', PlaceholderAPI.setPlaceholders(Bukkit.getServer().getPlayer(playerlist[i]),Authenticationrequestnotification)));
            }
        }
    }
public static String get(Player target){
    String ok = "false";
    try {
        ok = pointsDatabase.getPlayerPoints(target);
        return ok;
    } catch (SQLException e) {
        throw new RuntimeException(e);
    }
}


}


