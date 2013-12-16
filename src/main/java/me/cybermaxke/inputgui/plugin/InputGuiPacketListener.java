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
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import me.cybermaxke.inputgui.api.InputGuiSign;
import me.cybermaxke.inputgui.api.event.CommandBlockEditEvent;
import me.cybermaxke.inputgui.api.event.ItemRenameEvent;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CommandBlock;
import org.bukkit.entity.Player;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.PacketType.Play.Client;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerOptions;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.injector.GamePhase;

import com.google.common.base.Charsets;

public class InputGuiPacketListener implements PacketListener {
	private InputGuiPlugin plugin;

	public InputGuiPacketListener(InputGuiPlugin plugin) {
		this.plugin = plugin;
		ProtocolLibrary.getProtocolManager().addPacketListener(this);
	}

	@Override
	public ListeningWhitelist getSendingWhitelist() {
		return ListeningWhitelist.newBuilder().normal().gamePhase(GamePhase.PLAYING)
				.options(new ListenerOptions[] {})
				.types(
					Server.BLOCK_CHANGE,
					Server.BLOCK_CHANGE,
					Server.CLOSE_WINDOW,
					Server.OPEN_WINDOW,
					Server.RESPAWN)
				.build();
	}

	@Override
	public ListeningWhitelist getReceivingWhitelist() {
		return ListeningWhitelist.newBuilder().normal().gamePhase(GamePhase.PLAYING)
				.options(new ListenerOptions[] {})
				.types(
					Client.CUSTOM_PAYLOAD,
					Client.CHAT,
					Client.ARM_ANIMATION,
					Client.BLOCK_PLACE,
					Client.WINDOW_CLICK,
					Client.USE_ENTITY,
					Client.BLOCK_DIG,
					Client.CLOSE_WINDOW,
					Client.SET_CREATIVE_SLOT,
					Client.UPDATE_SIGN)
				.build();
	}

	@Override
	public Plugin getPlugin() {
		return this.plugin;
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
		PacketType type = e.getPacketType();
		if (type.equals(Server.BLOCK_CHANGE)) {
			int x = packet.getIntegers().read(0);
			int y = packet.getIntegers().read(1);
			int z = packet.getIntegers().read(2);

			Material material;
			if (ProtocolLibrary.getProtocolManager().getMinecraftVersion()
					.getVersion().startsWith("1.6.")) {
				material = Material.getMaterial(packet.getIntegers().read(3));
			} else {
				material = packet.getBlocks().read(0);
			}

			Location l = player.getFakeBlockLocation();
			if (l.getBlockX() == x && l.getBlockY() == y && l.getBlockZ() == z && 
					material != Material.COMMAND) {
				e.setCancelled(true);
				return;
			}
		/**
		 * If this packets are send, the gui can't be open.
		 */
		} else if (player.isCheckingPackets() && (type.equals(Server.CLOSE_WINDOW) ||
				type.equals(Server.OPEN_WINDOW))) {
			player.setCancelled();
		}
	}

