package top.wcpe.wcpelib.nukkit;

import cn.nukkit.entity.EntityHuman;
import cn.nukkit.entity.custom.CustomEntity;
import cn.nukkit.entity.custom.EntityDefinition;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import lombok.Setter;

/**
 * @author NuStar
 * @since 2026/1/31 01:00
 */
@Setter
public class CustomDefineEntity extends EntityHuman implements CustomEntity {

    private EntityDefinition definition;

    public CustomDefineEntity(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public EntityDefinition getEntityDefinition() {
        return definition;
    }
}
