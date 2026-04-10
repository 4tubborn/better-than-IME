package betterthanime.mixin.interfaces;

import betterthanime.gui.settings.EStoreMode;
import betterthanime.gui.settings.IOptions;
import net.minecraft.client.Minecraft;
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

	@Unique
	public final OptionBoolean renderPinyin = new OptionBoolean(thisAs, "betterthanime.renderPinyin", true); // 默认开启

	@Override
	public OptionBoolean btime$renderPinyin() {
		return renderPinyin;
	}

	@Unique
	public final OptionBoolean mixinFullScreen = new OptionBoolean(thisAs, "betterthanime.mixininFullScreen", true){
		@Override
		public void onUpdate() {
			super.onUpdate();
			// 关键：当用户点击按钮切换开关时，强制触发窗口更新
			Minecraft mc = Minecraft.getMinecraft();
			if (mc.gameWindow != null) {
				System.out.println("[BTA-IME] 检测到开关变化，正在重新计算窗口伪装状态...");
				mc.gameWindow.updateWindowState();
			}
		}
	};

	@Override
	public OptionBoolean btime$mixinFullScreen() {
		return mixinFullScreen;
	}

	@Override
	public OptionBoolean btime$autoAdsorb() {
		return autoAdsorb;
	}

	@Override public OptionEnum<EStoreMode> btime$StoreMode() { return storeMode; }

	@Override
	public OptionBoolean btime$blacklistCommandMode() { return  blacklistCommandMode; }


	@Override
	public OptionBoolean btime$EnableCommandMode() {
		return enableCommandMode;
	}

}
