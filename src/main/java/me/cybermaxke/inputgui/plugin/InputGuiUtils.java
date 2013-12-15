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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.BukkitUnwrapper;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.nbt.NbtBase;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;

public class InputGuiUtils {
	private static String ALLOWED_CHARS;

	private InputGuiUtils() {

	}

	public static PacketContainer getSignPacket(Location location, String[] text) {
		PacketContainer packet = new PacketContainer(Server.UPDATE_SIGN);

		String[] text1 = new String[4];
		for (int i = 0; i < text1.length; i++) {
			if (i < text.length) {
				text1[i] = text[i];
			} else {
				text1[i] = "";
			}
		}

		packet.getIntegers().write(0, location.getBlockX());
		packet.getIntegers().write(1, location.getBlockY());
		packet.getIntegers().write(2, location.getBlockZ());
		packet.getStringArrays().write(0, text);

		return packet;
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
		/**
		 * Packet was added in 1.6.2
		 */
		if (ProtocolLibrary.getProtocolManager().getMinecraftVersion()
				.getVersion().startsWith("1.6.")) {
			PacketContainer packet = new PacketContainer(Server.OPEN_SIGN_ENTITY);

			packet.getIntegers().write(0, 0);
			packet.getIntegers().write(1, location.getBlockX());
			packet.getIntegers().write(2, location.getBlockY());
			packet.getIntegers().write(3, location.getBlockZ());

			return packet;
		/**
		 * In 1.7.2 and higher is the tile entity id removed.
		 * TODO: Check changes in ProtocolLib
		 */
		} else {
			PacketContainer packet = new PacketContainer(Server.OPEN_SIGN_ENTITY);

			packet.getIntegers().write(0, location.getBlockX());
			packet.getIntegers().write(1, location.getBlockY());
			packet.getIntegers().write(2, location.getBlockZ());

			return packet;
		}
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
			Object container = new BukkitUnwrapper().unwrapItem(view);
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

	/**
	 * Sets the item name in the inventory view.
	 * @param view
	 * @param name
	 */
	public static void setItemName(InventoryView view, String name) {
		if (!(view.getTopInventory() instanceof AnvilInventory)) {
			return;
		}

		Object container = new BukkitUnwrapper().unwrapItem(view);

		try {
			container.getClass()
					.getMethod("a", String.class)
					.invoke(container, getResult(name));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets a string which chars are filtered.
	 * @param string
	 * @return result
	 */
	public static String getResult(String string) {
		StringBuilder builder = new StringBuilder();

		for (char character : string.toCharArray()) {
			if (InputGuiUtils.isAllowedChatCharacter(character)) {
				builder.append(character);
			}
		}

		return builder.toString();
	}

	/**
	 * Gets if the character is valid.
	 * @param character
	 * @return valid
	 */
	public static boolean isAllowedChatCharacter(char character) {
		return InputGuiUtils.ALLOWED_CHARS.indexOf(character) >= 0 || character > ' ';
	}

	static {
		try {
			Method method = MinecraftReflection
					.getMinecraftClass("SharedConstants")
					.getDeclaredMethod("a", new Class[] {});
			method.setAccessible(true);

			InputGuiUtils.ALLOWED_CHARS = (String) method.invoke(null, new Object[] {});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}