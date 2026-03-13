// 1. 设置项目名称
rootProject.name = providers.gradleProperty("mod_name").get()

// 2. 插件管理块
pluginManagement {
	repositories {
		mavenCentral()
		gradlePluginPortal()
		// 在 Gradle 9 中，推荐使用这种显式的写法
		maven {
			url = java.net.URI("https://maven.fabricmc.net")
		}
		maven {
			url = java.net.URI("https://maven.thesignalumproject.net/infrastructure")
		}
	}
}

// 3. 依赖解析管理（可选，如果你的 libs 找不到可以加在这里）
dependencyResolutionManagement {
	repositories {
		mavenCentral()
		maven {
			url = java.net.URI("https://maven.fabricmc.net")
		}
	}
}

// 4. 应用工具链插件（这里必须独立出来）
plugins {
	id("org.gradle.toolchains.foojay-resolver-convention").version("0.8.0")
}
