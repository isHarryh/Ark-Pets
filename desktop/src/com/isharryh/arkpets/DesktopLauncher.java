/** Copyright (c) 2022-2023, Harry Huang
 * At GPL-3.0 License
 */
package com.isharryh.arkpets;

import com.isharryh.arkpets.utils.ArgPending;
import com.isharryh.arkpets.utils.Logger;
import javafx.application.Application;


/** The entrance of the whole program, also the bootstrap for ArkHomeFX.
 * @see com.isharryh.arkpets.ArkHomeFX
 */
public class DesktopLauncher {
	public static void main (String[] args) {
		ArgPending.argCache = args;
		// Logger
		Logger.initialize("logs/desktop", 8);
		new ArgPending("--quiet", args) {
			protected void process(String command, String addition) {
				Logger.setLevel(Logger.ERROR);
			}
		};
		new ArgPending("--warn", args) {
			protected void process(String command, String addition) {
				Logger.setLevel(Logger.WARN);
			}
		};
		new ArgPending("--info", args) {
			protected void process(String command, String addition) {
				Logger.setLevel(Logger.INFO);
			}
		};
		new ArgPending("--debug", args) {
			protected void process(String command, String addition) {
				Logger.setLevel(Logger.DEBUG);
			}
		};

		// If requested to start the core app directly
		new ArgPending("--direct-start", args) {
			protected void process(String command, String addition) {
				EmbeddedLauncher.main(args);
				System.exit(0);
			}
		};
		// Java FX bootstrap
		Application.launch(ArkHomeFX.class, args);
		System.exit(0);
	}
}
