plugins {
    id("java-library")
}
java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

dependencies {
    implementation(files("libs/java-cup-11b-runtime.jar"))
}