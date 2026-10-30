plugins {
    id("buildlogic.gamelan-profiles")
}

group = providers.gradleProperty("gamelan.group")
    .orElse("tech.kayys.gamelan")
    .get()
version = providers.gradleProperty("gamelan.version")
    .orElse("0.1.0")
    .get()
