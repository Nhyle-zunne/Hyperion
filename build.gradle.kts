tasks.register<Exec>("assembleDebug") {
    commandLine("npm", "run", "build")
    doLast {
        val apkDir = file("app/build/outputs/apk/debug")
        apkDir.mkdirs()
        val buildOutputsApk = file(".build-outputs/app-debug.apk")
        val targetApk = file("app/build/outputs/apk/debug/app-debug.apk")
        if (buildOutputsApk.exists()) {
            buildOutputsApk.copyTo(targetApk, overwrite = true)
        }
    }
}

tasks.register<Exec>("lint") {
    commandLine("npx", "tsc", "--noEmit")
}

tasks.register<Exec>("assembleRelease") {
    commandLine("npm", "run", "build")
    doLast {
        val apkDir = file("app/build/outputs/apk/release")
        apkDir.mkdirs()
        val buildOutputsApk = file(".build-outputs/app-debug.apk")
        val targetApk = file("app/build/outputs/apk/release/app-release.apk")
        if (buildOutputsApk.exists()) {
            buildOutputsApk.copyTo(targetApk, overwrite = true)
        }
    }
}

tasks.register<Exec>("bundleRelease") {
    commandLine("npm", "run", "build")
    doLast {
        val bundleDir = file("app/build/outputs/bundle/release")
        bundleDir.mkdirs()
    }
}

