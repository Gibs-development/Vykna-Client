package com.client.graphics.interfaces.dropdown;

import com.client.Client;
import com.client.graphics.interfaces.MenuItem;
import com.client.graphics.interfaces.RSInterface;
import com.client.utilities.settings.Settings;

public class InterfaceTransparencyMenu implements MenuItem {
	private static final int[] VALUES = { 0, 10, 20, 30, 40, 50, 60 };

	@Override
	public void select(int optionSelected, RSInterface rsInterface) {
		Settings settings = Client.getUserSettings();
		if (settings == null) {
			return;
		}
		int index = Math.max(0, Math.min(optionSelected, VALUES.length - 1));
		settings.setRs3InterfaceTransparency(VALUES[index]);
	}
}
