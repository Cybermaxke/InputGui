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

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;

public final class InputPlayer {
	private static final int PACKET_TILE_EDITOR_OPEN = 0x85;

	private boolean checkMove;
	private BukkitRunnable checkMoveTask;
	private Location fakeBlockLoc = null;
	private Player player;
	private Plugin plugin;

	protected InputPlayer(Plugin plugin, Player player) {
		this.player = player;
		this.plugin = plugin;
	}

	protected boolean isCheckingMovement() {
		return this.checkMove;
	}

	protected Location getFakeBlockLocation() {
		return this.fakeBlockLoc;
	}

	public Player getPlayer() {
		return this.player;
	}

	public boolean hasGuiOpened() {
		return this.fakeBlockLoc != null;
	}

	public void setGuiOpened(boolean open) {
		if (!open) {
			if (!this.hasGuiOpened()) {
				return;
			}

			Block b = this.fakeBlockLoc.getBlock();
			this.fakeBlockLoc = null;
			this.player.sendBlockChange(b.getLocation(), b.getTypeId(), b.getData());
			this.checkMove = false;

			if (this.checkMoveTask != null) {
				this.checkMoveTask.cancel();
				this.checkMoveTask = null;
			}
		} else {
			/**
			 * Getting the location hehind the player.
			 */
			Location l = this.player.getLocation();
			Vector d = l.getDirection().normalize().multiply(-2);
			this.fakeBlockLoc = l.add(d);
			this.openGui();

			if (this.checkMoveTask != null) {
				this.checkMoveTask.cancel();
			}

			this.checkMoveTask = new BukkitRunnable() {

				@Override
				public void run() {
					InputPlayer.this.checkMove = true;
					InputPlayer.this.checkMoveTask = null;
				}

			};

			this.checkMoveTask.runTaskLater(this.plugin, 3L);
		}
	}

	private boolean openGui() {
		if (!this.hasGuiOpened()) {
			return false;
		}

		/**
		 * Sending a fake block change or the sign edit gui will open instead of the command block gui.
		 */
		this.player.sendBlockChange(this.fakeBlockLoc, Material.COMMAND.getId(), (byte) 0);

		/**
		 * Sending the tile editor packet.
		 */
		try {
			ProtocolLibrary.getProtocolManager().sendServerPacket(this.player, this.getOpenGuiPacket(this.fakeBlockLoc));
		} catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}

	/**
	 * Creates a tile editor packet for the location.
	 * @param location
	 * @return packet
	 */
	private PacketContainer getOpenGuiPacket(Location location) {
		PacketContainer packet = new PacketContainer(PACKET_TILE_EDITOR_OPEN);

		packet.getIntegers().write(0, 0);
		packet.getIntegers().write(1, location.getBlockX());
		packet.getIntegers().write(2, location.getBlockY());
		packet.getIntegers().write(3, location.getBlockZ());

		return packet;
	}
}