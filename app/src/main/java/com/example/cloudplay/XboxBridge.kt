package com.example.cloudplay

import android.util.Log
import android.webkit.JavascriptInterface
import com.example.vpn.VpnManager
import org.json.JSONObject

class XboxBridge(private val controller: StreamController) {

    companion object {
        private const val TAG = "XboxBridge"

        fun getInjectionScript(
            clarityBoost: Boolean,
            targetCountry: String = "JP",
            targetIp: String = "138.199.21.239"
        ): String {
            return """
                (function() {
                    if (window.__cloudPlayInjected) {
                        if (window.__toggleClarityBoost) window.__toggleClarityBoost($clarityBoost);
                        return;
                    }
                    window.__cloudPlayInjected = true;

                    function notify(method, arg) {
                        try {
                            if (window.CloudPlayBridge && window.CloudPlayBridge[method]) {
                                window.CloudPlayBridge[method](arg);
                            }
                        } catch(e) {
                            console.error("CloudPlayBridge notification error: " + e);
                        }
                    }

                    const targetCountry = "$targetCountry";
                    const bypassIp = "$targetIp";

                    // ==========================================
                    // 1. Better-xCloud Smart Region Interceptor
                    // ==========================================
                    try {
                        const NATIVE_FETCH = window.fetch;
                        window.fetch = async function(request, init) {
                            let url = typeof request === 'string' ? request : (request ? request.url : '');
                            let isXboxApi = url.includes('/v2/login/user') || 
                                           url.includes('/sessions/cloud/play') || 
                                           url.includes('emerald.xboxservices.com') ||
                                           url.includes('xboxlive.com');

                            if (isXboxApi) {
                                if (typeof request === 'string') {
                                    init = init || {};
                                    let headers = new Headers(init.headers || {});
                                    headers.set('X-Forwarded-For', bypassIp);
                                    headers.set('X-Client-IP', bypassIp);
                                    headers.set('X-Real-IP', bypassIp);
                                    init.headers = headers;
                                } else if (request instanceof Request) {
                                    let headers = new Headers(request.headers);
                                    headers.set('X-Forwarded-For', bypassIp);
                                    headers.set('X-Client-IP', bypassIp);
                                    headers.set('X-Real-IP', bypassIp);
                                    request = new Request(request, { headers: headers });
                                }
                            }

                            let response;
                            try {
                                response = await NATIVE_FETCH(request, init);
                            } catch(err) {
                                throw err;
                            }

                            // Intercept /v2/login/user to spoof allowed regions to Japan East / Korea / US
                            if (url.includes('/v2/login/user') && response && response.ok) {
                                try {
                                    const clone = response.clone();
                                    const data = await clone.json();
                                    if (data && data.offeringSettings && data.offeringSettings.regions) {
                                        let targetName = targetCountry === 'KR' ? 'KOREACENTRAL' : 
                                                         (targetCountry === 'US' ? 'WESTUS' : 'JAPANEAST');
                                        
                                        let found = data.offeringSettings.regions.find(r => r.name.toUpperCase() === targetName);
                                        if (found) {
                                            found.isDefault = true;
                                            data.offeringSettings.regions.forEach(r => { if (r !== found) r.isDefault = false; });
                                        } else if (data.offeringSettings.regions.length > 0) {
                                            data.offeringSettings.regions[0].isDefault = true;
                                        }

                                        notify('onRegionBypassed', JSON.stringify({
                                            country: targetCountry,
                                            ip: bypassIp,
                                            targetRegion: targetName
                                        }));

                                        response.json = () => Promise.resolve(data);
                                        response.text = () => Promise.resolve(JSON.stringify(data));
                                    }
                                } catch(e) {
                                    console.warn("Could not patch login response", e);
                                }
                            }

                            return response;
                        };

                        // Patch XMLHttpRequest for older components
                        const origXhrOpen = XMLHttpRequest.prototype.open;
                        const origXhrSend = XMLHttpRequest.prototype.send;
                        XMLHttpRequest.prototype.open = function(method, url) {
                            this._url = url;
                            return origXhrOpen.apply(this, arguments);
                        };
                        XMLHttpRequest.prototype.send = function(data) {
                            if (this._url && (this._url.includes('/v2/login/user') || this._url.includes('xboxlive.com'))) {
                                try {
                                    this.setRequestHeader('X-Forwarded-For', bypassIp);
                                    this.setRequestHeader('X-Client-IP', bypassIp);
                                } catch(e) {}
                            }
                            return origXhrSend.apply(this, arguments);
                        };
                    } catch(e) {
                        console.error("Failed to setup fetch/xhr interceptor", e);
                    }

                    // ==========================================
                    // 2. Spoof Geolocation to Tokyo, Japan
                    // ==========================================
                    try {
                        const fakePosition = {
                            coords: {
                                latitude: 35.6762,
                                longitude: 139.6503,
                                accuracy: 15,
                                altitude: null,
                                altitudeAccuracy: null,
                                heading: null,
                                speed: null
                            },
                            timestamp: Date.now()
                        };
                        navigator.geolocation.getCurrentPosition = function(success) {
                            if (success) success(fakePosition);
                        };
                        navigator.geolocation.watchPosition = function(success) {
                            if (success) success(fakePosition);
                            return 1;
                        };
                    } catch(e) {}

                    // ==========================================
                    // 3. In-WebView Live IP & Region probe
                    // ==========================================
                    setTimeout(async function() {
                        try {
                            const res = await window.fetch('https://api.country.is/');
                            if (res.ok) {
                                const geo = await res.json();
                                notify('onWebViewIpDetected', JSON.stringify(geo));
                            }
                        } catch(e) {
                            try {
                                const res2 = await window.fetch('http://ipwho.is/');
                                if (res2.ok) {
                                    const geo2 = await res2.json();
                                    notify('onWebViewIpDetected', JSON.stringify(geo2));
                                }
                            } catch(err) {}
                        }
                    }, 1500);

                    // ==========================================
                    // 4. Hook RTCPeerConnection for Stream Detect
                    // ==========================================
                    try {
                        const OrigPeerConnection = window.RTCPeerConnection;
                        if (OrigPeerConnection) {
                            window.RTCPeerConnection = function(...args) {
                                const pc = new OrigPeerConnection(...args);
                                pc.addEventListener('iceconnectionstatechange', function() {
                                    if (pc.iceConnectionState === 'connected' || pc.iceConnectionState === 'completed') {
                                        notify('onStreamConnected', 'ice_connected');
                                    } else if (pc.iceConnectionState === 'disconnected' || pc.iceConnectionState === 'closed') {
                                        notify('onStreamDisconnected', 'ice_closed');
                                    }
                                });
                                pc.addEventListener('connectionstatechange', function() {
                                    if (pc.connectionState === 'connected') {
                                        notify('onStreamConnected', 'pc_connected');
                                    }
                                });
                                return pc;
                            };
                            window.RTCPeerConnection.prototype = OrigPeerConnection.prototype;
                        }
                    } catch(e) {
                        console.error("Failed to hook RTCPeerConnection: " + e);
                    }

                    // ==========================================
                    // 5. Monitor DOM & URL for Launch State
                    // ==========================================
                    let lastUrl = location.href;
                    setInterval(function() {
                        if (location.href !== lastUrl) {
                            lastUrl = location.href;
                            notify('onUrlChanged', location.href);
                            if (location.href.indexOf('/play/launch/') !== -1) {
                                notify('onGameLaunchInitiated', document.title || 'Xbox Game');
                            }
                        }
                    }, 800);

                    document.addEventListener('click', function(e) {
                        const target = e.target;
                        if (target) {
                            const btn = target.closest('button');
                            if (btn && (btn.innerText.includes('Play') || btn.getAttribute('data-testid') === 'play-button')) {
                                notify('onGameLaunchInitiated', document.title || 'Xbox Game');
                            }
                        }
                    }, true);

                    // ==========================================
                    // 6. Synthetic Gamepad Hook
                    // ==========================================
                    const virtualGamepadState = {
                        id: "Xbox 360 Controller (CloudPlay Gamepad)",
                        index: 0,
                        connected: true,
                        timestamp: Date.now(),
                        mapping: "standard",
                        axes: [0, 0, 0, 0],
                        buttons: Array.from({ length: 17 }, () => ({ pressed: false, touched: false, value: 0 }))
                    };

                    window.addEventListener('virtual_gamepad_button', function(e) {
                        const idx = e.detail.index;
                        const pressed = e.detail.pressed;
                        if (idx >= 0 && idx < 17) {
                            virtualGamepadState.buttons[idx].pressed = pressed;
                            virtualGamepadState.buttons[idx].touched = pressed;
                            virtualGamepadState.buttons[idx].value = pressed ? 1.0 : 0.0;
                            virtualGamepadState.timestamp = Date.now();
                        }
                    });

                    window.addEventListener('virtual_gamepad_axis', function(e) {
                        const axis = e.detail.axis;
                        const val = e.detail.value;
                        if (axis >= 0 && axis < 4) {
                            virtualGamepadState.axes[axis] = val;
                            virtualGamepadState.timestamp = Date.now();
                        }
                    });

                    try {
                        const origGetGamepads = navigator.getGamepads ? navigator.getGamepads.bind(navigator) : null;
                        navigator.getGamepads = function() {
                            const real = origGetGamepads ? origGetGamepads() : [];
                            const list = Array.from(real || []);
                            let hasConnected = false;
                            for (let i = 0; i < list.length; i++) {
                                if (list[i] && list[i].connected) {
                                    hasConnected = true;
                                    break;
                                }
                            }
                            if (!hasConnected) {
                                list[0] = virtualGamepadState;
                            }
                            return list;
                        };
                    } catch(e) {}

                    // ==========================================
                    // 7. Dynamic Clarity Boost Controller
                    // ==========================================
                    let clarityStyleEl = null;
                    window.__toggleClarityBoost = function(active) {
                        if (active) {
                            if (!clarityStyleEl) {
                                clarityStyleEl = document.createElement('style');
                                clarityStyleEl.id = 'cloudplay-clarity-boost';
                                clarityStyleEl.textContent = `
                                    video#stream-video, video[data-testid="stream-video"], video {
                                        filter: contrast(1.05) saturate(1.08) drop-shadow(0 0 1px rgba(0,0,0,0.4)) !important;
                                        image-rendering: -webkit-optimize-contrast !important;
                                    }
                                `;
                                document.head.appendChild(clarityStyleEl);
                            }
                        } else {
                            if (clarityStyleEl && clarityStyleEl.parentNode) {
                                clarityStyleEl.parentNode.removeChild(clarityStyleEl);
                                clarityStyleEl = null;
                            }
                        }
                    };

                    window.__toggleClarityBoost($clarityBoost);
                })();
            """.trimIndent()
        }
    }

