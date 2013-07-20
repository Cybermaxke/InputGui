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

import java.util.HashMap;
import java.util.Map;

import me.cybermaxke.inputgui.api.InputGui;
import me.cybermaxke.inputgui.api.InputGuiAPI;
import me.cybermaxke.inputgui.api.InputPlayer;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class InputGuiPlugin extends JavaPlugin implements InputGuiAPI, Listener {
	private Map<String, InputGuiPlayer> players = new HashMap<String, InputGuiPlayer>();

	@Override
	public void onEnable() {
		this.getServer().getPluginManager().registerEvents(this, this);
		new InputGuiPacketAdapter(this);
	}

	@Override
	public InputGuiPlayer getPlayer(Player player) {
		String name = player.getName();

		if (this.players.containsKey(name)) {
			return this.players.get(name);
		}

		InputGuiPlayer iplayer = new InputGuiPlayer(this, player);
		this.players.put(name, iplayer);

		return iplayer;
	}

	public void removePlayer(Player player) {
		this.players.remove(player.getName());
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		this.getPlayer(e.getPlayer());
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		this.removePlayer(e.getPlayer());
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		InputGuiPlayer player = this.getPlayer(e.getPlayer());
		if (player.isCheckingMovement()) {
			player.setCancelled();
		}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
		if (e.getAction() != Action.RIGHT_CLICK_AIR) {
			return;
		}

		InputGuiPlayer player = this.getPlayer(e.getPlayer());
		player.openGui(new InputGui() {

			@Override
			public String getDefaultText() {
				return "The Cake Is A Lie";
			}

			@Override
			public void onConfirm(InputPlayer player, String input) {
				player.getPlayer().sendMessage(this.getDefaultText() + " -> " + input);
			}

			@Override
			public void onCancel(InputPlayer player) {

			}

		});
	}
}