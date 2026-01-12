package net.runelite.client.util;

import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.lang.reflect.Method;

/**
 * A class with OSX-specific functions to improve integration.
 */
@Slf4j
public class OSXUtil
{
	/**
	 * Enables the macOS native fullscreen if running on macOS.
	 *
	 * @param gui The gui to enable the fullscreen on.
	 */
	public static void tryEnableFullscreen(JFrame gui)
	{
		if (OSType.getOSType() != OSType.MacOS)
		{
			return;
		}

		try
		{
			Class<?> fsUtils = Class.forName("com.apple.eawt.FullScreenUtilities");
			Method setWindowCanFullScreen = fsUtils.getMethod(
					"setWindowCanFullScreen",
					java.awt.Window.class,
					boolean.class
			);
			setWindowCanFullScreen.invoke(null, gui, true);

			log.debug("Enabled fullscreen on macOS");
		}
		catch (Throwable ex)
		{
			log.debug("Failed to enable macOS fullscreen", ex);
		}
	}

	/**
	 * Requests the foreground in a macOS friendly way.
	 */
	public static void requestFocus()
	{
		if (OSType.getOSType() != OSType.MacOS)
		{
			return;
		}

		try
		{
			Class<?> appClass = Class.forName("com.apple.eawt.Application");
			Method getApplication = appClass.getMethod("getApplication");
			Object app = getApplication.invoke(null);

			Method requestForeground = appClass.getMethod("requestForeground", boolean.class);
			requestForeground.invoke(app, true);

			log.debug("Requested focus on macOS");
		}
		catch (Throwable ex)
		{
			log.debug("Failed to request macOS foreground", ex);
		}
	}
}
