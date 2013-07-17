/**
 * 
 * This file is part of InputGui.
 *
 * Copyright (c) 2013 Cybermaxke
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 */
package me.cybermaxke.inputgui;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import org.bukkit.Location;

import com.comphenix.protocol.Packets.Server;
import com.comphenix.protocol.Packets.Client;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;

public class InputPacketAdapter extends PacketAdapter {
	private InputGuiPlugin plugin;

	public InputPacketAdapter(InputGuiPlugin plugin) {
		super(plugin, ConnectionSide.BOTH, ListenerPriority.NORMAL,
				/**
				 * Packets from the server.
				 */
				Server.BLOCK_CHANGE,
				Server.CLOSE_WINDOW,
				Server.OPEN_WINDOW,

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
		InputPlayer player = this.plugin.getPlayerManager().getPlayer(e.getPlayer());

		/**
		 * If the gui is closed, return.
		 */
		if (!player.hasGuiOpened()) {
			return;
		}

		/**
		 * Cancel block changes for the block until the gui closes.
		 */
		int id = e.getPacketID();
		if (id == Server.BLOCK_CHANGE) {
			/**
			 * 
			 * TODO: Fix
			 * 
			 * int x = packet.getIntegers().read(0);
			 * int y = packet.getIntegers().read(1);
			 * int z = packet.getIntegers().read(2);
			 *
			 * Location l = player.getFakeBlockLocation();
			 * if (l.getBlockX() == x || l.getBlockY() == y || l.getBlockZ() == z) {
			 * 	  e.setCancelled(true);
			 *	  return;
			 * }
			 * 
			 */
		/**
		 * If this packets are send, the gui can't be open.
		 */
		} else if (id == Server.CLOSE_WINDOW ||
				id == Server.OPEN_WINDOW) {
			player.setGuiOpened(false);
		}
	}

	@Override
    public void onPacketReceiving(PacketEvent e) {
		PacketContainer packet = e.getPacket();
		InputPlayer player = this.plugin.getPlayerManager().getPlayer(e.getPlayer());

		/**
		 * If the gui is closed, return.
		 */
		if (!player.hasGuiOpened()) {
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
				if (l.getBlockX() != x || l.getBlockY() != y || l.getBlockZ() != z) {
					player.setGuiOpened(false);
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
				player.setGuiOpened(false);

				/**
				 * Test the string!
				 */
				e.getPlayer().sendMessage(string);
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
			player.setGuiOpened(false);
		}
	}
}