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

public interface InputGuiBase<T> {

	/**
	 * Gets the default text of this gui.
	 * @return text
	 */
	public T getDefaultText();

	/**
	 * Called when the player clicks the confirm button and the input text 
	 * is received from the client.
	 * @param player
	 * @param input
	 */
	public void onConfirm(InputPlayer player, T input);

	/**
	 * Called when the player clicks the cancel button or closes the gui.
	 * @param player
	 */
	public void onCancel(InputPlayer player);
}