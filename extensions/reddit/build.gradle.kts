dependencies {
    compileOnly(project(":extensions:reddit:stub"))
    implementation(libs.hiddenapibypass)
}

android {
    defaultConfig {
        minSdk = 28
    }
}
