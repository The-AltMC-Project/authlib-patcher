package org.altmc;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Inject {
    public static String accessToken;
    public static String uuid;

    public static String authServerBase = "https://auth.altmc.org";
    public static boolean useCustomAuth = false;
    public static String originalAuthServer = "https://sessionserver.mojang.com";

    public static void saveMinecraftArgs(String[] args) {
        Main.LOGGER.info("Got launch args");
        boolean prevIsAt = false;
        boolean prevIsUuid = false;
        for (String arg : args) {
            if (arg.equals("--accessToken")) {
                prevIsAt = true;
                continue;
            } else if (arg.equals("--uuid")) {
                prevIsUuid = true;
                continue;
            }
            if (prevIsAt) {
                accessToken = arg;
                prevIsAt = false;
            }
            if (prevIsUuid) {
                uuid = arg.replaceAll("-", "");
                prevIsUuid = false;
            }

        }
    }

    public static void testForMicrosoftAuth() {
        new Thread(() -> {
            try {
                Main.LOGGER.info("Testing if Microsoft auth is working...");
                URL url = new URL(originalAuthServer + "/session/minecraft/join");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setRequestProperty("Content-Type", "application/json");
                con.setDoOutput(true);
                OutputStream out = con.getOutputStream();
                out.write(("{\"accessToken\":\"" + accessToken + "\",\"selectedProfile\":\"" + uuid + "\",\"serverId\":\"4ed1f46bbe04bc756bcb17c0c7ce3e4632f06a48\"}").getBytes());
                out.flush();
                out.close();
                int responseCode = con.getResponseCode();
                if (responseCode != 204) {
                    Main.LOGGER.info("Microsoft auth had errors, turning AltMC auth on");
                    useCustomAuth = true;
                    return;
                }
                useCustomAuth = false;
                Main.LOGGER.info("Microsoft auth seems fine, won't use AltMC servers for this session");
            } catch (Exception e) {
                useCustomAuth = true;
                Main.LOGGER.info("Microsoft auth had errors, turning AltMC auth on");
                e.printStackTrace();
            }
        }).start();
    }

    public static URL getAuthServer(URL url) throws MalformedURLException {
        return useCustomAuth ? new URL(url.toString().replace(originalAuthServer, authServerBase)) : url;
    }

    public static String getAuthServer(String string) {
        return useCustomAuth ? string.replace(originalAuthServer, authServerBase) : string;
    }

}