	@Override
	public void onPacketReceiving(PacketEvent e) {
		PacketContainer packet = e.getPacket();

		Player player = e.getPlayer();
		InputGuiPlayer iplayer = this.plugin.getPlayer(player);

		PacketType type = e.getPacketType();
		if (type.equals(Client.CUSTOM_PAYLOAD)) {
			String tag = packet.getStrings().read(0);
			byte[] data = packet.getByteArrays().read(0);

			/**
			 * This is the tag that is used by the command block.
			 */
			if (tag.equals("MC|AdvCdm")) {
				ByteArrayInputStream bis = new ByteArrayInputStream(data);
				DataInputStream dis = new DataInputStream(bis);

				try {
					byte action = 0;

					if (!ProtocolLibrary.getProtocolManager().getMinecraftVersion()
							.getVersion().startsWith("1.6.")) {
						action = dis.readByte();
					}

					/**
					 * TODO: Editing a command block minecart, not supported yet.
					 */
					if (action == 1) {
						return;
					}

					/**
					 * Reading the coords of the fake block location.
					 */
					int x = dis.readInt();
					int y = dis.readInt();
					int z = dis.readInt();

					/**
					 * Reading the string.
					 */
					String string;
					if (ProtocolLibrary.getProtocolManager().getMinecraftVersion()
							.getVersion().startsWith("1.6.")) {
						StringBuilder builder = new StringBuilder();

						short stringLength = dis.readShort();
						for (int i = 0; i < stringLength; i++) {
							builder.append(dis.readChar());
						}

						string = builder.toString();
					} else {
						byte[] bytes = new byte[dis.available()];
						dis.read(bytes);
						string = new String(bytes, Charsets.UTF_8).trim();
					}

					/**
					 * We are using a custom input gui.
					 */
					if (iplayer.isGuiOpen() &&
							!(iplayer.getCurrentGui() instanceof InputGuiSign)) {
						/**
						 * Match the two locations.
						 */
						Location l = iplayer.getFakeBlockLocation();
						if (l == null || l.getBlockX() != x || l.getBlockY() != y || 
								l.getBlockZ() != z) {
							iplayer.setCancelled();
							return;
						}

						e.setCancelled(true);
						iplayer.setConfirmed(string);
					/**
					 * We are changing a command block.
					 */
					} else {
						Block block = e.getPlayer().getWorld().getBlockAt(x, y, z);
						BlockState state = block.getState();

						if (state instanceof CommandBlock) {
							CommandBlock cblock = (CommandBlock) state;
							CommandBlockEditEvent event = new CommandBlockEditEvent(player, cblock,
									cblock.getCommand(), string);
							Bukkit.getPluginManager().callEvent(event);

							if (event.isCancelled()) {
								e.setCancelled(true);
								return;
							}

							String command = event.getNewCommand();

							ByteArrayOutputStream bos = new ByteArrayOutputStream();
							DataOutputStream dos = new DataOutputStream(bos);

							dos.writeInt(x);
							dos.writeInt(y);
							dos.writeInt(z);

							dos.writeShort(command.length());
							dos.writeChars(command);

							packet.getByteArrays().write(0, bos.toByteArray());

							dos.close();
							bos.close();
						}
					}

					dis.close();
					bis.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			/**
			 * This is the tag that is used by the anvil renaming.
			 */
			} else if (tag.equals("MC|ItemName")) {
				InventoryView view = player.getOpenInventory();

				if (view != null && view.getTopInventory() instanceof AnvilInventory) {
					AnvilInventory inv = (AnvilInventory) view.getTopInventory();

					ItemStack renamed = inv.getItem(0);
					if (renamed == null) {
						return;
					}

					ItemMeta meta = renamed.getItemMeta();
					String oldName = meta != null && meta.hasDisplayName() ? meta.getDisplayName() 
							: null;
					String newName = null;

					if (ProtocolLibrary.getProtocolManager().getMinecraftVersion()
							.getVersion().startsWith("1.6.")) {
						newName = (data == null || data.length < 1) ? "" : new String(data);
					} else {
						newName = (data == null || data.length < 1) ? ""
								: new String(data, Charsets.UTF_8);
					}

					ItemRenameEvent event = new ItemRenameEvent(player, view, oldName, newName);
					Bukkit.getPluginManager().callEvent(event);

					if (event.isCancelled() || event.getNewName() == null) {
						new UpdateAnvilSlots(player, view);
						e.setCancelled(true);
						return;
					}

					if (event.isResetted()) {
						newName = "";
					} else {
						newName = event.getNewName();
					}

					InputGuiUtils.setItemName(view, newName);
					new UpdateAnvilSlots(player, view);
					e.setCancelled(true);
				}
			}
		} else if (type.equals(Client.UPDATE_SIGN)) {
			int x = packet.getIntegers().read(0);
			int y = packet.getIntegers().read(1);
			int z = packet.getIntegers().read(2);

			String[] lines = packet.getStringArrays().read(0);

			/**
			 * We are using a custom input gui.
			 */
			if (iplayer.isGuiOpen() && iplayer.getCurrentGui() instanceof InputGuiSign) {
				/**
				 * Match the two locations.
				 */
				Location l = iplayer.getFakeBlockLocation();
				if (l == null || l.getBlockX() != x || l.getBlockY() != y || 
						l.getBlockZ() != z) {
					iplayer.setCancelled();
					return;
				}

				e.setCancelled(true);
				iplayer.setConfirmed(lines);
			}
		/**
		 * Close the gui once the player is doing something that can't happen when the gui open.
		 */
		} else if (iplayer.isGuiOpen() && iplayer.isCheckingPackets() && (
				type.equals(Client.CHAT) ||
				type.equals(Client.ARM_ANIMATION) ||
				type.equals(Client.BLOCK_PLACE) ||
				type.equals(Client.WINDOW_CLICK) ||
				type.equals(Client.USE_ENTITY) ||
				type.equals(Client.BLOCK_DIG) ||
				type.equals(Client.CLOSE_WINDOW) ||
				type.equals(Client.SET_CREATIVE_SLOT))) {
			iplayer.setCancelled();
		}

		/**
		 * Update the slots after someone clicked on it, this fixes glitches.
		 */
		if (type.equals(Client.WINDOW_CLICK)) {
			InventoryView view = player.getOpenInventory();

			if (view != null && view.getTopInventory() instanceof AnvilInventory) {
				int slot = packet.getIntegers().read(1);

				if (slot == 2) {
					new UpdateAnvilSlots(player, view);
				}
			}
		}
	}

	/**
	 * We update all the slots of the anvil inventory.
	 */
	private class UpdateAnvilSlots extends BukkitRunnable {
		private InventoryView view;
		private Player player;

		public UpdateAnvilSlots(Player player, InventoryView view) {
			this.runTaskLater(InputGuiPacketListener.this.plugin, 1L);
			this.view = view;
			this.player = player;
		}

		@Override
		public void run() {
			AnvilInventory inv = (AnvilInventory) this.view.getTopInventory();

			ItemStack item1 = inv.getItem(0);
			ItemStack item2 = inv.getItem(1);
			ItemStack item3 = InputGuiUtils.getResult(inv);

			PacketContainer packet1 = InputGuiUtils.getSetSlotPacket(this.view, 0, item1);
			PacketContainer packet2 = InputGuiUtils.getSetSlotPacket(this.view, 1, item2);
			PacketContainer packet3 = InputGuiUtils.getSetSlotPacket(this.view, 2, item3);

			try {
				ProtocolLibrary.getProtocolManager().sendServerPacket(this.player, packet1);
				ProtocolLibrary.getProtocolManager().sendServerPacket(this.player, packet2);
				ProtocolLibrary.getProtocolManager().sendServerPacket(this.player, packet3);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}