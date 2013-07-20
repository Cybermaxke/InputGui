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

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;

import me.cybermaxke.inputgui.api.InputGui;
import me.cybermaxke.inputgui.api.InputPlayer;

public class InputGuiPlayer implements InputPlayer {
	public static final ProtocolManager PROTOCOL_MANAGER = ProtocolLibrary.getProtocolManager();

	private boolean checkPackets;
	private boolean checkMove;
	private BukkitRunnable checkPacketsTask;
	private BukkitRunnable checkMoveTask;
	private Location fakeBlockLoc = null;

	private Plugin plugin;
	private Player player;
	private InputGui gui;

	public InputGuiPlayer(Plugin plugin, Player player) {
		this.plugin = plugin;
		this.player = player;
	}

	@Override
	public Player getPlayer() {
		return this.player;
	}

	@Override
	public boolean isGuiOpen() {
		return this.gui != null;
	}

	@Override
	public InputGui getCurrentGui() {
		return this.gui;
	}

	@Override
	public boolean closeGui() {
		if (this.gui == null) {
			return false;
		}

		try {
			PROTOCOL_MANAGER.sendServerPacket(this.player, InputGuiUtils.getCloseGuiPacket());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}

	@Override
	public void openGui(InputGui gui) {
		this.setCancelled();
		this.gui = gui;

		Location playerLoc = this.player.getLocation();
		Vector direction = playerLoc.getDirection().normalize().multiply(-5);
		this.fakeBlockLoc = playerLoc.add(direction);

		this.player.sendBlockChange(this.fakeBlockLoc, Material.COMMAND, (byte) 0);
		this.checkPackets = false;
		this.checkMove = false;

		try {
			PacketContainer packet1 = InputGuiUtils.getTileDataPacket(this.fakeBlockLoc, gui.getDefaultText());
			PacketContainer packet2 = InputGuiUtils.getOpenGuiPacket(this.fakeBlockLoc);

			PROTOCOL_MANAGER.sendServerPacket(this.player, packet1);
			PROTOCOL_MANAGER.sendServerPacket(this.player, packet2);
		} catch (Exception e) {
			e.printStackTrace();
		}

		this.checkPacketsTask = new BukkitRunnable() {

			@Override
			public void run() {
				InputGuiPlayer.this.checkPackets = true;
				InputGuiPlayer.this.checkPacketsTask = null;
			}

		};

		this.checkMoveTask = new BukkitRunnable() {

			@Override
			public void run() {
				InputGuiPlayer.this.checkMove = true;
				InputGuiPlayer.this.checkMoveTask = null;
			}

		};

		this.checkPacketsTask.runTaskLater(this.plugin, 3L);
		this.checkMoveTask.runTaskLater(this.plugin, 17L);
	}

	public void setCancelled() {
		if (this.checkMoveTask != null) {
			this.checkMoveTask.cancel();
			this.checkMoveTask = null;
		}

		if (this.gui != null) {
			this.gui.onCancel(this);
			this.gui = null;
		}

		if (this.fakeBlockLoc != null) {
			Block block = this.fakeBlockLoc.getBlock();
			this.player.sendBlockChange(this.fakeBlockLoc, block.getTypeId(), block.getData());
		}
	}

	public void setConfirmed(String input) {
		if (this.checkMoveTask != null) {
			this.checkMoveTask.cancel();
			this.checkMoveTask = null;
		}

		if (this.gui != null) {
			this.gui.onConfirm(this, input);
			this.gui = null;
		}
	}

	public boolean isCheckingMovement() {
		return this.checkMove;
	}

	public boolean isCheckingPackets() {
		return this.checkPackets;
	}

	public Location getFakeBlockLocation() {
		return this.fakeBlockLoc;
	}
}