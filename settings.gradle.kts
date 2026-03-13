pluginManagement {
	repositories {
		maven("https://maven.fabricmc.net/")
		mavenCentral()
		gradlePluginPortal()
	}
	plugins {
		id("org.gradle.toolchains.foojay-resolver-convention") version("0.8.0")
	}
}

dependencyResolutionManagement {
	versionCatalogs {
		create("libs") {
			from(files("gradle/libs.versions.toml"))
		}
	}
}

rootProject.name = "Better-Than-IME"
