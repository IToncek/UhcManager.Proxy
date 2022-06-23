package me.itoncek.uhccore.proxy;

import express.Express;
import express.utils.Status;
import jdk.internal.loader.AbstractClassLoaderValue;
import net.ME1312.Galaxi.Library.Version.Version;
import net.ME1312.SubServers.Bungee.Host.*;
import net.ME1312.SubServers.Bungee.SubAPI;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.json.JSONObject;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class ManagerThread implements Runnable {
    private final SubAPI api;
    private final JSONObject config;

    public ManagerThread(SubAPI api, JSONObject object) {
        this.api = api;
        this.config = object;
    }

    @Override
    public void run() {
        Express express = new Express();

        express.get("/", (req, res) -> {
            res.send(LocalDateTime.now().format(DateTimeFormatter.ISO_INSTANT));
        });

        express.get("/create", (req, res) -> {
            SecureRandom random = new SecureRandom();

            String id = "uhc-" + random.ints(48, 122 + 1)
                .limit(8)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
            int port = 0;
            for (Integer reservedPort : api.getHost("~").getCreator().getReservedPorts()) {
                if (reservedPort > port){
                    port = reservedPort;
                }
            }

            Map<String, ? extends SubServer> pre = api.getHost("~").getSubServers();

            int finalPort = port;
            api.getHost("~").getCreator().create(id, api.getHost("~").getCreator().getTemplate("UhcCore"), Version.fromString("1.19"), port+1, (subServer -> {
                Map<String, ? extends SubServer> post = api.getHost("~").getSubServers();
                List<SubServer> sub = new ArrayList<>();
                for (Map.Entry<String, ? extends SubServer> entry : post.entrySet()) {
                    if(pre.containsKey(entry.getKey())){
                        sub.add(entry.getValue());
                    }
                }
                if(sub.size() == 1) {
                    res.send(new JSONObject().put("serverName", sub.get(0).getName()).put("port", finalPort).toString(4));
                } else {
                    res.sendStatus(Status._300);
                    res.send("Too many servers");
                }
            }));
        });

        express.get("/send/", (req, res) -> {
            Scanner sc = new Scanner(req.getBody());
            StringBuilder sb = new StringBuilder();
            while (sc.hasNext()){
                sb.append(sc.next());
            }
            JSONObject object = new JSONObject(sb.toString());

            ProxiedPlayer player = api.getRemotePlayer(object.getString("id")).get();
            ServerInfo info = api.getServer(object.getString("server"));

            if(player.isConnected()){
                if(player.getServer().getInfo().getName().equals("main")){
                    player.connect(info);
                    res.send(new JSONObject().put("status", "200 OK").toString(4));
                } else {
                    res.send(new JSONObject().put("status", "401 Unauthorised").toString(4));
                }
            } else {
                res.send(new JSONObject().put("status", "404 Not Found").toString(4));
            }
        });

        express.listen(config.getInt("port"));
    }
}
