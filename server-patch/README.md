# server-patch — 服务器端编译 patch 留档

## 背景

线上核心(Nukkit-MOT FAP 版)更新后,`AvailableEntityIdentifiersPacket` 的字段由 `tag` 改名为
`identifiers`,旧版 wcpelib.jar 编译产物仍引用 `tag`,导致玩家登录时持续报:

```
java.lang.NoSuchFieldError: Class cn.nukkit.network.protocol.AvailableEntityIdentifiersPacket
does not have member field 'byte[] tag'
  at top.wcpe.wcpelib.nukkit.WcpeLibListener.listenerDataPacketSendEvent(WcpeLibListener.kt:26)
```

## 修复方式

- 仓库主源码(Kotlin)已修复:`wcpelib-nukkit/src/main/kotlin/top/wcpe/wcpelib/nukkit/WcpeLibListener.kt`
  使用 `packet.identifiers`。
- 服务器端无 Kotlin 编译环境,Gradle 本地构建也不可用,因此将 Kotlin 逻辑转写为等价 Java
  (`server-patch/WcpeLibListener.java`),在服务器上用 javac 编译
  (classpath = server.jar + wcpelib.jar + KotlinLib.jar),再 `jar uf` 替换 jar 内的
  `WcpeLibListener.class`。
- 2026-11-08 已替换全服 109 个 wcpelib.jar(验证:每个 jar 的 class 内均含 identifiers 字段引用)。
- ⚠️ 线上生效需重启实例:jar uf 只改磁盘文件,运行中 JVM 里仍是旧类。替换后未重启的实例
  会继续报错,直至下一次重启。

## 约定

- 本目录不参与 Gradle 构建(Java 版与 Kotlin 版同包同名类,进编译路径会冲突)。
- 未来若修改 Kotlin 主源码的同类逻辑,请同步更新本目录的 Java 转写版,保持两边一致。
