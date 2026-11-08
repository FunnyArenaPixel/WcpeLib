package top.wcpe.wcpelib.nukkit;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.server.DataPacketSendEvent;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.AvailableEntityIdentifiersPacket;
import java.nio.ByteOrder;
import java.util.Map;

/**
 * WcpeLibListener 修复版(服务器端 javac patch 用 Java 转写版)。
 *
 * 说明:
 * - 仓库主源码是 Kotlin 版(src/main/kotlin/top/wcpe/wcpelib/nukkit/WcpeLibListener.kt),
 *   已用正确字段名 identifiers(旧版编译产物引用 tag 字段,核心更新后改名,导致
 *   NoSuchFieldError: AvailableEntityIdentifiersPacket.tag)。
 * - 服务器端没有 Kotlin 编译环境,本文件是 Kotlin 版的等价 Java 转写,
 *   2026-11-08 用服务器端 javac(classpath=server.jar+wcpelib.jar+KotlinLib.jar)
 *   编译并 jar uf 替换全服 109 个 wcpelib.jar,线上生效需重启实例(JVM 重新加载)。
 * - 本文件位于 server-patch/ 不参与 Gradle 编译(与 .kt 同包同名类,进编译路径会冲突),
 *   仅作为线上实际 patch 内容的源码留档。
 */
public class WcpeLibListener implements Listener {
    @EventHandler
    public void listenerDataPacketSendEvent(DataPacketSendEvent e) {
        var packet = e.getPacket();
        if (packet instanceof AvailableEntityIdentifiersPacket) {
            var p = (AvailableEntityIdentifiersPacket) packet;
            try {
                var tags = (CompoundTag) NBTIO.read(p.identifiers, ByteOrder.LITTLE_ENDIAN, true);
                var idList = tags.getList("idlist", CompoundTag.class);
                for (var entry : WcpeLib.getRegisterEntityInfoMap().entrySet()) {
                    var value = entry.getValue();
                    idList.add(
                        new CompoundTag()
                            .putBoolean("hasspawnegg", value.isHasSpawnEgg())
                            .putBoolean("summonable", value.isSummonAble())
                            .putString("id", value.getId())
                            .putString("bid", value.getBid())
                            .putInt("rid", value.getRid())
                    );
                }
                tags.putList("idlist", idList);
                p.identifiers = NBTIO.write(tags, ByteOrder.LITTLE_ENDIAN, true);
            } catch (Exception ignored) {
                // 与原 Kotlin 版一致:静默容错,不影响正常游戏
            }
        }
    }
}
