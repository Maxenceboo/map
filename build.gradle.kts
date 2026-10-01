// Fichier racine : déclare les plugins sans les appliquer (ils sont appliqués dans :app).
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
}
