dependencies {
    compileOnly(project(":extensions:reddit:stub"))
    compileOnly(project(":extensions:shared:library"))

    implementation(libs.hiddenapibypass)
}

android {
    defaultConfig {
        minSdk = 28
    }
}
