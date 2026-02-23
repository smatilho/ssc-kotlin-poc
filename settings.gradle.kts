pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "ssc-kotlin-poc"

include(":app")
include(":core:common")
include(":core:model")
include(":core:network")
include(":core:database")
include(":core:auth")
include(":core:payments")
include(":core:work")
include(":core:testing")

include(":feature:invite-auth")
include(":feature:membership")
include(":feature:lodge-catalog")
include(":feature:booking")
include(":feature:documents")
include(":feature:committee-admin")
include(":feature:profile")
