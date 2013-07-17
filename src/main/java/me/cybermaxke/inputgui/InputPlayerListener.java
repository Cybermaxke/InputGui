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

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class InputPlayerListener implements Listener {
	private InputGuiPlugin plugin;

	public InputPlayerListener(InputGuiPlugin plugin) {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		this.plugin.getPlayerManager().getPlayer(e.getPlayer());

		final Player player = e.getPlayer();
		new BukkitRunnable() {

			@Override
			public void run() {
				InputPlayerListener.this.plugin.getPlayerManager().getPlayer(player).setGuiOpened(true);
			}

		}.runTaskLater(this.plugin, 50L);
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		this.plugin.getPlayerManager().removePlayer(e.getPlayer());
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		InputPlayer player = this.plugin.getPlayerManager().getPlayer(e.getPlayer());
		if (player.isCheckingMovement()) {
			player.setGuiOpened(false);
		}
	}
}