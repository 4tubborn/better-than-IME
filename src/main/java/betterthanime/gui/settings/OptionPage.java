package betterthanime.gui.settings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.options.ScreenOptions;
import net.minecraft.client.gui.options.components.BooleanOptionComponent;
import net.minecraft.client.gui.options.components.OptionsCategory;
import net.minecraft.client.gui.options.components.ToggleableOptionComponent;
import net.minecraft.client.gui.options.data.OptionsPage;
import net.minecraft.client.gui.options.data.OptionsPages;
import net.minecraft.core.item.Items;

public class OptionPage {
	// 保持这个引用永远不变，确保注册系统能找到它
	public static final OptionsPage btimeOptions = new OptionsPage("options.betterthanime.title", Items.PAPER.getDefaultStack());
	private static boolean isRegistered = false;

	public static void updatePageStructure() {
		Minecraft mc = Minecraft.getMinecraft();
		if (!(mc.gameSettings instanceof IOptions)) return;
		IOptions modSettings = (IOptions) mc.gameSettings;

		// 核心：清空旧组件列表，实现动态重构
		// 这里的 getComponents() 如果没有 public 权限，需要通过 Accessor 或在同包下操作
		btimeOptions.getComponents().clear();

		// 1. 基础设置分类
		OptionsCategory generalCat = new OptionsCategory("options.betterthanime.category.general");
		generalCat.
			withComponent(new BooleanOptionComponent(modSettings.btime$EnableCommandMode()))

		;

		// 2. StoreMode 按钮（带实时刷新逻辑）
		ToggleableOptionComponent<EStoreMode> storeModeBtn = new ToggleableOptionComponent<EStoreMode>(modSettings.btime$StoreMode()) {
			private EStoreMode lastMode = modSettings.btime$StoreMode().value;

			@Override
			public void onMouseClick(int mouseButton, int x, int y, int width, int relativeMouseX, int relativeMouseY) {
				// 仅执行赋值逻辑，不触发 init()
				super.onMouseClick(mouseButton, x, y, width, relativeMouseX, relativeMouseY);
			}

			@Override
			public void onMouseRelease(int mouseButton, int x, int y, int width, int relativeMouseX, int relativeMouseY) {
				super.onMouseRelease(mouseButton, x, y, width, relativeMouseX, relativeMouseY);

				// 检查值是否真的变了
				EStoreMode currentMode = modSettings.btime$StoreMode().value;
				if (currentMode != lastMode) {
					lastMode = currentMode;

					// 刷新结构
					updatePageStructure();

					// 延迟一帧刷新或直接刷新，因为此时鼠标已经松开
					if (Minecraft.getMinecraft().currentScreen instanceof ScreenOptions) {
						Minecraft.getMinecraft().currentScreen.init();
					}
				}
			}
		};

		generalCat.withComponent(storeModeBtn);
		btimeOptions.withComponent(generalCat);

		// 3. 动态分类：黑名单
		if (modSettings.btime$StoreMode().value == EStoreMode.BLACKLIST) {
			OptionsCategory blacklistCat = new OptionsCategory("options.betterthanime.category.blacklist");
			blacklistCat.withComponent(new BooleanOptionComponent(modSettings.btime$blacklistCommandMode()));
			btimeOptions.withComponent(blacklistCat);
		}

		// 4. 固定分类：指示器
		btimeOptions
			.withComponent(new OptionsCategory("options.betterthanime.category.imeStatusIndicator")
				.withComponent(new BooleanOptionComponent(modSettings.btime$autoAdsorb()))
			)
			.withComponent(new OptionsCategory("options.betterthanime.category.debug")
				.withComponent(new BooleanOptionComponent(modSettings.btime$mixinFullScreen()))
				.withComponent(new BooleanOptionComponent(modSettings.btime$renderPinyin()))
			)
		;
	}

	public static void register() {
		if (!isRegistered) {
			updatePageStructure();
			OptionsPages.register(btimeOptions);
			isRegistered = true;
		}
	}

	public static Screen getOptionsPage(Screen parent) {
		updatePageStructure();
		return new ScreenOptions(parent, btimeOptions);
	}
}
