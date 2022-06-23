package me.itoncek.uhccore.proxy;

import net.ME1312.Galaxi.Library.Version.Version;
import net.ME1312.SubServers.Bungee.SubAPI;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.plugin.Plugin;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.util.Scanner;

public final class Proxy extends Plugin {
    @Override
    public void onEnable() {
        JSONObject object;
        if(new File("config.json").exists()) {
            try {
                Scanner sc = new Scanner(new File("config.json"));
                StringBuilder sb = new StringBuilder();

                while (sc.hasNextLine()) {
                    sb.append(sc.nextLine());
                }

                sc.close();

                object = new JSONObject(sb.toString());
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                object = new JSONObject();
                object.put("port", 8765);
                FileWriter fw = new FileWriter("config.json");
                fw.write(object.toString(4));
                fw.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if(new File("./SubServers/Templates/Main").exists()){
            getProxy().getLogger().info(ChatColor.GREEN + "Main template exists");
        } else {
            getProxy().getLogger().severe(ChatColor.RED.toString() + ChatColor.BOLD + "Main template isn't installed");
            getProxy().stop(ChatColor.RED.toString() + ChatColor.BOLD + "Main template isn't installed");
        }
        if(new File("./SubServers/Templates/UhcCore").exists()){
            getProxy().getLogger().info(ChatColor.GREEN + "UhcCore template exists");
        } else {
            getProxy().getLogger().severe(ChatColor.RED.toString() + ChatColor.BOLD + "UhcCore template isn't installed");
            getProxy().stop(ChatColor.RED.toString() + ChatColor.BOLD + "UhcCore template isn't installed");
        }

        SubAPI api = SubAPI.getInstance();

        if(api.getServer("main") != null){
            api.getHost("~").getCreator().create("main", api.getHost("~").getCreator().getTemplate("Main"), Version.fromString("1.19"), 25500, subServer -> {
                getProxy().getLogger().info("Created main server");
            });
        }

        Thread thread = new Thread(new ManagerThread(api, object));
        thread.start();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
