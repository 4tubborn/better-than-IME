package betterthanime.mixin;

import betterthanime.gui.IMEStatusComponent;
import betterthanime.util.IMEUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = Screen.class, remap = false)
public abstract class MixinScreen {
	@Shadow public List<ButtonElement> buttons;
	@Shadow protected Minecraft mc;

	/*@Inject(method = "render", at = @At("RETURN"))
	private void onRenderReturn(int mx, int my, float partialTick, CallbackInfo ci) {
		// 1. 遍历 Screen 现有的所有组件
		for (Object element : this.buttons) {
			// 2. 通过类名判断，完全避开编译期的继承检查
			if (element.getClass().getSimpleName().equals("TextFieldElement")) {
				try {
					// 3. 强行转换（运行时其实是可以通过的，只要包路径对）
					net.minecraft.client.gui.TextFieldElement field = (net.minecraft.client.gui.TextFieldElement) element;

					if (field.isFocused) {
						// 此时已经完全跳出了 TextFieldElement 的方法作用域
						// 执行 JNA 或 打印，绝对不会触发 TextFieldElement 的字节码报错

						if (betterthanime.util.IMEUtil.enableIME) {
							String comp = betterthanime.util.IMEUtil.getCompositionString();

							if (comp != null && !comp.isEmpty()) {
								// 在这里渲染你的绿色拼音提示
								// 位置：field.xPosition, field.yPosition
							}
						}
					}
				} catch (Exception ignored) {
					// 容错处理
				}
			}
		}
	}*/
}
