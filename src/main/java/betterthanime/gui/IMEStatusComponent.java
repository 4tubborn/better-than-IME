package betterthanime.gui;

import betterthanime.BetterThanIME;
import betterthanime.gui.settings.EStoreMode;
import betterthanime.gui.settings.IOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ButtonElement;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.hud.HudIngame;
import net.minecraft.client.gui.hud.component.ComponentAnchor;
import net.minecraft.client.gui.hud.component.HudComponentMovable;
import net.minecraft.client.gui.hud.component.layout.Layout;
import net.minecraft.core.lang.I18n;
import betterthanime.util.IMEUtil;
import org.spongepowered.asm.mixin.Unique;

public class IMEStatusComponent extends HudComponentMovable {

	private int stickerX = -1;
	private int stickerY = -1;

	public static int padding = 2;

	public static IMEStatusComponent INSTANCE;

	public static ButtonElement Button = new ButtonElement(999, 0, 0, 20, 20, "")
		.setTextures(
			BetterThanIME.MOD_ID+":gui/ime_status_component", // 正常状态纹理
			BetterThanIME.MOD_ID+":gui/ime_status_component_hover", // 悬停状态 (如果没有第二张图可以填一样的)
			BetterThanIME.MOD_ID+":gui/ime_status_component_disabled" // 禁用状态纹理 (可选)
		);

	public IMEStatusComponent(String key, Layout layout) {
		super(key, 20, 20, layout);
		INSTANCE = this;
	}

	@Unique
	private boolean isAutoAdsorbEnabled(Minecraft mc) {
		if (mc.gameSettings instanceof IOptions) {
			return ((IOptions) mc.gameSettings).btime$autoAdsorb().value;
		}
		return false;
	}

	@Override
	public boolean isVisible(Minecraft mc) {
		boolean inHudDesigner = mc.currentScreen instanceof net.minecraft.client.gui.ScreenHudDesigner ;
		return IMEUtil.enableIME || (inHudDesigner && !isAutoAdsorbEnabled(mc));
	}

	@Override
	public void render(Minecraft minecraft, HudIngame hudIngame, int i, int i1, float v) {

	}


	@Override
	public int getAnchorY(ComponentAnchor anchor) {
		// 严格抄自示例代码：使用动态获取的 getYSize 计算锚点位置
		return (int)(anchor.yPosition * (float)this.getYSize(Minecraft.getMinecraft()));
	}

	@Override
	public int getYSize(Minecraft mc) {
		// 严格抄自示例代码：在 Designer 中必须返回实际高度，否则虚线框和坐标计算会错位
		if (!this.isVisible(mc)) {
			return 0;
		}
		return 20; // 对应按钮高度
	}

	public void syncPosition(Minecraft mc, int sw, int sh) {
		if (isAutoAdsorbEnabled(mc)) {
			Button.xPosition = stickerX;
			Button.yPosition = stickerY;
		} else {
			// 否则回归：手动拖拽的位置
			Button.xPosition = getLayout().getComponentX(mc, this, sw);
			Button.yPosition = getLayout().getComponentY(mc, this, sh);
		}
	}

	// 在 IMEStatusComponent 类中添加


	/**
	 * 提供给外部调用的接口，用于设置吸附位置
	 */
	public void setStickerPosition(int x, int y) {
		this.stickerX = x;
		this.stickerY = y;
	}

	@Override
	public void renderPreview(Minecraft mc, Gui gui, net.minecraft.client.gui.hud.component.layout.Layout layout, int sw, int sh) {
		// 预览逻辑
		updateButtonStatus(mc, sw, sh);
		if (Button.visible) {
			Button.drawButton(mc, -999, -999);
		}
	}


	// 提取出一个公共方法供 Mixin 调用
	public void updateButtonStatus(Minecraft mc, int sw, int sh) {
		boolean inHudDesigner = mc.currentScreen instanceof net.minecraft.client.gui.ScreenHudDesigner;

		// 1. 同步显示开关：这是解决“关不掉”的关键
		Button.visible = this.isVisible(mc);

		// 2. 同步交互开关：在编辑界面禁用点击，防止意外切换
		Button.enabled = !inHudDesigner;

		if (Button.visible) {
			// 3. 同步坐标与文字
			syncPosition(mc, sw, sh);
			// 2. 动态获取本地化字符串
			I18n i18n = I18n.getInstance();
			String key = IMEUtil.getPhysicalInputStatus() ? "gui.betterthanime.ime_status.zh" : "gui.betterthanime.ime_status.en";
			Button.displayString = i18n.translateKey(key);
		}
	}
}
