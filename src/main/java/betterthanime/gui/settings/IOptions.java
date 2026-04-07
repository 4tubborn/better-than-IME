package betterthanime.gui.settings;

import net.minecraft.client.option.*;

public interface IOptions {
	// 这里的命名建议带上前缀防止与其他 Mod 冲突
	OptionBoolean btime$EnableCommandMode();

	OptionEnum<EStoreMode> btime$StoreMode();
	// 新增黑名单字符串选项 (用于存储以逗号分隔的命令)
	OptionBoolean btime$BlacklistCommandMode();
	OptionBoolean btime$autoAdsorb();
}
