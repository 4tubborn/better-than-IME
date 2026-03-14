package betterthanime.mixin.accessors;

import net.minecraft.client.gui.options.components.OptionsComponent;
import net.minecraft.client.gui.options.data.OptionsPage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import java.util.List;

@Mixin(value = OptionsPage.class, remap = false)
public interface OptionsPageAccessor {
	@Accessor("components")
	List<OptionsComponent> getComponents();
}
