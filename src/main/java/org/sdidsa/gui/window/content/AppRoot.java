package org.sdidsa.gui.window.content;

import java.awt.Dimension;

import org.sdidsa.gui.factory.Backgrounds;
import org.sdidsa.gui.factory.Borders;
import org.sdidsa.gui.style.Style;
import org.sdidsa.gui.style.Styleable;
import org.sdidsa.gui.window.Page;
import org.sdidsa.gui.window.Window;
import org.sdidsa.gui.window.content.app_bar.AppBar;
import org.sdidsa.gui.window.content.app_bar.AppBarButton;
import org.sdidsa.gui.window.helpers.MoveResizeHelper;
import org.sdidsa.gui.window.helpers.TileHint.Tile;

import javafx.beans.property.ObjectProperty;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public class AppRoot extends BorderPane implements Styleable {
	public static final Color DEFAULT_WINDOW_BORDER = Color.gray(.5);

	private AppPreRoot parent;
	private MoveResizeHelper helper;

	private Paint borderFill;
	private double borderWidth;

	private AppBar bar;

	public AppRoot(Window window, AppPreRoot parent) {
		this.parent = parent;
		DropShadow ds = new DropShadow(15, Color.gray(0, .25));
		setEffect(ds);

		addEventFilter(MouseEvent.MOUSE_PRESSED, e -> requestFocus());

		setBorderFill(DEFAULT_WINDOW_BORDER, 1);

		helper = new MoveResizeHelper(window, parent, 5);

		addEventFilter(MouseEvent.MOUSE_MOVED, helper::onMove);

		addEventFilter(MouseEvent.MOUSE_PRESSED, helper::onPress);

		addEventFilter(MouseEvent.MOUSE_RELEASED, e -> helper.onRelease());

		setOnMouseDragged(helper::onDrag);

		setOnMouseClicked(helper::onClick);

		parent.paddedProperty().addListener((obs, ov, nv) -> {
			setFill(getBackground().getFills().get(0).getFill());
			setBorderFill(borderFill, borderWidth);
		});

		bar = new AppBar(window, helper);
		setTop(bar);
		
		applyStyle(window.getStyl());
	}
	
	public void addBarButton(AppBarButton button) {
		addBarButton(0, button);
	}
	
	public void addBarButton(int index, AppBarButton button) {
		bar.addButton(index, button);
	}

	public void setOnInfo(Runnable runnable) {
		bar.setOnInfo(runnable);
	}
	
	public AppBarButton getInfo() {
		return bar.getInfo();
	} 
	
	public void setFill(Paint fill) {
		setBackground(Backgrounds.make(fill, parent.isPadded() ? 10.0 : 0));
	}

	public void setBorderFill(Paint fill, double width) {
		borderFill = fill;
		borderWidth = width;
		setBorder(Borders.make(fill, 0,0));
	}

	private Page old = null;
	public void setContent(Page page) {
		if (old != null) {
			old.destroy();
		}

		page.setup();
		setCenter(page);
		
		old = page;
	}

	public void applyTile(Tile tile) {
		helper.applyTile(tile);
	}

	public void unTile() {
		helper.unTile();
	}

	public boolean isTiled() {
		return helper.isTiled();
	}

	public void setMinSize(Dimension d) {
		helper.setMinSize(d);
	}

	public AppBar getAppBar() {
		return bar;
	}

	public void setIcon(String image) {
		bar.setIcon(image);
	}

	@Override
	public void applyStyle(Style style) {
		setFill(style.getBackgroundSecondary());
	}

	@Override
	public void applyStyle(ObjectProperty<Style> style) {
		Styleable.bindStyle(this, style);
	}

}
