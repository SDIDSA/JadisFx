package org.sdidsa.gui.window;

import java.awt.Dimension;

import org.json.JSONObject;
import org.sdidsa.gui.style.Styleable;
import org.sdidsa.gui.window.content.AppPreRoot;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleExpression;
import javafx.beans.value.ChangeListener;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

/**
 * Abstract class representing a page within a Window.
 * 
 * <p>
 * Pages are the building blocks of the application, each encapsulating a
 * specific functionality or view. They are displayed within the application
 * window and can have a defined minimum size.
 * </p>
 * 
 * <p>
 * Subclasses of this class should provide the specific implementation for the
 * page's content and behavior.
 * </p>
 * 
 * @author SDIDSA
 */
public abstract class Page extends StackPane implements Styleable {

	/**
	 * The default minimum size for the window associated with the page.
	 */
	public static final Dimension DEFAULT_WINDOW_MINSIZE = new Dimension(1280, 720);

	/**
	 * The associated window where the page is displayed.
	 */
	protected Window window;

	/**
	 * The minimum size of the page.
	 */
	protected Dimension minSize;

	/**
	 * Listener for changes in the padding property of the window.
	 */
	private ChangeListener<? super Boolean> onPaddingChange;

	/**
	 * Constructs a page with the specified window and minimum size.
	 * 
	 * @param window  The associated window.
	 * @param minSize The minimum size of the page.
	 */
	protected Page(Window window, Dimension minSize) {
		this.window = window;
		this.minSize = minSize;

		Rectangle clipBottom = new Rectangle();
		double arc = 13;
		clipBottom.setArcHeight(arc);
		clipBottom.setArcWidth(arc);

		Rectangle clipTop = new Rectangle();

		widthProperty().addListener((obs, ov, nv) -> {
			clipBottom.setWidth(nv.doubleValue());
			clipTop.setWidth(nv.doubleValue());

			setClip(Shape.union(clipBottom, clipTop));
		});

		heightProperty().addListener((obs, ov, nv) -> {
			clipTop.setHeight(nv.doubleValue() / 2 + arc);
			clipBottom.setHeight(nv.doubleValue() / 2);
			clipBottom.setY(nv.doubleValue() / 2);

			setClip(Shape.union(clipBottom, clipTop));
		});

		onPaddingChange = (obs, ov, nv) -> {
			if (nv.booleanValue()) {
				clipBottom.setArcHeight(arc);
				clipBottom.setArcWidth(arc);
			} else {
				clipBottom.setArcHeight(0);
				clipBottom.setArcWidth(0);
			}

			setClip(Shape.union(clipBottom, clipTop));
		};

		DoubleExpression height = window.heightProperty()
				.subtract(Bindings.when(window.getRoot().paddedProperty()).then(AppPreRoot.DEFAULT_PADDING * 2)
						.otherwise(0))
				.subtract(window.getAppBar().heightProperty()).subtract(window.getBorderWidth().multiply(2));

		DoubleExpression width = window
				.widthProperty().subtract(Bindings.when(window.getRoot().paddedProperty())
						.then(AppPreRoot.DEFAULT_PADDING * 2).otherwise(0))
				.subtract(window.getBorderWidth().multiply(2));

		setMinHeight(0);
		maxHeightProperty().bind(height);
		setMinWidth(0);
		maxWidthProperty().bind(width);
	}

	/**
	 * Constructs a page with the specified window and default minimum size.
	 * 
	 * @param window The associated window.
	 */
	protected Page(Window window) {
		this(window, DEFAULT_WINDOW_MINSIZE);
	}

	/**
	 * Gets the associated window.
	 * 
	 * @return The associated window.
	 */
	public Window getWindow() {
		return window;
	}

	/**
	 * Gets the JSON data associated with the specified key from the window.
	 * 
	 * @param key The key to retrieve JSON data.
	 * @return The JSON data associated with the key.
	 */
	public JSONObject getJsonData(String key) {
		return window.getJsonData(key);
	}

	/**
	 * Performs additional setup for the page.
	 * 
	 * <p>
	 * This method is called after the page is constructed. Subclasses can override
	 * this method to provide additional setup steps.
	 * </p>
	 */
	public void setup() {
		window.setMinSize(minSize);

		window.paddedProperty().addListener(onPaddingChange);
	}

	/**
	 * Performs cleanup or resource release when the page is being destroyed.
	 * 
	 * <p>
	 * This method is called when the page is being removed or replaced. Subclasses
	 * can override this method to perform cleanup tasks.
	 * </p>
	 */
	public void destroy() {
		window.paddedProperty().removeListener(onPaddingChange);
	}
}
