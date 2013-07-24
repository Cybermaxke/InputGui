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

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.InventoryView;

public class ItemRenameEvent extends InventoryEvent implements Cancellable {
	private static final HandlerList handlers = new HandlerList();

	private String newName;
	private String oldName;
	private boolean cancel;
	private boolean reset;

	public ItemRenameEvent(InventoryView view, String oldName, String newName) {
		super(view);
		this.newName = newName;
		this.oldName = oldName;
	}

	/**
	 * Gets the anvil inventory.
	 * @return inventory
	 */
	public AnvilInventory getInventory() {
		return (AnvilInventory) this.getView().getTopInventory();
	}

	/**
	 * Gets the old name.
	 * @return name
	 */
	public String getOldName() {
		return this.oldName;
	}

	/**
	 * Gets the new name.
	 * @return name
	 */
	public String getNewName() {
		return this.newName;
	}

	/**
	 * Sets the new name.
	 * @param name
	 */
	public void setNewName(String name) {
		this.newName = name;
	}

	/**
	 * Gets if the name should reset.
	 * @return reset
	 */
	public boolean isResetted() {
		return this.reset;
	}

	/**
	 * Sets if the name should reset.
	 * @param reset
	 */
	public void setResetted(boolean reset) {
		this.reset = reset;
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