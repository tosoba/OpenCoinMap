plugins { id("opencoinmap.android.feature") }

android { namespace = "com.trm.opencoinmap.feature.map" }

dependencies { implementation(libs.osmdroid) }
