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
import com.comphenix.protocol.events.PacketContainer;

import me.cybermaxke.inputgui.api.InputGui;
import me.cybermaxke.inputgui.api.InputGuiBase;
import me.cybermaxke.inputgui.api.InputGuiSign;
import me.cybermaxke.inputgui.api.InputPlayer;

public class InputGuiPlayer implements InputPlayer {
	private boolean checkPackets;
	private boolean checkMove;

	private BukkitRunnable checkPacketsTask;
	private BukkitRunnable checkMoveTask;
	private Location fakeBlockLoc = null;

	private Plugin plugin;
	private Player player;
	private InputGuiBase<?> gui;

	public InputGuiPlayer(Plugin plugin, Player player) {
		this.plugin = plugin;
		this.player = player;
	}

	@Override
	public void openTileEditor(Block block) {
		this.setCancelled();

		try {
			PacketContainer packet = InputGuiUtils.getOpenGuiPacket(block.getLocation());
			ProtocolLibrary.getProtocolManager().sendServerPacket(this.player, packet);
		} catch (Exception e) {
			e.printStackTrace();
		}
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
	public InputGuiBase<?> getCurrentGui() {
		return this.gui;
	}

	@Override
	public boolean closeGui() {
		if (this.gui == null) {
			return false;
		}

		try {
			ProtocolLibrary.getProtocolManager().sendServerPacket(
					this.player, InputGuiUtils.getCloseGuiPacket());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}

	@Override
	public void openGui(InputGuiBase<?> gui) {
		this.openGui(gui, 17, 3);
	}

	@Override
	public void openGui(InputGuiBase<?> gui, int moveCheckTicks, int packetCheckTicks) {
		this.setCancelled();
		this.gui = gui;

		Location playerLoc = this.player.getLocation();
		Vector direction = playerLoc.getDirection().normalize().multiply(-5);
		this.fakeBlockLoc = playerLoc.add(direction);

		this.checkPackets = false;
		this.checkMove = false;

		if (gui instanceof InputGuiSign) {
			String[] lines = (String[]) gui.getDefaultText();

			try {
				ProtocolLibrary.getProtocolManager().sendServerPacket(this.player,
						InputGuiUtils.getSignPacket(this.fakeBlockLoc, lines));
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (gui instanceof InputGui) {
			this.player.sendBlockChange(this.fakeBlockLoc, Material.COMMAND, (byte) 0);

			String text = (String) gui.getDefaultText();
			try {
				ProtocolLibrary.getProtocolManager().sendServerPacket(this.player,
						InputGuiUtils.getTileDataPacket(this.fakeBlockLoc, text));
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			throw new IllegalArgumentException(
					"Unsupported gui: '" + gui.getClass().getSimpleName() + "'!");
		}

		try {
			ProtocolLibrary.getProtocolManager().sendServerPacket(this.player,
					InputGuiUtils.getOpenGuiPacket(this.fakeBlockLoc));
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

		this.checkPacketsTask.runTaskLater(this.plugin, packetCheckTicks);
		this.checkMoveTask.runTaskLater(this.plugin, moveCheckTicks);
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

	public void setConfirmed(Object input) {
		if (this.checkMoveTask != null) {
			this.checkMoveTask.cancel();
			this.checkMoveTask = null;
		}

		if (this.gui instanceof InputGuiSign) {
			((InputGuiSign) this.gui).onConfirm(this, (String[]) input);
		} else if (this.gui instanceof InputGui) {
			((InputGui) this.gui).onConfirm(this, (String) input);
		}

		this.gui = null;
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