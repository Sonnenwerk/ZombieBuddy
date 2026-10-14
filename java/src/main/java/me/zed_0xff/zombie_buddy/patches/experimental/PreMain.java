package me.zed_0xff.zombie_buddy.patches.experimental;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Base64;

import me.zed_0xff.zombie_buddy.*;

public class PreMain {
    public static void premain(String[] args) {
        JavaStateDumper.init();
        WatchesAPI.init();

        // Configure HTTP server host, port and timeout from Agent.arguments (if provided)
        String hostValue    = Agent.arguments.get("lua_server_host");
        String portValue    = Agent.arguments.get("lua_server_port");
        String timeoutValue = Agent.arguments.get("lua_task_timeout");
        String apiToken     = Agent.arguments.get("lua_api_token");

        if (!Utils.isBlank(portValue)) {
            if (Utils.isBlank(apiToken)) {
                SecureRandom random = new SecureRandom();
                byte[] tokenBytes = new byte[24];
                random.nextBytes(tokenBytes);
                apiToken = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
                Logger.info("Generated random Lua API token: " + apiToken);

                try {
                    Path tokenFile = Path.of(Utils.getCacheDir(), "zbLuaAPI_token.txt");
                    Files.writeString(tokenFile, apiToken);
                    Logger.info("Wrote random API token to " + tokenFile.toAbsolutePath());
                } catch (IOException e) {
                    Logger.error("Failed to write token file: " + e.getMessage());
                    Logger.printStackTrace(e);
                }
            } else {
                Logger.info("Using configured Lua API token");
            }
            HttpServer.luaApiToken = apiToken;

            try {
                int serverPort;
                boolean isRandomPort = false;
                if ("random".equalsIgnoreCase(portValue)) {
                    serverPort = 0; // 0 means random port
                    isRandomPort = true;
                    Logger.info("Using random port for HTTP server");
                } else {
                    serverPort = Integer.parseInt(portValue);
                    if (serverPort == 0) {
                        isRandomPort = true;
                        Logger.info("Using random port for HTTP server");
                    }
                }

                HttpServer.g_verbosity = Logger.getLevel();
                String bindHost = (!Utils.isBlank(hostValue)) ? hostValue : "127.0.0.1";
                HttpServer httpServer = new HttpServer(bindHost, serverPort, isRandomPort);
                httpServer.start();

                // Register hook to write the random port file on game init
                if (isRandomPort) {
                    Callbacks.onGameInitComplete.register(() -> {
                        HttpServer server = HttpServer.getInstance();
                        if (server != null && server.wasRandomPort()) {
                            writePortFile(server.getPort());
                        }
                    });
                }
            } catch (NumberFormatException e) {
                Logger.error("invalid server_port value: " + portValue);
            } catch (Exception e) {
                Logger.error("failed to start HTTP server: " + e.getMessage());
            }
        }

        // Configure Lua task timeout from Agent.arguments (if provided)
        if (!Utils.isBlank(timeoutValue)) {
            try {
                HttpServer.luaTaskTimeoutMs = Long.parseLong(timeoutValue);
                Logger.info("Lua task timeout set to " + HttpServer.luaTaskTimeoutMs + "ms");
            } catch (NumberFormatException e) {
                Logger.error("invalid lua_task_timeout value: " + timeoutValue);
            }
        }
    }

    private static void writePortFile(int port) {
        try {
            Path portFile = Path.of(Utils.getCacheDir(), "zbLuaAPI.txt");
            Files.writeString(portFile, String.valueOf(port));
            Logger.info("Wrote random API port " + port + " to " + portFile.toAbsolutePath());
        } catch (IOException e) {
            Logger.error("Failed to write port file: " + e.getMessage());
            Logger.printStackTrace(e);
        }
    }
}
