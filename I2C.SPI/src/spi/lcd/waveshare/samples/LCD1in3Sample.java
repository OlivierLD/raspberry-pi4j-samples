package spi.lcd.waveshare.samples;

import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import spi.lcd.waveshare.LCD1in3;
import spi.lcd.waveshare.fonts.Font16;
import spi.lcd.waveshare.fonts.Font20;
import spi.lcd.waveshare.fonts.Font24;
import utils.StaticUtil;
import utils.TimeUtil;

import java.util.function.Consumer;

import static spi.lcd.waveshare.LCD1in3.DrawFill;

public class LCD1in3Sample {

	private static boolean k1 = false, k2 = false, k3 = false, jUp = false, jDown = false, jRight = false, jLeft = false, jPressed = false;
	private static Consumer<GpioPinDigitalStateChangeEvent> key1Consumer = (event) -> k1 = event.getState().isLow();
	private static Consumer<GpioPinDigitalStateChangeEvent> key2Consumer = (event) -> k2 = event.getState().isLow();
	private static Consumer<GpioPinDigitalStateChangeEvent> key3Consumer = (event) -> k3 = event.getState().isLow();

	private static Consumer<GpioPinDigitalStateChangeEvent> jUpConsumer = (event) -> jUp = event.getState().isLow();
	private static Consumer<GpioPinDigitalStateChangeEvent> jDownConsumer = (event) -> jDown = event.getState().isLow();
	private static Consumer<GpioPinDigitalStateChangeEvent> jLeftConsumer = (event) -> jLeft = event.getState().isLow();
	private static Consumer<GpioPinDigitalStateChangeEvent> jRightConsumer = (event) -> jRight = event.getState().isLow();
	private static Consumer<GpioPinDigitalStateChangeEvent> jPressedConsumer = (event) -> jPressed = event.getState().isLow();

