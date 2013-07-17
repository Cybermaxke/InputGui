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

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public final class InputPlayerManager {
	private Map<String, InputPlayer> players = new HashMap<String, InputPlayer>();
	private Plugin plugin;

	protected InputPlayerManager(Plugin plugin) {
		this.plugin = plugin;
	}

	protected void removePlayer(Player player) {
		this.players.remove(player.getName());
	}

	public InputPlayer getPlayer(Player player) {
		String name = player.getName();

		if (this.players.containsKey(name)) {
			return this.players.get(name);
		}

		InputPlayer player2 = new InputPlayer(this.plugin, player);
		this.players.put(name, player2);

		return player2;
	}
}