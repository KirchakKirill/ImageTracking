package com.example.imagetracking

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.imagetracking.ui.theme.ImageTrackingTheme
import com.google.ar.core.AugmentedImage
import com.google.ar.core.TrackingState
import dev.romainguy.kotlin.math.Float3
import dev.romainguy.kotlin.math.Quaternion
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.ar.arcore.getUpdatedAugmentedImages
import io.github.sceneview.ar.node.AugmentedImageNode
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberModelInstance
import io.github.sceneview.rememberOnGestureListener

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ImageTrackingTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    innerPadding
                    MainScreen()
                }
            }
        }
    }
}

@Composable
fun MainScreen() {
    val augmentedImageDatabaseProvider = remember { AugmentedImageDatabaseProvider() }
    val statusLoadImage = remember { mutableStateOf(false) }
    val imageConverter = remember { ImageConverter() }
    val context = LocalContext.current
    val imageModel = remember { mutableStateOf(ImageModel()) }
    val enabledLoad = remember { mutableStateOf(true) }
    val updatedImage = remember { mutableStateOf<List<AugmentedImage>>(emptyList()) }
    val selected = remember { mutableStateOf<ModelNode?>(null) }

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                imageConverter.convertImageUriToBitmap(context, uri).let { bitmap ->
                    imageModel.value = ImageModel(
                        name = uri.lastPathSegment!!,
                        bitmap = bitmap,
                        imageWithInMeters = 0.225f
                    )
                    statusLoadImage.value = true
                }
            } else {
                imageConverter.convertImageToBitmap(context, "image.jpg").let { bitmap ->
                    imageModel.value = ImageModel(
                        name = "image.jpg",
                        bitmap = bitmap,
                        imageWithInMeters = 0.225f
                    )
                    statusLoadImage.value = true
                }
            }
        }

    if (statusLoadImage.value) {
        ARSceneView(
            modifier = Modifier.fillMaxSize(),
            sessionConfiguration = { session, config ->
                val database = augmentedImageDatabaseProvider.Instance(session)
                val imageDao = ImageDao(database)
                imageDao.insertImage(imageModel.value)

                config.augmentedImageDatabase = database
            },
            onSessionUpdated = { _, frame ->
                updatedImage.value = frame.getUpdatedAugmentedImages()
                    .filter {
                        it.trackingState == TrackingState.TRACKING
                    }
            }, onGestureListener = rememberOnGestureListener(
                onRotate = { detector, _, _ ->
                    selected.value?.let { node ->
                        val upAxis = Float3(y = 1f)
                        val sensitivity = 0.55f

                        val deltaDegrees = detector.rotation * sensitivity
                        val deltaRotation = Quaternion.fromAxisAngle(upAxis, -deltaDegrees)
                        node.quaternion *= deltaRotation
                    }
                },
                onScale = { detector, _, _ ->
                    val scale = 1 + (detector.scaleFactor - 1) * 0.1f
                    selected.value?.let { selected ->
                        selected.scale *= scale
                    }
                }
            )
        ) {
            updatedImage.value.forEach { augmentedImage ->
                AugmentedImageNode(
                    augmentedImage = augmentedImage) {

                    rememberModelInstance(
                        modelLoader,
                        "models/Space_Shuttle.glb"
                    )?.let { modelInstance ->
                        ModelNode(modelInstance = modelInstance, scaleToUnits = 0.2f, apply = { selected.value = this } )
                    }
                }
            }
        }
    } else {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                enabled = enabledLoad.value,
                onClick = {
                    enabledLoad.value = false
                    val mimeTypes = arrayOf("image/jpeg", "image/jpg", "image/png")
                    launcher.launch(mimeTypes)
                }) {
                if (enabledLoad.value) {
                    Text("Load image")
                } else {
                    Text("Loading...")
                }
            }
        }

    }
}