    @JavascriptInterface
    fun onGameLaunchInitiated(gameTitle: String?) {
        Log.d(TAG, "Game launch detected: $gameTitle")
        controller.onGameLaunchInitiated(gameTitle ?: "Xbox Game")
    }

    @JavascriptInterface
    fun onStreamConnected(detail: String?) {
        Log.d(TAG, "Stream established: $detail")
        controller.onStreamConnected()
    }

    @JavascriptInterface
    fun onStreamDisconnected(detail: String?) {
        Log.d(TAG, "Stream ended: $detail")
        controller.onStreamDisconnected()
    }

    @JavascriptInterface
    fun onUrlChanged(url: String?) {
        url?.let { controller.onUrlChanged(it) }
    }

    @JavascriptInterface
    fun onRegionBypassed(info: String?) {
        Log.d(TAG, "Region bypassed: $info")
        VpnManager.onRegionBypassed(info ?: "")
    }

    @JavascriptInterface
    fun onWebViewIpDetected(jsonStr: String?) {
        if (jsonStr.isNullOrBlank()) return
        try {
            val json = JSONObject(jsonStr)
            val ip = json.optString("ip", "")
            val countryCode = json.optString("country", json.optString("country_code", "JP")).uppercase()
            val countryName = json.optString("country_name", json.optString("country", "Nhật Bản"))
            val city = json.optString("city", "")
            if (ip.isNotBlank()) {
                VpnManager.onWebViewIpDetected(ip, countryCode, countryName, city)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not parse onWebViewIpDetected JSON", e)
        }
    }

    @JavascriptInterface
    fun log(msg: String?) {
        Log.d("XboxBridgeJS", msg ?: "")
    }
}
