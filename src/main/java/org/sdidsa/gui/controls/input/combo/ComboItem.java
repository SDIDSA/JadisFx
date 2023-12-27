package org.sdidsa.gui.controls.input.combo;

import org.sdidsa.gui.controls.popup.context.items.MenuItem;

public interface ComboItem {
	String getDisplay();
	String getValue();
	MenuItem menuItem();
	boolean match(String other);
}
