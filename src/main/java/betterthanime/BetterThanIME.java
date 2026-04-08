package betterthanime;

import betterthanime.util.IMEUtil;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BetterThanIME implements ModInitializer {
	public static final String MOD_ID = "betterthanime";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	@Override
	public void onInitialize() {
		IMEUtil.initialize();
		LOGGER.info("Better Than IME initialized.");
	}
}
