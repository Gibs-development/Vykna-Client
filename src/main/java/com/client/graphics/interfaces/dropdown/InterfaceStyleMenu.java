package com.client.graphics.interfaces.dropdown;

import com.client.Client;
import com.client.features.gameframe.ScreenMode;
import com.client.graphics.interfaces.MenuItem;
import com.client.graphics.interfaces.RSInterface;
import com.client.utilities.settings.InterfaceStyle;

public class InterfaceStyleMenu implements MenuItem {
	@Override
	public void select(int optionSelected, RSInterface rsInterface) {
		InterfaceStyle style = optionSelected == 1 ? InterfaceStyle.RS3 : InterfaceStyle.OSRS;
		if (Client.instance != null
				&& Client.getUserSettings() != null
				&& Client.getUserSettings().getInterfaceStyle() == InterfaceStyle.RS3
				&& style == InterfaceStyle.OSRS) {
			Client.instance.persistRs3ViewportBounds();
		}
		Client.getUserSettings().setInterfaceStyle(style);
		if (style == InterfaceStyle.RS3) {
			Client.instance.setGameMode(ScreenMode.RESIZABLE, true);
		} else if (Client.instance != null) {
			Client.instance.resetRs3InventoryLayout();
		}
	}
}
