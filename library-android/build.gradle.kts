import java.io.FileInputStream
import java.util.Properties

version = LibraryAndroidCoordinates.LIBRARY_VERSION

plugins {
    id("com.android.library")
    kotlin("android")
    id("kotlin-android-extensions")
    id("maven-publish")
}

android {
    compileSdkVersion(Sdk.COMPILE_SDK_VERSION)

    defaultConfig {
        minSdkVersion(Sdk.MIN_SDK_VERSION)
        targetSdkVersion(Sdk.TARGET_SDK_VERSION)

        versionCode = LibraryAndroidCoordinates.LIBRARY_VERSION_CODE
        versionName = LibraryAndroidCoordinates.LIBRARY_VERSION

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    lintOptions {
        isWarningsAsErrors = true
        isAbortOnError = true
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk7"))

    implementation(SupportLibs.ANDROIDX_APPCOMPAT)
    implementation(SupportLibs.ANDROIDX_CORE_KTX)

    testImplementation(TestingLib.JUNIT)

    androidTestImplementation(AndroidTestingLib.ANDROIDX_TEST_RUNNER)
    androidTestImplementation(AndroidTestingLib.ANDROIDX_TEST_EXT_JUNIT)
}

val githubProperties = Properties()

val propertiesFile = rootProject.file("github.properties")
if (propertiesFile.exists()) {
    githubProperties.load(FileInputStream(rootProject.file("github.properties")))
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
            }
        }
        repositories {
            maven {
                name = "GitHubPackages"
                /** Configure path of your package repository on Github
                 *  Replace GITHUB_USERID with your/organisation Github userID and REPOSITORY with the repository name on GitHub
                 */
                url = uri("https://maven.pkg.github.com/alosdev/speechToText")
                credentials {
                    /**Create github.properties in root project folder file with gpr.usr=GITHUB_USER_ID  & gpr.key=PERSONAL_ACCESS_TOKEN
                     * OR
                     * Set environment variables
                     */
                    username = githubProperties["gpr.usr"] as String? ?: System.getenv("GH_USER")
                    password =
                        githubProperties["gpr.key"] as String? ?: System.getenv("GH_ACCESS_TOKEN")
                }
            }
        }
    }
}
