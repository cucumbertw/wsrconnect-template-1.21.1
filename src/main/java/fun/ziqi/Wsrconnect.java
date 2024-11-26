package fun.ziqi;

import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.command.CommandSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.PlayerManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;


import net.minecraft.text.Text;
import org.java_websocket.WebSocket;
import com.google.gson.Gson;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.server.WebSocketServer;
import net.minecraft.server.command.ServerCommandSource;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static net.minecraft.server.command.CommandManager.*;

import static net.minecraft.server.command.CommandManager.literal;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
//import net.minecraft.server.command.CommandRegistryAccess;
//import net.minecraft.server.command.CommandSource;
//import net.minecraft.server.command.CommandSyntaxException;
import net.minecraft.text.Text;


public class Wsrconnect implements ModInitializer {
	private WebSocketServer webSocketServer;
	private static MinecraftServer server;
	private WebSocketClient client;
	private final ScheduledExecutorService reconnectExecutor = Executors.newSingleThreadScheduledExecutor();
	private boolean allowReconnect = true;






	@Override
	public void onInitialize() {
		System.out.println("启动聊天监听");
		System.out.println(WebSocketServer.class.getResource("/org/java_websocket/server/WebSocketServer.class"));

//		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
//			dispatcher.register(literal("stoprco").executes(context -> {
//				return toggleReconnect(context.getSource(), false);
//			}));
//
//			dispatcher.register(literal("startrco").executes(context -> {
//				return toggleReconnect(context.getSource(), true);
//			}));
//		});
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(literal("WSConnect")
					.then(literal("allowAutoReconnect")
							.requires(source -> source.hasPermissionLevel(2))
							.then(literal("true")
									.executes(context -> {

										System.out.println("true");
										// 对于 1.19 以下的版本，使用 ''new LiteralText''。
										// 对于 1.20 以下的版本，直接使用 ''Text'' 对象而非 supplier。
										allowReconnect = true;
										context.getSource().sendFeedback(() -> Text.literal("设置为true"), false);

										return 1;
									})
							)
							.then(literal("false")
									.executes(context -> {

										System.out.println("false");
										// 对于 1.19 以下的版本，使用 ''new LiteralText''。
										// 对于 1.20 以下的版本，直接使用 ''Text'' 对象而非 supplier。
										allowReconnect = false;
										context.getSource().sendFeedback(() -> Text.literal("设置为false"), false);

										return 1;
									})
							)

					)

			);

		});





		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
					Wsrconnect.server = server;
					ConnectWS();
				});
//		int port = 20266;

//		webSocketServer = new WebSocketServer(new InetSocketAddress(port)) {
//			@Override
//			public void onOpen(WebSocket conn, ClientHandshake handshake) {
//				System.out.println("Robot connected: " + conn.getRemoteSocketAddress());
//			}
//			@Override
//			public void onClose(WebSocket conn, int code, String reason, boolean remote) {
//				System.out.println("Robot disconnected: " + reason);
//			}
//
//			@Override
//			public void onMessage(WebSocket conn, String message) {
//				System.out.println("收到消息: " + message);
//                try {
//                    broadcastToMinecraft(message);
//                } catch (CommandSyntaxException e) {
//                    throw new RuntimeException(e);
//                }
//
//            }
//
//			@Override
//			public void onError(WebSocket conn, Exception ex) {
//				ex.printStackTrace();
//			}
//
//			@Override
//			public void onStart() {
//				System.out.println("WebSocket server started on port " + port);
//			}
//		};
//
//
//
//		webSocketServer.start();

