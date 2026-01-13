package com.client.graphics.interfaces.dropdown;

import com.client.Client;
import com.client.graphics.interfaces.MenuItem;
import com.client.graphics.interfaces.RSInterface;
import com.client.utilities.settings.InterfaceStyle;
import com.client.utilities.settings.Settings;

public class Rs3EditModeMenu implements MenuItem {
	@Override
	public void select(int optionSelected, RSInterface rsInterface) {
		Settings settings = Client.getUserSettings();
		if (settings == null) {
			return;
		}
		boolean enabled = optionSelected == 1;
		if (settings.getInterfaceStyle() != InterfaceStyle.RS3) {
			enabled = false;
		}
		settings.setRs3EditMode(enabled);
		Client instance = Client.getInstance();
		if (instance != null) {
			instance.setRs3EditMode(enabled);
		}
	}
}
