package me.doclic.noencryption;

import io.netty.channel.*;
import me.doclic.noencryption.compatibility.Compatibility;
import me.doclic.noencryption.config.ConfigurationHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin (PlayerJoinEvent e) {

        final Player player = e.getPlayer();
        final ChannelPipeline pipeline = Compatibility.COMPATIBLE_PLAYER.getChannel(player).pipeline();
        pipeline.addBefore("packet_handler", player.getUniqueId().toString(), new ChannelDuplexHandler() {

            @Override
            public void channelRead(ChannelHandlerContext channelHandlerContext, Object packet) throws Exception {

                Object newPacket = Compatibility.COMPATIBLE_PACKET_LISTENER.readPacket(channelHandlerContext, packet);
                super.channelRead(channelHandlerContext, newPacket);

            }

            @Override
            public void write(ChannelHandlerContext channelHandlerContext, Object packet, ChannelPromise promise) throws Exception {

                Object newPacket = Compatibility.COMPATIBLE_PACKET_LISTENER.writePacket(channelHandlerContext, packet, promise);
                super.write(channelHandlerContext, newPacket, promise);

            }

        });

        if (ConfigurationHandler.getLoginProtectionMessage() != null) {
            if (!ConfigurationHandler.getLoginProtectionMessage().trim().equals("")) {
                if (NoEncryption.getNewChat()) {
                    try {
                        player.sendMessage(
                                net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacy('&').deserialize(ConfigurationHandler.getLoginProtectionMessage())
                        );
                    } catch (Exception ex) {
                        player.sendMessage(
                                org.bukkit.ChatColor.translateAlternateColorCodes('&', ConfigurationHandler.getLoginProtectionMessage())
                        );
                    }
                } else {
                    player.sendMessage(
                            org.bukkit.ChatColor.translateAlternateColorCodes('&', ConfigurationHandler.getLoginProtectionMessage())
                    );
                }
            }
        }

    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuit (PlayerQuitEvent e) {

        final Player player = e.getPlayer();
        final Channel channel = Compatibility.COMPATIBLE_PLAYER.getChannel(player);
        channel.eventLoop().submit(() -> channel.pipeline().remove(player.getUniqueId().toString()));

    }

}