	private static void drawKeyListenInit(LCD1in3 lcd) {
		lcd.GUIClear(LCD1in3.WHITE);

		/* Press */
		lcd.GUIDrawCircle(90, 120, 25, LCD1in3.RED, (jPressed ? DrawFill.DRAW_FILL_FULL : DrawFill.DRAW_FILL_EMPTY), LCD1in3.DOT_PIXEL_DFT);
		lcd.GUIDrawString(82, 112, "P", Font24.getInstance(), lcd.IMAGE_BACKGROUND, LCD1in3.BLUE);

		/* Left */
		lcd.GUIDrawRectangle(15, 95, 65, 145, LCD1in3.RED, (jLeft ? DrawFill.DRAW_FILL_FULL : DrawFill.DRAW_FILL_EMPTY), LCD1in3.DOT_PIXEL_DFT);
		lcd.GUIDrawString(32, 112, "L", Font24.getInstance(), lcd.IMAGE_BACKGROUND, LCD1in3.BLUE);

		/* Down */
		lcd.GUIDrawRectangle(65, 145, 115, 195, LCD1in3.RED, (jDown ? DrawFill.DRAW_FILL_FULL : DrawFill.DRAW_FILL_EMPTY), LCD1in3.DOT_PIXEL_DFT);
		lcd.GUIDrawString(82, 162, "D", Font24.getInstance(), lcd.IMAGE_BACKGROUND, LCD1in3.BLUE);

		/* Right */
		lcd.GUIDrawRectangle(115, 95, 165, 145, LCD1in3.RED, (jRight ? DrawFill.DRAW_FILL_FULL : DrawFill.DRAW_FILL_EMPTY), LCD1in3.DOT_PIXEL_DFT);
		lcd.GUIDrawString(132, 112, "R", Font24.getInstance(), lcd.IMAGE_BACKGROUND, LCD1in3.BLUE);

		/* Up */
		lcd.GUIDrawRectangle(65, 45, 115, 95, LCD1in3.RED, (jUp ? DrawFill.DRAW_FILL_FULL : DrawFill.DRAW_FILL_EMPTY), LCD1in3.DOT_PIXEL_DFT);
		lcd.GUIDrawString(82, 62, "U", Font24.getInstance(), lcd.IMAGE_BACKGROUND, LCD1in3.BLUE);

		/* Key1 */
		lcd.GUIDrawRectangle(185, 35, 235, 85, LCD1in3.RED, (k1 ? DrawFill.DRAW_FILL_FULL : DrawFill.DRAW_FILL_EMPTY), LCD1in3.DOT_PIXEL_DFT);
		lcd.GUIDrawString(195, 52, "K1", Font24.getInstance(), lcd.IMAGE_BACKGROUND, LCD1in3.BLUE);

		/* Key2	*/
		lcd.GUIDrawRectangle(185, 95, 235, 145, LCD1in3.RED, (k2 ? DrawFill.DRAW_FILL_FULL : DrawFill.DRAW_FILL_EMPTY), LCD1in3.DOT_PIXEL_DFT);
		lcd.GUIDrawString(195, 112, "K2", Font24.getInstance(), lcd.IMAGE_BACKGROUND, LCD1in3.BLUE);

		/* Key3 */
		lcd.GUIDrawRectangle(185, 155, 235, 205, LCD1in3.RED, (k3 ? DrawFill.DRAW_FILL_FULL : DrawFill.DRAW_FILL_EMPTY), LCD1in3.DOT_PIXEL_DFT);
		lcd.GUIDrawString(195, 172, "K3", Font24.getInstance(), lcd.IMAGE_BACKGROUND, LCD1in3.BLUE);

		if (!lcd.isSimulating()) {
			lcd.LCDDisplay();
		}
		// Loop here
		while (true) {
			if (k1 && k3) {
				break;
			}
			if (jUp) {
				lcd.GUIDrawRectangle(65, 45, 115, 95, LCD1in3.RED, DrawFill.DRAW_FILL_FULL, LCD1in3.DOT_PIXEL_DFT);
				lcd.GUIDrawString(82, 62, "U", Font24.getInstance(), lcd.IMAGE_BACKGROUND, LCD1in3.BLUE);
				lcd.LCDDisplayWindows(65, 45, 115, 95);
			} else {
				lcd.GUIClearWindows(65, 145, 115, 195, LCD1in3.WHITE);
				lcd.GUIDrawRectangle(65, 45, 115, 95, LCD1in3.WHITE, DrawFill.DRAW_FILL_FULL, LCD1in3.DOT_PIXEL_DFT);
				lcd.GUIDrawRectangle(65, 45, 115, 95, LCD1in3.RED, DrawFill.DRAW_FILL_EMPTY, LCD1in3.DOT_PIXEL_DFT);
				lcd.GUIDrawString(82, 62, "U", Font24.getInstance(), lcd.IMAGE_BACKGROUND, LCD1in3.BLUE);
				lcd.LCDDisplayWindows(65, 45, 115, 95);
			}
			if (jDown) {
				lcd.GUIDrawRectangle(65, 145, 115, 195, LCD1in3.RED, DrawFill.DRAW_FILL_FULL, LCD1in3.DOT_PIXEL_DFT);
				lcd.GUIDrawString(82, 162, "D", Font24.getInstance(), lcd.IMAGE_BACKGROUND, LCD1in3.BLUE);
				lcd.LCDDisplayWindows(65, 45, 115, 195);
			} else {
				lcd.GUIClearWindows(65, 145, 115, 195, LCD1in3.WHITE);
				lcd.GUIDrawRectangle(65, 45, 115, 195, LCD1in3.WHITE, DrawFill.DRAW_FILL_FULL, LCD1in3.DOT_PIXEL_DFT);
				lcd.GUIDrawRectangle(65, 45, 115, 195, LCD1in3.RED, DrawFill.DRAW_FILL_EMPTY, LCD1in3.DOT_PIXEL_DFT);
				lcd.GUIDrawString(82, 162, "D", Font24.getInstance(), lcd.IMAGE_BACKGROUND, LCD1in3.BLUE);
				lcd.LCDDisplayWindows(65, 45, 115, 195);
			}
			if (jLeft) {
				lcd.GUIDrawRectangle(15, 95, 65, 145, LCD1in3.RED, DrawFill.DRAW_FILL_FULL, LCD1in3.DOT_PIXEL_DFT);
				lcd.GUIDrawString(82, 112, "L", Font24.getInstance(), lcd.IMAGE_BACKGROUND, LCD1in3.BLUE);
				lcd.LCDDisplayWindows(15, 45, 65, 145);
			} else {
				lcd.GUIClearWindows(15, 95, 65, 145, LCD1in3.WHITE);
				lcd.GUIDrawRectangle(15, 45, 65, 145, LCD1in3.WHITE, DrawFill.DRAW_FILL_FULL, LCD1in3.DOT_PIXEL_DFT);
				lcd.GUIDrawRectangle(15, 45, 65, 145, LCD1in3.RED, DrawFill.DRAW_FILL_EMPTY, LCD1in3.DOT_PIXEL_DFT);
				lcd.GUIDrawString(82, 112, "L", Font24.getInstance(), lcd.IMAGE_BACKGROUND, LCD1in3.BLUE);
				lcd.LCDDisplayWindows(15, 45, 65, 145);
			}
			if (jRight) {
				lcd.GUIDrawRectangle(115, 95, 165, 145, LCD1in3.RED, DrawFill.DRAW_FILL_FULL, LCD1in3.DOT_PIXEL_DFT);
				lcd.GUIDrawString(132, 112, "R", Font24.getInstance(), lcd.IMAGE_BACKGROUND, LCD1in3.BLUE);
				lcd.LCDDisplayWindows(115, 45, 165, 145);
			} else {
				lcd.GUIClearWindows(115, 95, 165, 145, LCD1in3.WHITE);
				lcd.GUIDrawRectangle(115, 45, 165, 145, LCD1in3.WHITE, DrawFill.DRAW_FILL_FULL, LCD1in3.DOT_PIXEL_DFT);
				lcd.GUIDrawRectangle(115, 45, 165, 145, LCD1in3.RED, DrawFill.DRAW_FILL_EMPTY, LCD1in3.DOT_PIXEL_DFT);
				lcd.GUIDrawString(132, 112, "R", Font24.getInstance(), lcd.IMAGE_BACKGROUND, LCD1in3.BLUE);
				lcd.LCDDisplayWindows(115, 45, 165, 145);
			}
			if (jPressed) {
				lcd.GUIDrawCircle(90, 120, 25, LCD1in3.RED, DrawFill.DRAW_FILL_FULL, LCD1in3.DOT_PIXEL_DFT);
				lcd.GUIDrawString(82, 112, "P", Font24.getInstance(), lcd.IMAGE_BACKGROUND, LCD1in3.BLUE);
				lcd.LCDDisplayWindows(65, 95, 115, 145);
			} else {
				lcd.GUIDrawCircle(90, 120, 25, LCD1in3.WHITE, DrawFill.DRAW_FILL_FULL, LCD1in3.DOT_PIXEL_DFT);
				lcd.GUIDrawCircle(90, 120, 25, LCD1in3.RED, DrawFill.DRAW_FILL_EMPTY, LCD1in3.DOT_PIXEL_DFT);
				lcd.GUIDrawString(82, 112, "P", Font24.getInstance(), lcd.IMAGE_BACKGROUND, LCD1in3.BLUE);
				lcd.LCDDisplayWindows(65, 95, 115, 145);
			}
			if (k1) {
				lcd.GUIDrawRectangle(185, 35, 235, 85, LCD1in3.RED, DrawFill.DRAW_FILL_FULL, LCD1in3.DOT_PIXEL_DFT);
				lcd.GUIDrawString(195, 52, "K1", Font24.getInstance(), lcd.IMAGE_BACKGROUND, LCD1in3.BLUE);
				lcd.LCDDisplayWindows(185, 45, 235, 85);
			} else {
				lcd.GUIClearWindows(185, 35, 235, 85, LCD1in3.WHITE);
				lcd.GUIDrawRectangle(185, 45, 235, 85, LCD1in3.WHITE, DrawFill.DRAW_FILL_FULL, LCD1in3.DOT_PIXEL_DFT);
				lcd.GUIDrawRectangle(185, 45, 235, 85, LCD1in3.RED, DrawFill.DRAW_FILL_EMPTY, LCD1in3.DOT_PIXEL_DFT);
				lcd.GUIDrawString(195, 52, "K1", Font24.getInstance(), lcd.IMAGE_BACKGROUND, LCD1in3.BLUE);
				lcd.LCDDisplayWindows(185, 45, 235, 85);
			}
			if (k2) {
				lcd.GUIDrawRectangle(185, 95, 235, 145, LCD1in3.RED, DrawFill.DRAW_FILL_FULL, LCD1in3.DOT_PIXEL_DFT);
				lcd.GUIDrawString(195, 112, "K2", Font24.getInstance(), lcd.IMAGE_BACKGROUND, LCD1in3.BLUE);
				lcd.LCDDisplayWindows(185, 45, 235, 145);
			} else {
				lcd.GUIClearWindows(185, 95, 235, 145, LCD1in3.WHITE);
				lcd.GUIDrawRectangle(185, 45, 235, 145, LCD1in3.WHITE, DrawFill.DRAW_FILL_FULL, LCD1in3.DOT_PIXEL_DFT);
				lcd.GUIDrawRectangle(185, 45, 235, 145, LCD1in3.RED, DrawFill.DRAW_FILL_EMPTY, LCD1in3.DOT_PIXEL_DFT);
				lcd.GUIDrawString(195, 112, "K2", Font24.getInstance(), lcd.IMAGE_BACKGROUND, LCD1in3.BLUE);
				lcd.LCDDisplayWindows(185, 45, 235, 145);
			}
			if (k3) {
				lcd.GUIDrawRectangle(185, 155, 235, 205, LCD1in3.RED, DrawFill.DRAW_FILL_FULL, LCD1in3.DOT_PIXEL_DFT);
				lcd.GUIDrawString(195, 172, "K3", Font24.getInstance(), lcd.IMAGE_BACKGROUND, LCD1in3.BLUE);
				lcd.LCDDisplayWindows(185, 45, 235, 205);
			} else {
				lcd.GUIClearWindows(185, 155, 235, 205, LCD1in3.WHITE);
				lcd.GUIDrawRectangle(185, 45, 235, 205, LCD1in3.WHITE, DrawFill.DRAW_FILL_FULL, LCD1in3.DOT_PIXEL_DFT);
				lcd.GUIDrawRectangle(185, 45, 235, 205, LCD1in3.RED, DrawFill.DRAW_FILL_EMPTY, LCD1in3.DOT_PIXEL_DFT);
				lcd.GUIDrawString(195, 172, "K3", Font24.getInstance(), lcd.IMAGE_BACKGROUND, LCD1in3.BLUE);
				lcd.LCDDisplayWindows(185, 45, 235, 205);
			}
		}

	}

