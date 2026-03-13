package betterthanime.client.gui;

import betterthanime.BetterThanIME;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ButtonElement;
import net.minecraft.client.gui.ClickableLabelElement;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.hud.HudIngame;
import net.minecraft.client.gui.hud.component.ComponentAnchor;
import net.minecraft.client.gui.hud.component.HudComponentMovable;
import net.minecraft.client.gui.hud.component.layout.Layout;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.sound.SoundCategory;
import org.lwjgl.input.Mouse;
import betterthanime.util.IMEUtil;

public class IMEStatusComponent extends HudComponentMovable {
	public static ButtonElement Button = new ButtonElement(999, 0, 0, 20, 20, "")
		.setTextures(
			BetterThanIME.MOD_ID+":gui/ime_status_component", // 正常状态纹理
			BetterThanIME.MOD_ID+":gui/ime_status_component_hover", // 悬停状态 (如果没有第二张图可以填一样的)
			BetterThanIME.MOD_ID+":gui/ime_status_component_disabled" // 禁用状态纹理 (可选)
		);

	public IMEStatusComponent(String key, Layout layout) {
		super(key, 20, 20, layout);
	}

	@Override
	public boolean isVisible(Minecraft mc) {
		boolean inHudDesigner = mc.currentScreen instanceof net.minecraft.client.gui.ScreenHudDesigner;
		return IMEUtil.enableIME || inHudDesigner;
	}

	@Override
	public void render(Minecraft minecraft, HudIngame hudIngame, int i, int i1, float v) {

	}


	@Override
	public int getAnchorY(net.minecraft.client.gui.hud.component.ComponentAnchor anchor) {
		// 严格抄自示例代码：使用动态获取的 getYSize 计算锚点位置
		return (int)(anchor.yPosition * (float)this.getYSize(net.minecraft.client.Minecraft.getMinecraft()));
	}

	@Override
	public int getYSize(net.minecraft.client.Minecraft mc) {
		// 严格抄自示例代码：在 Designer 中必须返回实际高度，否则虚线框和坐标计算会错位
		if (!(mc.currentScreen instanceof net.minecraft.client.gui.ScreenHudDesigner) && !this.isVisible(mc)) {
			return 0;
		}
		return 20; // 对应按钮高度
	}

	public void syncPosition(net.minecraft.client.Minecraft mc, int sw, int sh) {
		// 严格参考 HarvestToolComponent 示例：
		// 直接取 getComponentX/Y，不要再做多余的减法
		Button.xPosition = getLayout().getComponentX(mc, this, sw);
		Button.yPosition = getLayout().getComponentY(mc, this, sh);
	}

	@Override
	public void renderPreview(net.minecraft.client.Minecraft mc, net.minecraft.client.gui.Gui gui, net.minecraft.client.gui.hud.component.layout.Layout layout, int sw, int sh) {
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
			String key = IMEUtil.getPhysicalInputStatus() ? "gui."+BetterThanIME.MOD_ID+".ime_status.zh" : "gui."+BetterThanIME.MOD_ID+".ime_status.en";
			Button.displayString = i18n.translateKey(key);
		}
	}
}
