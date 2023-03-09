/** Copyright (c) 2022-2023, Harry Huang
 * At GPL-3.0 License
 */
package com.isharryh.arkpets;

import com.isharryh.arkpets.utils.ArgPending;
import javafx.application.Application;


/** The entrance of the whole program, also the bootstrap for ArkHomeFX.
 * @see com.isharryh.arkpets.ArkHomeFX
 */
public class DesktopLauncher {
	public static void main (String[] args) {
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