	public static void main(String... args) {

		LCD1in3 lcd = new LCD1in3(LCD1in3.HORIZONTAL, LCD1in3.BLUE);
		// Key listeners
		lcd.setKey1Consumer(key1Consumer);
		lcd.setKey2Consumer(key2Consumer);
		lcd.setKey3Consumer(key3Consumer);
		lcd.setJUpConsumer(jUpConsumer);
		lcd.setJDownConsumer(jDownConsumer);
		lcd.setJLeftConsumer(jLeftConsumer);
		lcd.setJRightConsumer(jRightConsumer);
		lcd.setJPressedConsumer(jPressedConsumer);

		lcd.GUINewImage(LCD1in3.IMAGE_RGB, LCD1in3.LCD_WIDTH, LCD1in3.LCD_HEIGHT, LCD1in3.IMAGE_ROTATE_0, LCD1in3.IMAGE_COLOR_POSITIVE);
		lcd.GUIClear(LCD1in3.WHITE);

//	lcd.setKey1Consumer((event) -> System.out.println(String.format(">> FROM CUSTOM CONSUMER, Key 1 from main: Pin: %s, State: %s", event.getPin().toString(), event.getState().toString())));

		TimeUtil.delay(500L);

		System.out.println("...Dots");

		/* 2.Drawing on the image */
		lcd.GUIDrawPoint(5, 10, LCD1in3.RED, LCD1in3.DotPixel.DOT_PIXEL_1X1, LCD1in3.DOT_STYLE_DFT);
		lcd.GUIDrawPoint(5, 25, LCD1in3.RED, LCD1in3.DotPixel.DOT_PIXEL_2X2, LCD1in3.DOT_STYLE_DFT);
		lcd.GUIDrawPoint(5, 40, LCD1in3.BLACK, LCD1in3.DotPixel.DOT_PIXEL_3X3, LCD1in3.DOT_STYLE_DFT);
		lcd.GUIDrawPoint(5, 55, LCD1in3.BLACK, LCD1in3.DotPixel.DOT_PIXEL_4X4, LCD1in3.DOT_STYLE_DFT);
		lcd.GUIDrawPoint(5, 70, LCD1in3.GREEN, LCD1in3.DotPixel.DOT_PIXEL_5X5, LCD1in3.DOT_STYLE_DFT);
		lcd.GUIDrawPoint(5, 85, LCD1in3.GREEN, LCD1in3.DotPixel.DOT_PIXEL_6X6, LCD1in3.DOT_STYLE_DFT);

		System.out.println("...Lines");

		lcd.GUIDrawLine(10, 10, 10, 200, LCD1in3.BROWN, LCD1in3.LineStyle.LINE_STYLE_SOLID, LCD1in3.DotPixel.DOT_PIXEL_2X2);
		lcd.GUIDrawLine(10, 10, 230, 230, LCD1in3.BROWN, LCD1in3.LineStyle.LINE_STYLE_SOLID, LCD1in3.DotPixel.DOT_PIXEL_2X2);

		lcd.GUIDrawLine(20, 10, 70, 60, LCD1in3.RED, LCD1in3.LineStyle.LINE_STYLE_SOLID, LCD1in3.DotPixel.DOT_PIXEL_1X1);
		lcd.GUIDrawLine(70, 10, 20, 60, LCD1in3.RED, LCD1in3.LineStyle.LINE_STYLE_SOLID, LCD1in3.DotPixel.DOT_PIXEL_1X1);
		lcd.GUIDrawLine(170, 15, 170, 55, LCD1in3.RED, LCD1in3.LineStyle.LINE_STYLE_DOTTED, LCD1in3.DotPixel.DOT_PIXEL_1X1);
		lcd.GUIDrawLine(150, 35, 190, 35, LCD1in3.RED, LCD1in3.LineStyle.LINE_STYLE_DOTTED, LCD1in3.DotPixel.DOT_PIXEL_1X1);

		System.out.println("...Rectangles");

		lcd.GUIDrawRectangle(20, 10, 70, 60, LCD1in3.BLUE, DrawFill.DRAW_FILL_EMPTY, LCD1in3.DotPixel.DOT_PIXEL_1X1);
		lcd.GUIDrawRectangle(85, 10, 130, 60, LCD1in3.BLUE, LCD1in3.DrawFill.DRAW_FILL_FULL, LCD1in3.DotPixel.DOT_PIXEL_1X1);

		System.out.println("...Circles");

		lcd.GUIDrawCircle(170, 35, 20, LCD1in3.GREEN, DrawFill.DRAW_FILL_EMPTY, LCD1in3.DotPixel.DOT_PIXEL_1X1);
		lcd.GUIDrawCircle(170, 85, 20, LCD1in3.GREEN, LCD1in3.DrawFill.DRAW_FILL_FULL, LCD1in3.DotPixel.DOT_PIXEL_1X1);

		System.out.println("...Strings");

		lcd.GUIDrawString(5, 70, "hello world", Font16.getInstance(), LCD1in3.WHITE, LCD1in3.BLACK);
		lcd.GUIDrawString(5, 90, "OlivSoft rocks!", Font20.getInstance(), LCD1in3.RED, LCD1in3.CYAN);
		lcd.GUIDrawString(5, 120, "WaveShare", Font24.getInstance(), LCD1in3.BLUE, lcd.IMAGE_BACKGROUND);

		System.out.println("Displaying...");
		if (!lcd.isSimulating()) {
			lcd.LCDDisplay();
		}

		// Wait for CR
		StaticUtil.userInput("Hit Return to move on...");

		System.out.println("Use the buttons. Press K1 + K3 to exit the loop");
		drawKeyListenInit(lcd);

//		while (true) {
//			drawKeyListenInit(lcd);
//			if (k1 && k3) {
//				break;
//			}
//		}

		System.out.println("Out of the loop");

		if (!lcd.isSimulating()) {
			lcd.LCDClear(LCD1in3.BLACK);
			lcd.LCDDisplay();
//		lcd.shutdown();
		}

		StaticUtil.userInput("Hit Return to finish.\n");

		if (!lcd.isSimulating()) {
			lcd.LCDClear(LCD1in3.BLACK);
			lcd.GUIClear(LCD1in3.BLACK);
			lcd.GUIDrawString(30, 70, "Bye!", Font24.getInstance(), LCD1in3.BLACK, LCD1in3.WHITE);
			lcd.LCDDisplay();
			TimeUtil.delay(1_000);

			lcd.LCDClear(LCD1in3.BLACK);
			lcd.GUIClear(LCD1in3.BLACK);
			lcd.LCDDisplay();

			TimeUtil.delay(1_000);
			lcd.shutdown();
		}
		System.out.println("End of Sample");
		System.out.println("Bye.");
	}
}
