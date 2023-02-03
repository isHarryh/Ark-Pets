/** Copyright (c) 2022-2023, Harry Huang
 * At GPL-3.0 License
 */
package com.isharryh.arkpets;

import javafx.application.Application;


/** The entrance of the whole program, also the bootstrap for ArkHomeFX.
 * @see com.isharryh.arkpets.ArkHomeFX
 */
public class DesktopLauncher {
	public static void main (String[] arg) {
		if (arg.length > 0 && arg[0].equals("--direct-start")) {
			EmbeddedLauncher.main(arg);
			return;
		}
		// Java FX bootstrap
		Application.launch(ArkHomeFX.class, arg);
		System.exit(0);
	}
}
