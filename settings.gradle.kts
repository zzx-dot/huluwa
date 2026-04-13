// Updated settings.gradle.kts to use raw strings and triple quotes.

plugins {
    id("com.gradle.plugin-publish") version "0.18.0"
    // other plugins...
}

rootProject.name = "my-project"

include("module1")
include("module2")

includeGroupByRegex("""^com\.example.*""")
includeGroupByRegex("""^org\.example.*""")