//		registerEvents();










	}




	private void ConnectWS(){
		try {

			client = new WebSocketClient(new URI("ws://571d24446069d653.natapp.cc:60244")) {
//			client = new WebSocketClient(new URI("ws://127.0.0.1:8765")) {
				@Override
				public void onOpen(ServerHandshake serverHandshake) {

				}

				@Override
				public void onMessage(String s) {
					System.out.println("收到消息: " + s);
					try {
						broadcastToMinecraft(s);
					} catch (CommandSyntaxException e) {
						throw new RuntimeException(e);
					}

				}

				@Override
				public void onClose(int i, String s, boolean b) {
					if (allowReconnect){
						System.out.println("onclose");
						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
							throw new RuntimeException(e);
						}
//                    System.out.println(client.isClosed());
//					System.out.println(client.isClosing());
//					System.out.println(client.isOpen());
						new Thread(() -> {

							while (client.isClosing() || client.isClosed()){
								try {
									Thread.sleep(10000);
									System.out.println("尝试重连");
//							broadcastToMinecraft("机器人连接断开，尝试重连 ");
									PlayerManager pm = server.getPlayerManager();

									pm.broadcast(Text.of("机器人连接关闭，重连"), true);
									System.out.println("尝试重连222");
									try {
										reconnect();  // 在新线程中进行重连
									}finally {

									}

								} catch (InterruptedException e) {
									throw new RuntimeException(e);
								}

							}
							System.out.println("重连成功！");

						}).start();



					}

//					reconnectExecutor.scheduleWithFixedDelay(() -> {
//						if (client.isClosed() || client.isClosing()) {
//							try {
//								System.out.println("尝试重连...");
//								reconnect();
//							} catch (Exception e) {
//								e.printStackTrace();
//							}
//						}
//					}, 0, 10, TimeUnit.SECONDS);




				}

				@Override
				public void onError(Exception e) {
//					try {
//						System.out.println("onerror");
//						broadcastToMinecraft(String.valueOf(e));
//						System.out.println(client.isClosing() || client.isClosed());
//						new Thread(() -> {
//
//						while (client.isClosing() || client.isClosed()){
//							try {
//								Thread.sleep(10000);
//								System.out.println("尝试重连");
////							broadcastToMinecraft("机器人连接断开，尝试重连 ");
//								PlayerManager pm = server.getPlayerManager();
//
//								pm.broadcast(Text.of("机器人连接发生错误，重连"), true);
//									try {
//										reconnect();  // 在新线程中进行重连
//									}finally {
//
//									}
//
//							} catch (InterruptedException ex) {
//                                throw new RuntimeException(ex);
//                            }
//						}
//						}).start();
//
//						System.out.println("重连成功！");
//					} catch (CommandSyntaxException ex) {
//						throw new RuntimeException(ex);
//					}
				}
			};
			client.connect();
			registerEvents();


		}catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}



	private void registerEvents() {
		ServerMessageEvents.CHAT_MESSAGE.register((message, player, sender) -> {
			String msg = message.getContent().getString();
			String pname = player.getName().getString();
			System.out.println("[Chat] " + pname + "说了: " + msg);
			sendMessageToRobot(pname, msg, "0");
		});
	}

	private void broadcastToMinecraft(String message) throws CommandSyntaxException {

		try {
			JsonObject jsonObject = JsonParser.parseString(message).getAsJsonObject();
			String nickname = jsonObject.get("nickname").getAsString();
			String msg = jsonObject.get("msg").getAsString();
			int code = jsonObject.get("code").getAsInt();

			PlayerManager pm = server.getPlayerManager();
			String Version = server.getVersion();
			int ServerPort = server.getServerPort();
			String ServerIP = server.getServerIp();
//		String icon = server.getIconFile();
			String motd = server.getServerMotd();
			StringBuilder playerList2 = new StringBuilder();
			int onlinePlayer2 = 0;

			for (ServerPlayerEntity player : pm.getPlayerList()) {
				System.out.println(player);
				if (!player.getGameProfile().getName().toLowerCase().startsWith("bot")) {
					onlinePlayer2++;
					playerList2.append(player.getGameProfile().getName()).append("\n");
				}
			}
			String playerList = playerList2.toString();
			String onlinePlayer = String.valueOf(onlinePlayer2);





			System.out.println(nickname + ": " + msg + code);
//		sendMessage(Text.of(message));
			if (code == 0){
//			正常发送消息
				pm.broadcast(Text.of("[群组]" + nickname + ": " + msg), false);
			} else if (code == 1) {
//			在线玩家
				sendMessageToRobot(onlinePlayer, playerList, "1");
				pm.broadcast(Text.of(nickname + ": 在群内触发了获取在线玩家命令"), true);
			} else if (code == 2){
//			服务器信息
				pm.broadcast(Text.of(nickname + ": 在群内触发了获取在线玩家命令"), true);

				String content = "服务器端口(参考):" + ServerPort + "\nmotd" + motd;
				sendMessageToRobot("server",content,"2");
			} else if (code == 3){
//			添加白名单
				System.out.println("添加白名单" + msg);




				// 提取出匹配的白名单字符串op
				String whitelistName = msg;
				String command = "easywhitelist add " + whitelistName;
				System.out.println("提取的白名单字符串: [" + whitelistName + "]");

				ServerCommandSource source = server.getCommandSource();
				CommandDispatcher<ServerCommandSource> dispatcher = server.getCommandManager().getDispatcher();
				ParseResults<ServerCommandSource> parseResults = dispatcher.parse(command, source);
				sendMessageToRobot("server","text","3");
				dispatcher.execute(parseResults);
				pm.broadcast(Text.of(msg + " 被添加为白名单"), true);

			} else if (code ==4){
				pm.broadcast(Text.of(nickname + " 在群内申请了白名单,请管理员查看群聊"), true);

			}
		}catch (JsonSyntaxException  e){
			System.out.println("非json");
		}




	}






	public void sendMessageToRobot(String pname, String message, String code) {
//		if (webSocketServer != null) {
		if (client.isOpen()) {
//			for (WebSocket conn : webSocketServer.getConnections()) {
//				Map<String, String> messageData = new HashMap<>();
//
//				messageData.put("player", pname);
//				messageData.put("message", message);
//				messageData.put("code", code);
//
//				Gson gson = new Gson();
//				String jsonMessage = gson.toJson(messageData);
//
//				conn.send(jsonMessage);
//			}
			Map<String, String> messageData = new HashMap<>();

			messageData.put("player", pname);
			messageData.put("message", message);
			messageData.put("code", code);

			Gson gson = new Gson();
			String jsonMessage = gson.toJson(messageData);

			client.send(jsonMessage);
		} else {
			System.out.println("WebSocket server is not initialized or not ready.");
		}



	}
}
