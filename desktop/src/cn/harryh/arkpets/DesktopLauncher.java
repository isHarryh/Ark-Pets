/** Copyright (c) 2022-2023, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets;

import cn.harryh.arkpets.utils.ArgPending;
import cn.harryh.arkpets.utils.Logger;
import javafx.application.Application;

import static cn.harryh.arkpets.Const.*;


/** The entrance of the whole program, also the bootstrap for ArkHomeFX.
 * @see ArkHomeFX
 */
public class DesktopLauncher {
	public static void main (String[] args) {
		ArgPending.argCache = args;
		// Logger
		Logger.initialize("logs/desktop", 8);
		Logger.info("System", "Entering the app of DesktopLauncher");
		new ArgPending(LogLevels.errorArg, args) {
			protected void process(String command, String addition) {
				Logger.setLevel(Logger.ERROR);
			}
		};
		new ArgPending(LogLevels.warnArg, args) {
			protected void process(String command, String addition) {
				Logger.setLevel(Logger.WARN);
			}
		};
		new ArgPending(LogLevels.infoArg, args) {
			protected void process(String command, String addition) {
				Logger.setLevel(Logger.INFO);
			}
		};
		new ArgPending(LogLevels.debugArg, args) {
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
		Logger.info("System", "Exited from DesktopLauncher successfully");
		System.exit(0);
	}
}
