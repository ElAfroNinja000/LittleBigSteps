plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.littlebigsteps.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.littlebigsteps.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    // Flavors de test manuel uniquement (pas un vrai modèle de distribution) :
    // permettent d'installer une variante "premium forcé" à côté de la variante
    // normale sur le même appareil, sans dépendre de Play Billing (§13 CLAUDE.md).
    flavorDimensions += "tier"
    productFlavors {
        create("free") {
            dimension = "tier"
            buildConfigField("boolean", "FORCE_PREMIUM", "false")
        }
        create("premium") {
            dimension = "tier"
            applicationIdSuffix = ".premiumtest"
            versionNameSuffix = "-premium-test"
            buildConfigField("boolean", "FORCE_PREMIUM", "true")
        }
    }

    // Le catalogue de défis (/content à la racine du repo) est embarqué tel quel
    // dans l'APK, en plus d'être servi par le CDN : il sert de contenu immédiat
    // au premier lancement (y compris hors-ligne) et lors d'un changement de
    // langue, sans attendre le réseau (voir BundledContentSource). Déclaré comme
    // dossier d'assets plutôt que recopié, pour n'avoir qu'une source de vérité.
    // Les chemins d'assets sont donc "fr/manifest.json", "en/drawing.json", etc.
    sourceSets {
        getByName("main") {
            assets.srcDir(rootProject.file("content"))
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    // AppCompatDelegate.setApplicationLocales() : changement de langue in-app
    // (Paramètres) — nécessite MainActivity: AppCompatActivity et un thème
    // dérivant de Theme.AppCompat (voir themes.xml), sinon ne fait rien.
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.billing.ktx)
    implementation(libs.posthog.android)
    implementation(libs.konfetti.compose)

    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.kotlinx.serialization)

    debugImplementation(libs.androidx.ui.tooling)
}
