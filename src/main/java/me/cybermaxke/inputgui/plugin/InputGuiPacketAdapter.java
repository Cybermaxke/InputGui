/**
 * 
 * This software is part of the InputGui
 * 
 * Copyright (c) 2013 Cybermaxke
 * 
 * InputGui is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or 
 * any later version.
 * 
 * InputGui is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with InputGui. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package me.cybermaxke.inputgui.plugin;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import org.bukkit.Location;
import org.bukkit.Material;

import com.comphenix.protocol.Packets.Server;
import com.comphenix.protocol.Packets.Client;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;

public class InputGuiPacketAdapter extends PacketAdapter {
	private InputGuiPlugin plugin;

	public InputGuiPacketAdapter(InputGuiPlugin plugin) {
		super(plugin, ConnectionSide.BOTH, ListenerPriority.NORMAL,
				/**
				 * Packets from the server.
				 */
				Server.BLOCK_CHANGE,
				Server.CLOSE_WINDOW,
				Server.OPEN_WINDOW,
				Server.RESPAWN,

				/**
				 * Packets from the client.
				 */
				Client.CUSTOM_PAYLOAD,
				Client.CHAT,
				Client.ARM_ANIMATION,
				Client.PLACE,
				Client.WINDOW_CLICK,
				Client.USE_ENTITY,
				Client.BLOCK_DIG,
				Client.BLOCK_ITEM_SWITCH,
				Client.SET_CREATIVE_SLOT);
		ProtocolLibrary.getProtocolManager().addPacketListener(this);
		this.plugin = plugin;
	}

	@Override
	public void onPacketSending(PacketEvent e) {
		PacketContainer packet = e.getPacket();
		InputGuiPlayer player = this.plugin.getPlayer(e.getPlayer());

		/**
		 * If the gui is closed, return.
		 */
		if (!player.isGuiOpen()) {
			return;
		}

		/**
		 * Cancel block changes for the block until the gui closes.
		 */
		int id = e.getPacketID();
		if (id == Server.BLOCK_CHANGE) {
			int x = packet.getIntegers().read(0);
			int y = packet.getIntegers().read(1);
			int z = packet.getIntegers().read(2);
			int material = packet.getIntegers().read(3);

			Location l = player.getFakeBlockLocation();
			if (l.getBlockX() == x && l.getBlockY() == y && l.getBlockZ() == z && 
					material != Material.COMMAND.getId()) {
				e.setCancelled(true);
				return;
			}
		/**
		 * If this packets are send, the gui can't be open.
		 */
		} else if (id == Server.CLOSE_WINDOW ||
				id == Server.OPEN_WINDOW) {
			player.setCancelled();
		}
	}

	@Override
	public void onPacketReceiving(PacketEvent e) {
		PacketContainer packet = e.getPacket();
		InputGuiPlayer player = this.plugin.getPlayer(e.getPlayer());

		/**
		 * If the gui is closed, return.
		 */
		if (!player.isGuiOpen()) {
			return;
		}

		int id = e.getPacketID();
		if (id == Client.CUSTOM_PAYLOAD) {
			String tag = packet.getStrings().read(0);

			/**
			 * This is the tag that is used by the command block.
			 */
			if (!tag.equals("MC|AdvCdm")) {
				return;
			}

			byte[] data = packet.getByteArrays().read(0);

			ByteArrayInputStream bis = new ByteArrayInputStream(data);
			DataInputStream dis = new DataInputStream(bis);

			try {
				/**
				 * Reading the coords of the fake block location.
				 */
				int x = dis.readInt();
				int y = dis.readInt();
				int z = dis.readInt();

				/**
				 * Match the two locations.
				 */
				Location l = player.getFakeBlockLocation();
				if (l != null && l.getBlockX() != x || l.getBlockY() != y || l.getBlockZ() != z) {
					player.setCancelled();
					return;
				}

				/**
				 * Reading the string.
				 */
				StringBuilder builder = new StringBuilder();

				short stringLength = dis.readShort();
				for (int i = 0; i < stringLength; i++) {
					builder.append(dis.readChar());
				}

				String string = builder.toString();
				e.setCancelled(true);
				player.setConfirmed(string);
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		/**
		 * Close the gui once the player is doing something that can't happen when the gui open.
		 */
		} else if (id == Client.CHAT ||
				id == Client.ARM_ANIMATION ||
				id == Client.PLACE ||
				id == Client.WINDOW_CLICK ||
				id == Client.USE_ENTITY ||
				id == Client.BLOCK_DIG ||
				id == Client.BLOCK_ITEM_SWITCH ||
				id == Client.SET_CREATIVE_SLOT) {
			player.setCancelled();
		}
	}
}