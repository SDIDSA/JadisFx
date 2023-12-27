package org.sdidsa.gui.controls.alert;

import java.util.ArrayList;
import java.util.Random;

import org.sdidsa.gui.controls.SplineInterpolator;
import org.sdidsa.gui.factory.Backgrounds;
import org.sdidsa.gui.window.Page;
import org.sdidsa.gui.window.Window;

import javafx.animation.Animation.Status;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * StackPane-based class representing a flexible overlay that can be shown or
 * hidden with customizable animations.
 *
 * @author SDIDSA
 */
public class Overlay extends StackPane {
	private Pane owner;
	private Window window;

	private StackPane back;
	private VBox content;

	private Timeline show;
	private Timeline hide;

	private ArrayList<Runnable> onShown;
	private ArrayList<Runnable> onShowing;
	private ArrayList<Runnable> onHidden;
	private ArrayList<Runnable> onHiding;

	private boolean autoHide = true;

	/**
	 * Constructs an Overlay instance with the specified owner Pane and Window.
	 *
	 * @param owner  The owner Pane of the overlay.
	 * @param window The Window associated with the overlay.
	 */
	public Overlay(Pane owner, Window window) {
		this.owner = owner;
		this.window = window;

		onShown = new ArrayList<>();
		onShowing = new ArrayList<>();
		onHidden = new ArrayList<>();
		onHiding = new ArrayList<>();

		back = new StackPane();
		back.setBackground(Backgrounds.make(Color.gray(0, .8)));

		content = new VBox();
		content.setAlignment(Pos.CENTER);
		content.setPickOnBounds(false);

		content.maxWidthProperty().bind(owner.widthProperty());
		content.maxHeightProperty().bind(owner.heightProperty());

		addOnShowing(() -> {
			back.setCache(true);
			content.setCache(true);

			back.setCacheHint(CacheHint.SPEED);
			content.setCacheHint(CacheHint.SPEED);
		});

		addOnHiding(() -> {
			back.setCache(true);
			content.setCache(true);

			back.setCacheHint(CacheHint.SPEED);
			content.setCacheHint(CacheHint.SPEED);
		});

		show = new Timeline(
				new KeyFrame(Duration.seconds(.2), new KeyValue(back.opacityProperty(), 1, SplineInterpolator.EASE_OUT),
						new KeyValue(content.opacityProperty(), 1, SplineInterpolator.EASE_OUT),
						new KeyValue(content.scaleXProperty(), 1, SplineInterpolator.EASE_OUT),
						new KeyValue(content.scaleYProperty(), 1, SplineInterpolator.EASE_OUT)));

		show.setOnFinished(e -> {
			onShown.forEach(Runnable::run);

			back.setCache(false);
			content.setCache(false);
		});

		hide = new Timeline(
				new KeyFrame(Duration.seconds(.2), new KeyValue(back.opacityProperty(), 0, SplineInterpolator.EASE_IN),
						new KeyValue(content.opacityProperty(), 0, SplineInterpolator.EASE_IN),
						new KeyValue(content.scaleXProperty(), .8, SplineInterpolator.EASE_IN),
						new KeyValue(content.scaleYProperty(), .8, SplineInterpolator.EASE_IN)));

		back.setOpacity(0);
		content.setScaleX(.8);
		content.setScaleY(.8);

		back.setOnMouseClicked(e -> {
			if (autoHide)
				hide();
		});

		content.setOnMousePressed(e -> requestFocus());

		addEventFilter(KeyEvent.KEY_PRESSED, e -> {
			if (e.getCode().equals(KeyCode.ESCAPE) && autoHide) {
				hide();
			}
		});

		getChildren().addAll(back, content);
	}

	/**
	 * Constructs an Overlay instance with the specified owner Page.
	 *
	 * @param owner The owner Page of the overlay.
	 */
	public Overlay(Page owner) {
		this(owner, owner.getWindow());
	}

	public void setOwner(Pane owner) {
		this.owner = owner;

		content.maxWidthProperty().unbind();
		content.maxHeightProperty().unbind();

		content.maxWidthProperty().bind(owner.widthProperty());
		content.maxHeightProperty().bind(owner.heightProperty());
	}

	public void setAutoHide(boolean autoHide) {
		this.autoHide = autoHide;
	}

	public void addOnShown(Runnable onShown) {
		this.onShown.add(onShown);
	}

	public void addOnShowing(Runnable onShowing) {
		this.onShowing.add(onShowing);
	}

	public void addOnHidden(Runnable onHidden) {
		this.onHidden.add(onHidden);
	}

	public void addOnHiding(Runnable onHiding) {
		this.onHiding.add(onHiding);
	}

	public void addOnShown(int index, Runnable onShown) {
		this.onShown.add(index, onShown);
	}

	public void addContent(Node... cont) {
		this.content.getChildren().addAll(cont);
	}

	public void setContent(Node... cont) {
		this.content.getChildren().setAll(cont);
	}

	public void removeContent(Node... cont) {
		this.content.getChildren().removeAll(cont);
	}

	/**
	 * Shows the overlay with customizable animations and triggers associated
	 * actions.
	 */
	public void show() {
		if (getScene() != null && hide.getStatus() != Status.RUNNING) {
			hide();
			return;
		}
		hide.stop();
		if (!owner.getChildren().contains(this)) {
			last().setDisable(true);
			owner.getChildren().add(this);
		}
		show.playFromStart();
		onShowing.forEach(Runnable::run);
		requestFocus();
	}

	/**
	 * Hides the overlay with customizable animations and triggers associated
	 * actions.
	 */
	public void hide() {
		if (getScene() == null) {
			show();
			return;
		}
		show.stop();
		hide.setOnFinished(e -> {
			boolean hidingLast = this == last();
			owner.getChildren().remove(this);
			if (hidingLast) {
				last().setDisable(false);
			} else {
				this.setDisable(false);
			}

			owner.requestFocus();

			onHidden.forEach(Runnable::run);
		});
		hide.playFromStart();
		onHiding.forEach(Runnable::run);
	}

	/**
	 * Shows the overlay and enters a nested event loop until it is hidden.
	 */
	public void showAndWait() {
		show();
		Integer key = new Random().nextInt();
		Runnable exit = new Runnable() {
			@Override
			public void run() {
				Platform.exitNestedEventLoop(key, null);
				Platform.runLater(() -> onHidden.remove(this));
			}
		};
		onHidden.add(exit);
		Platform.enterNestedEventLoop(key);
	}

	private Node last() {
		return owner.getChildren().get(owner.getChildren().size() - 1);
	}

	public Pane getOwner() {
		return owner;
	}

	public Window getWindow() {
		return window;
	}
}
