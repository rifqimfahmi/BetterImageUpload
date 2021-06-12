object Versions {
    const val kotlin = "1.4.31"
    const val androidGradlePlugin = "4.2.1"

    const val androidxCoreKtx = "1.5.0"
    const val androidX = "1.3.0"
    const val androidxConstraintLayout = "2.0.4"
    const val androidxExifInterface = "1.3.2"

    const val glide = "4.12.0"
}

object Dependencies {
    const val androidGradlePlugin = "com.android.tools.build:gradle:${Versions.androidGradlePlugin}"
    const val kotlinGradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"

    const val kotlinStandardLibrary = "org.jetbrains.kotlin:kotlin-stdlib:${Versions.kotlin}"
    const val androidxCoreKtx = "androidx.core:core-ktx:${Versions.androidxCoreKtx}"
    const val androidxAppcompat = "androidx.appcompat:appcompat:${Versions.androidX}"
    const val googleMaterialDesign = "com.google.android.material:material:${Versions.androidX}"
    const val androidxConstraintLayout = "androidx.constraintlayout:constraintlayout:${Versions.androidxConstraintLayout}"
    const val androidxExifInterface = "androidx.exifinterface:exifinterface:${Versions.androidxExifInterface}"

    const val glide = "com.github.bumptech.glide:glide:${Versions.glide}"
    const val glideCompiler = "com.github.bumptech.glide:compiler:${Versions.glide}"

    // TODO: add later
//    testImplementation 'junit:junit:4.13.2'
//    androidTestImplementation 'androidx.test.ext:junit:1.1.2'
//    androidTestImplementation 'androidx.test.espresso:espresso-core:3.3.0'
}

object LocalDependencies {
    const val betterImageUpload = ":betterimageupload"
}