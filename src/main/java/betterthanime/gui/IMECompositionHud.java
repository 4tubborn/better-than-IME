package betterthanime.gui;

import betterthanime.util.IMEUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.TextFieldElement;
import org.lwjgl.opengl.GL11;

public class IMECompositionHud extends Gui {
	// 坐标存在组件内部
	private int x;
	private int y;

	public void render(Minecraft mc) {
		// 1. 实时扫描当前屏幕
		this.updatePos(mc);

		// 2. 如果没找到焦点，或者 IME 没开，直接返回
		if (!IMEUtil.enableIME) return;

		// 3. 获取拼音
		String comp = IMEUtil.getCompositionString();
		if (comp == null || comp.isEmpty()) return;

		// 4. 执行渲染 (使用内部保存的 x, y)
		renderText(mc, comp);
	}

	private void updatePos(Minecraft mc) {
		if (mc.currentScreen == null) return;

		// 遍历 Screen 里的所有按钮/组件，寻找 TextFieldElement
		for (Object obj : mc.currentScreen.buttons) {
			// 通过类名避开 BTA 7.3.4 的继承限制
			if (obj.getClass().getSimpleName().equals("TextFieldElement")) {
				TextFieldElement field = (TextFieldElement) obj;
				if (field.isFocused) {
					this.x = field.xPosition;
					this.y = field.yPosition;
					break;
				}
			}
		}
	}


	private void renderText(Minecraft mc, String text) {
		int padding = 2;
		int textWidth = mc.font.getStringWidth(text);
		int textHeight = 8;

		// 坐标计算：显示在输入框的正上方
		int drawX = this.x;
		int drawY = this.y - textHeight - (padding * 2) - 2;

		// 绘制背景（半透明黑）
		this.drawRect(drawX, drawY, drawX + textWidth + (padding * 2), drawY + textHeight + (padding * 2), 0xAA000000);
		// 绘制拼音（绿色）
		this.drawString(mc.font, text, drawX + padding, drawY + padding, 0x00FF00);
	}
}
