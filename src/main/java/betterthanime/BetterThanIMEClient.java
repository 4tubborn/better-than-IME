package betterthanime;

import betterthanime.gui.settings.OptionPage;
//import betterthanime.util.GLFWChecker;
import betterthanime.util.IMEUtil;
import net.minecraft.client.gui.options.data.OptionsPage;
import net.minecraft.client.gui.options.data.OptionsPages;
import turniplabs.halplibe.util.ClientStartEntrypoint;

public class BetterThanIMEClient implements ClientStartEntrypoint {


	// 这个方法由 HalpLibe 在游戏启动完成后调用
	@Override
	public void afterClientStart() {
		// 此时 Minecraft.getMinecraft().gameSettings 绝对已经存在
		// 执行注册，选项页才会出现在原版 Options 列表里
		IMEUtil.clientInitialize();
		OptionPage.register();
	}

	@Override
	public void beforeClientStart() {

		// 留空即可
	}
}
