/**
 * Copyright 2013-present Tatum Games, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tatumgames.tatumtech.android.ui.components.screens.scanner

import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.tatumgames.tatumtech.framework.android.logger.Logger
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

@Composable
fun CameraPreviewWithControls(
    onBarcodeDetected: (String) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }
    val handled = remember { AtomicBoolean(false) }

    DisposableEffect(Unit) {
        onDispose {
            analysisExecutor.shutdown()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PreviewView(ctx).also { previewView ->
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        bindCamera(
                            cameraProvider = cameraProvider,
                            lifecycleOwner = lifecycleOwner,
                            previewView = previewView,
                            analysisExecutor = analysisExecutor,
                            handled = handled,
                            onBarcodeDetected = onBarcodeDetected
                        )
                    }, ContextCompat.getMainExecutor(ctx))
                }
            }
        )
    }
}

@OptIn(ExperimentalGetImage::class)
private fun bindCamera(
    cameraProvider: ProcessCameraProvider,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    previewView: PreviewView,
    analysisExecutor: java.util.concurrent.ExecutorService,
    handled: AtomicBoolean,
    onBarcodeDetected: (String) -> Unit
) {
    try {
        cameraProvider.unbindAll()
        val preview = Preview.Builder().build()
        preview.setSurfaceProvider(previewView.surfaceProvider)
        val selector = CameraSelector.DEFAULT_BACK_CAMERA
        val scanner = BarcodeScanning.getClient()
        val analysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
        analysis.setAnalyzer(analysisExecutor) { imageProxy ->
            val mediaImage = imageProxy.image
            if (mediaImage == null || handled.get()) {
                imageProxy.close()
                return@setAnalyzer
            }
            val image = InputImage.fromMediaImage(
                mediaImage,
                imageProxy.imageInfo.rotationDegrees
            )
            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    val raw = barcodes
                        .firstOrNull { it.format == Barcode.FORMAT_QR_CODE }
                        ?.rawValue
                    if (!raw.isNullOrBlank() && handled.compareAndSet(false, true)) {
                        onBarcodeDetected(raw)
                    }
                }
                .addOnCompleteListener { imageProxy.close() }
        }
        cameraProvider.bindToLifecycle(lifecycleOwner, selector, preview, analysis)
    } catch (e: Exception) {
        e.printStackTrace()
        Logger.e("CameraPreview", "Failed to bind camera", e)
    }
}
