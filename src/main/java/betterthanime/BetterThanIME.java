package betterthanime;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BetterThanIME implements ModInitializer {
	public static final String MOD_ID = "betterthanime";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	@Override
	public void onInitialize() {
		// 这是一个极端的做法，仅用于本地调试排查卡顿
		System.setErr(new java.io.PrintStream(new java.io.OutputStream() {
			public void write(int b) {}
		}));
		LOGGER.info("Better Than IME initialized.");
	}
}
