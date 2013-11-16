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