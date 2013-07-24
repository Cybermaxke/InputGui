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

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.Packets.Server;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.nbt.NbtBase;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;

public class InputGuiUtils {
	public static final int PACKET_TILE_EDITOR_OPEN = 0x85;

	private InputGuiUtils() {

	}

	/**
	 * Gets a packet to close a tile gui.
	 * @return packet
	 */
	public static PacketContainer getCloseGuiPacket() {
		PacketContainer packet = new PacketContainer(Server.CLOSE_WINDOW);
		packet.getIntegers().write(0, 0);
		return packet;
	}

	/**
	 * Creates a tile data packet for the location and text.
	 * @param location
	 * @param text
	 * @return packet
	 */
	public static PacketContainer getTileDataPacket(Location location, String text) {
		PacketContainer packet = new PacketContainer(Server.TILE_ENTITY_DATA);

		List<NbtBase<?>> tags = new ArrayList<NbtBase<?>>();
		tags.add(NbtFactory.of("id", "Control"));
		tags.add(NbtFactory.of("Command", text == null ? "" : text));
		tags.add(NbtFactory.of("x", location.getBlockX()));
		tags.add(NbtFactory.of("y", location.getBlockY()));
		tags.add(NbtFactory.of("z", location.getBlockZ()));

		packet.getIntegers().write(0, location.getBlockX());
		packet.getIntegers().write(1, location.getBlockY());
		packet.getIntegers().write(2, location.getBlockZ());
		packet.getIntegers().write(3, 2);
		packet.getNbtModifier().write(0, NbtFactory.ofCompound("", tags));

		return packet;
	}

	/**
	 * Creates a tile editor packet for the location.
	 * @param location
	 * @return packet
	 */
	public static PacketContainer getOpenGuiPacket(Location location) {
		PacketContainer packet = new PacketContainer(PACKET_TILE_EDITOR_OPEN);

		packet.getIntegers().write(0, 0);
		packet.getIntegers().write(1, location.getBlockX());
		packet.getIntegers().write(2, location.getBlockY());
		packet.getIntegers().write(3, location.getBlockZ());

		return packet;
	}

	/**
	 * Gets a set slot packet for the view, slot and item.
	 * @param view
	 * @param slot
	 * @param item
	 * @return packet
	 */
	public static PacketContainer getSetSlotPacket(InventoryView view, int slot, ItemStack item) {
		PacketContainer packet = new PacketContainer(Server.SET_SLOT);

		packet.getIntegers().write(0, getWindowId(view));
		packet.getIntegers().write(1, slot);
		packet.getItemModifier().write(0, item);

		return packet;
	}

	/**
	 * Gets the window id for the view. This is not supported by the bukkit api.
	 * @param view
	 * @return id
	 */
	public static int getWindowId(InventoryView view) {
		try {
			Object container = view.getClass().getMethod("getHandle", new Class[] {})
					.invoke(view, new Object[] {});
			return container.getClass().getField("windowId").getInt(container);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return 0;
	}

	/**
	 * Gets the result item from the anvil inventory. This is not supported by the bukkit api.
	 * @param inventory
	 * @return item
	 */
	public static ItemStack getResult(AnvilInventory inventory) {
		try {
			Object handle = inventory.getClass().getMethod("getResultInventory", new Class[] {})
					.invoke(inventory, new Object[] {});
			Object item = handle.getClass().getMethod("getItem", int.class).invoke(handle, 0);
			return item == null ? null : MinecraftReflection.getBukkitItemStack(item);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}