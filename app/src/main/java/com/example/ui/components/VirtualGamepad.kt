package com.example.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.XboxCardBorder
import kotlin.math.roundToInt
import kotlin.math.sqrt

@Composable
fun VirtualGamepadOverlay(
    webView: WebView?,
    opacity: Float = 0.55f,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    fun vibrate() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
            } else {
                @Suppress("DEPRECATION")
                val v = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v?.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
                }
            }
        } catch (ignored: Exception) {
        }
    }

    fun sendButtonEvent(buttonIndex: Int, pressed: Boolean) {
        if (pressed) vibrate()
        val script = """
            (function() {
                if (window.dispatchEvent) {
                    const event = new CustomEvent('virtual_gamepad_button', {
                        detail: { index: $buttonIndex, pressed: $pressed }
                    });
                    window.dispatchEvent(event);
                }
            })();
        """.trimIndent()
        webView?.evaluateJavascript(script, null)
    }

    fun sendAxisEvent(axisIndex: Int, value: Float) {
        val script = """
            (function() {
                if (window.dispatchEvent) {
                    const event = new CustomEvent('virtual_gamepad_axis', {
                        detail: { axis: $axisIndex, value: $value }
                    });
                    window.dispatchEvent(event);
                }
            })();
        """.trimIndent()
        webView?.evaluateJavascript(script, null)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("virtual_gamepad_overlay")
    ) {
        // Shoulder Triggers
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GamepadShoulderButton("LT", 6, opacity) { sendButtonEvent(6, it) }
                GamepadShoulderButton("LB", 4, opacity) { sendButtonEvent(4, it) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GamepadShoulderButton("RB", 5, opacity) { sendButtonEvent(5, it) }
                GamepadShoulderButton("RT", 7, opacity) { sendButtonEvent(7, it) }
            }
        }

        // Left Controls: Analog Thumbstick & D-Pad
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 20.dp, bottom = 20.dp)
        ) {
            VirtualAnalogStick(
                opacity = opacity,
                onMove = { x, y ->
                    sendAxisEvent(0, x)
                    sendAxisEvent(1, y)
                }
            )
        }

        // Center: Xbox Nexus & View / Menu Buttons
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 48.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GamepadSmallButton("⧉ View", 8, opacity) { sendButtonEvent(8, it) }
            GamepadXboxNexusButton(opacity) { sendButtonEvent(16, it) }
            GamepadSmallButton("☰ Menu", 9, opacity) { sendButtonEvent(9, it) }
        }

        // Right Controls: ABXY Buttons
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 20.dp)
        ) {
            VirtualAbxyPad(opacity = opacity) { index, pressed -> sendButtonEvent(index, pressed) }
        }
    }
}

@Composable
private fun VirtualAnalogStick(
    opacity: Float,
    onMove: (Float, Float) -> Unit
) {
    var stickX by remember { mutableFloatStateOf(0f) }
    var stickY by remember { mutableFloatStateOf(0f) }
    val maxRadius = 60f

    Box(
        modifier = Modifier
            .size(130.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = opacity * 0.7f))
            .border(1.5.dp, XboxCardBorder, CircleShape)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        stickX = 0f
                        stickY = 0f
                        onMove(0f, 0f)
                    },
                    onDragCancel = {
                        stickX = 0f
                        stickY = 0f
                        onMove(0f, 0f)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val newX = stickX + dragAmount.x
                        val newY = stickY + dragAmount.y
                        val dist = sqrt(newX * newX + newY * newY)
                        if (dist <= maxRadius) {
                            stickX = newX
                            stickY = newY
                        } else {
                            stickX = (newX / dist) * maxRadius
                            stickY = (newY / dist) * maxRadius
                        }
                        onMove(stickX / maxRadius, stickY / maxRadius)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Inner thumb knob
        Box(
            modifier = Modifier
                .offset { IntOffset(stickX.roundToInt(), stickY.roundToInt()) }
                .size(54.dp)
                .clip(CircleShape)
                .background(Color.DarkGray.copy(alpha = opacity))
                .border(2.dp, Color.White.copy(alpha = opacity * 0.9f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "L", color = Color.White.copy(alpha = opacity), fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

@Composable
private fun GamepadShoulderButton(
    label: String,
    buttonIndex: Int,
    opacity: Float,
    onStateChange: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .size(width = 64.dp, height = 36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = opacity * 0.8f))
            .border(1.dp, XboxCardBorder, RoundedCornerShape(8.dp))
            .pointerInput(buttonIndex) {
                detectTapGestures(
                    onPress = {
                        onStateChange(true)
                        tryAwaitRelease()
                        onStateChange(false)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color.White.copy(alpha = opacity),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun GamepadSmallButton(
    label: String,
    index: Int,
    opacity: Float,
    onButton: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color.Black.copy(alpha = opacity * 0.7f))
            .border(1.dp, XboxCardBorder, RoundedCornerShape(6.dp))
            .pointerInput(index) {
                detectTapGestures(
                    onPress = {
                        onButton(true)
                        tryAwaitRelease()
                        onButton(false)
                    }
                )
            }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, color = Color.White.copy(alpha = opacity), fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun GamepadXboxNexusButton(
    opacity: Float,
    onButton: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = opacity))
            .border(1.5.dp, Color(0xFF00E676).copy(alpha = opacity), CircleShape)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        onButton(true)
                        tryAwaitRelease()
                        onButton(false)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(text = "✕", color = Color(0xFF00E676).copy(alpha = opacity), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun VirtualAbxyPad(
    opacity: Float,
    onButton: (Int, Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .size(130.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = opacity * 0.6f))
            .border(1.dp, XboxCardBorder, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        AbxyButton(Modifier.align(Alignment.TopCenter), "Y", Color(0xFFFFD166), 3, opacity, onButton)
        AbxyButton(Modifier.align(Alignment.BottomCenter), "A", Color(0xFF00E676), 0, opacity, onButton)
        AbxyButton(Modifier.align(Alignment.CenterStart), "X", Color(0xFF118AB2), 2, opacity, onButton)
        AbxyButton(Modifier.align(Alignment.CenterEnd), "B", Color(0xFFEF476F), 1, opacity, onButton)
    }
}

@Composable
private fun AbxyButton(
    modifier: Modifier,
    label: String,
    accentColor: Color,
    index: Int,
    opacity: Float,
    onButton: (Int, Boolean) -> Unit
) {
    Box(
        modifier = modifier
            .size(42.dp)
            .padding(2.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = opacity * 0.85f))
            .border(1.5.dp, accentColor.copy(alpha = opacity), CircleShape)
            .pointerInput(index) {
                detectTapGestures(
                    onPress = {
                        onButton(index, true)
                        tryAwaitRelease()
                        onButton(index, false)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = accentColor.copy(alpha = opacity),
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
    }
}
