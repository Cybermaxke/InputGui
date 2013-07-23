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
package me.cybermaxke.inputgui.api.event;

import org.bukkit.block.CommandBlock;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CommandBlockEditEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();

	private CommandBlock block;
	private String newCommand;
	private String oldCommand;
	private boolean cancel;

	public CommandBlockEditEvent(CommandBlock block, String oldCommand, String newCommand) {
		this.block = block;
		this.oldCommand = oldCommand;
		this.newCommand = newCommand;
	}

	/**
	 * Gets the command block.
	 * @return block
	 */
	public CommandBlock getBlock() {
		return this.block;
	}

	/**
	 * Gets the old command.
	 * @return command
	 */
	public String getOldCommand() {
		return this.oldCommand;
	}

	/**
	 * Gets the new command.
	 * @return command
	 */
	public String getNewCommand() {
		return this.newCommand;
	}

	/**
	 * Sets the new command.
	 * @param command
	 */
	public void setNewCommand(String command) {
		this.newCommand = command;
	}

	@Override
	public boolean isCancelled() {
		return this.cancel;
	}

	@Override
	public void setCancelled(boolean cancel) {
		this.cancel = cancel;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}