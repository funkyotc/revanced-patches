dependencies {
    compileOnly(project(":extensions:shared:library"))
    compileOnly(libs.okhttp)
}

android {
    defaultConfig {
        minSdk = 26
    }
}
