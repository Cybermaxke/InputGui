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
package me.cybermaxke.inputgui.api;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public interface InputPlayer {

	/**
	 * Gets the player.
	 * @return player
	 */
	public Player getPlayer();

	/**
	 * Gets if the player has a gui open.
	 * @return open
	 */
	public boolean isGuiOpen();

	/**
	 * Gets the gui that is currently open.
	 * @return gui
	 */
	public InputGui getCurrentGui();

	/**
	 * Closes the current gui and gets there was one open.
	 * @return wasOpen
	 */
	public boolean closeGui();

	/**
	 * Opens the gui, with default check settings.
	 * @param gui
	 */
	public void openGui(InputGui gui);

	/**
	 * Opens the gui, with specific settings to check before
	 * the packets and movement should be checked. (Used to call the gui close.)
	 * @param gui
	 * @param moveCheckTicks
	 * @param packetCheckTicks
	 */
	public void openGui(InputGui gui, int moveCheckTicks, int packetCheckTicks);

	/**
	 * Open a command block gui or sign edit gui.
	 * @param block
	 */
	public void openTileEditor(Block block);
}