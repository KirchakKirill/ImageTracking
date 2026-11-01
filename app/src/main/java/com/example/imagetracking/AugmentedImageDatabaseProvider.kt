package com.example.imagetracking

import com.google.ar.core.AugmentedImageDatabase
import com.google.ar.core.Session

class AugmentedImageDatabaseProvider {

    private var database: AugmentedImageDatabase? = null

    fun Instance(session: Session): AugmentedImageDatabase {
        return database ?: AugmentedImageDatabase(session).apply {
            database = this
        }
    }
}