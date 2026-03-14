package betterthanime.gui.settings;

import net.minecraft.core.util.helper.ITranslatable;

public enum EStoreMode implements ITranslatable {
	NEVER(0),
	ALWAYS(1),
	BLACKLIST(2);

	private final int index;

	EStoreMode(int index) {
		this.index = index;
	}

	public int index() {
		return this.index;
	}

	// 必须实现的方法，用于界面显示翻译
	@Override
	public String getTranslationKey() {
		return this.name().toLowerCase();
	}

	public static EStoreMode get(int i) {
		if (i == 1) return ALWAYS;
		if (i == 2) return BLACKLIST;
		return NEVER;
	}
}
