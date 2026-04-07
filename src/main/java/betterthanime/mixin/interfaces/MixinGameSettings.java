package betterthanime.mixin.interfaces;

import betterthanime.gui.settings.EStoreMode;
import betterthanime.gui.settings.IOptions;
import net.minecraft.client.option.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = GameSettings.class, remap = false)
public class MixinGameSettings implements IOptions {
	@Unique
	private final GameSettings thisAs = (GameSettings)(Object)this;

	@Unique
	public final OptionBoolean enableCommandMode = new OptionBoolean(thisAs, "betterthanime.enableCommandMode", true);

	@Unique
	public final OptionEnum<EStoreMode> storeMode = new OptionEnum<>(thisAs, "betterthanime.storeMode", EStoreMode.class, EStoreMode.ALWAYS);

	@Unique
	public final OptionBoolean blacklistCommandMode = new OptionBoolean(thisAs, "betterthanime.blacklistCommandMode", true); // 默认包含 /

	@Unique
	public final OptionBoolean autoAdsorb = new OptionBoolean(thisAs, "betterthanime.autoAdsorb", true); // 默认开启

	public OptionBoolean btime$autoAdsorb() {
		return autoAdsorb;
	}

	@Override public OptionEnum<EStoreMode> btime$StoreMode() { return storeMode; }

	@Override
	public OptionBoolean btime$BlacklistCommandMode() { return  blacklistCommandMode; }


	@Override
	public OptionBoolean btime$EnableCommandMode() {
		return enableCommandMode;
	}

}
