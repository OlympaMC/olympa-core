package fr.olympa.core.spigot;

import java.lang.reflect.Field;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import net.minecraft.server.v1_16_R3.IChatBaseComponent;
import net.minecraft.server.v1_16_R3.PacketPlayOutChat;

public class DebugTheTwo implements Listener {
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		handlePlayerPackets(e.getPlayer());
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		unhandlePlayerPacket(e.getPlayer());
	}
	

    private void unhandlePlayerPacket(Player player) {
        Channel channel = ((CraftPlayer) player).getHandle().playerConnection.networkManager.channel;
        channel.eventLoop().submit(() -> {
            channel.pipeline().remove(player.getName());
            return null;
        });
    }

    private void handlePlayerPackets(Player player) {
    	
        ChannelDuplexHandler channelDuplexHandler = new ChannelDuplexHandler() {
        	
            @Override
            public void channelRead(ChannelHandlerContext channelHandlerContext, Object handledPacket) throws Exception {
            	super.channelRead(channelHandlerContext, handledPacket);
            }

            @Override
            public void write(ChannelHandlerContext channelHandlerContext, Object packet, ChannelPromise channelPromise) throws Exception {
            	
            	
            	if (packet instanceof PacketPlayOutChat) {
            		PacketPlayOutChat chat = ((PacketPlayOutChat)packet);
            		
            		Field f = PacketPlayOutChat.class.getDeclaredField("a");
            		f.setAccessible(true);
            		IChatBaseComponent component = f.get(chat) == null ? null : (IChatBaseComponent) f.get(chat);
            		
            		if (component != null)
            			System.out.println("INTERCEPTED : " + component.getString());
            		
        			try {
                		
                		if (component != null && component.getString().contains("2")) {
                			throw new UnsupportedOperationException("§4Un petit 2 a été attrapé ! Message intercepté");
                		}	            		
        			} catch(Exception ex) {
        				ex.printStackTrace();
        			}

    				super.write(channelHandlerContext, packet, channelPromise);
        			return;
            	}
            	
                super.write(channelHandlerContext, packet, channelPromise);
            }
        };

        ChannelPipeline pipeline = ((CraftPlayer) player).getHandle().playerConnection.networkManager.channel.pipeline();
        pipeline.addBefore("packet_handler", player.getName() + "_2", channelDuplexHandler);

    }
}

