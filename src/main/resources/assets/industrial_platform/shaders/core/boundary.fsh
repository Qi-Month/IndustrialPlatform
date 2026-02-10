#version 150

#moj_import <fog.glsl>

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
uniform float GameTime;

in vec4 vertexColor;
in vec2 surfaceUV;
in float vertexDistance;

out vec4 fragColor;

// ============================================================
// Barrier distortion shader
// Simplex noise + turbulence adapted for Minecraft
// ============================================================

vec3 mod289(vec3 x) {
    return x - floor(x * (1.0 / 289.0)) * 289.0;
}

vec4 mod289(vec4 x) {
    return x - floor(x * (1.0 / 289.0)) * 289.0;
}

vec4 permute(vec4 x) {
    return mod289(((x * 34.0) + 1.0) * x);
}

vec4 taylorInvSqrt(vec4 r) {
    return 1.79284291400159 - 0.85373472095314 * r;
}

float snoise(vec3 v) {
    const vec2 C = vec2(1.0 / 6.0, 1.0 / 3.0);
    const vec4 D = vec4(0.0, 0.5, 1.0, 2.0);

    vec3 i  = floor(v + dot(v, C.yyy));
    vec3 x0 = v - i + dot(i, C.xxx);

    vec3 g = step(x0.yzx, x0.xyz);
    vec3 l = 1.0 - g;
    vec3 i1 = min(g.xyz, l.zxy);
    vec3 i2 = max(g.xyz, l.zxy);

    vec3 x1 = x0 - i1 + C.xxx;
    vec3 x2 = x0 - i2 + C.yyy;
    vec3 x3 = x0 - D.yyy;

    i = mod289(i);
    vec4 p = permute(permute(permute(
        i.z + vec4(0.0, i1.z, i2.z, 1.0))
              + i.y + vec4(0.0, i1.y, i2.y, 1.0))
              + i.x + vec4(0.0, i1.x, i2.x, 1.0));

    float n_ = 0.142857142857;
    vec3 ns = n_ * D.wyz - D.xzx;

    vec4 j = p - 49.0 * floor(p * ns.z * ns.z);

    vec4 x_ = floor(j * ns.z);
    vec4 y_ = floor(j - 7.0 * x_);

    vec4 x = x_ * ns.x + ns.yyyy;
    vec4 y = y_ * ns.x + ns.yyyy;
    vec4 h = 1.0 - abs(x) - abs(y);

    vec4 b0 = vec4(x.xy, y.xy);
    vec4 b1 = vec4(x.zw, y.zw);

    vec4 s0 = floor(b0) * 2.0 + 1.0;
    vec4 s1 = floor(b1) * 2.0 + 1.0;
    vec4 sh = -step(h, vec4(0.0));

    vec4 a0 = b0.xzyw + s0.xzyw * sh.xxyy;
    vec4 a1 = b1.xzyw + s1.xzyw * sh.zzww;

    vec3 p0 = vec3(a0.xy, h.x);
    vec3 p1 = vec3(a0.zw, h.y);
    vec3 p2 = vec3(a1.xy, h.z);
    vec3 p3 = vec3(a1.zw, h.w);

    vec4 norm = taylorInvSqrt(vec4(dot(p0, p0), dot(p1, p1), dot(p2, p2), dot(p3, p3)));
    p0 *= norm.x;
    p1 *= norm.y;
    p2 *= norm.z;
    p3 *= norm.w;

    vec4 m = max(0.6 - vec4(dot(x0, x0), dot(x1, x1), dot(x2, x2), dot(x3, x3)), 0.0);
    m = m * m;
    return 42.0 * dot(m * m, vec4(dot(p0, x0), dot(p1, x1), dot(p2, x2), dot(p3, x3)));
}

float fBm(vec3 coords) {
    float lacunarity = 2.0;
    float smoothness = 2.0;
    float result = 0.0;
    float totalAmplitude = 0.0;

    for (int o = 0; o < 2; ++o) {
        float amplitude = pow(lacunarity, -smoothness * float(o));
        result += snoise(coords) * amplitude;
        totalAmplitude += amplitude;
        coords *= lacunarity;
    }

    return result / totalAmplitude;
}

float turbulence(vec3 coords) {
    float t = 0.0;
    float f = 1.0;

    for (int o = 0; o < 6; ++o) {
        t += abs(snoise(coords)) / f;
        coords *= 2.0;
        f *= 2.0;
    }

    return t;
}

void main() {
    float time = GameTime * 400.0;

    // surfaceUV: x = perimeter distance, y = world height
    vec2 uv = surfaceUV * 0.04;

    // --- Distortion offsets (the core barrier ripple) ---
    vec2 x_offset = vec2(123.456, 0.0);
    vec2 y_offset = vec2(349.234, 1704.2);

    vec2 fbm_scale = vec2(1.0);
    vec2 t_scale = vec2(5.0);

    float xo = fBm(vec3((uv + x_offset) * fbm_scale, time));
    float xt = turbulence(vec3((uv + x_offset) * t_scale, time));

    float yo = fBm(vec3((uv + y_offset) * fbm_scale, time));
    float yt = turbulence(vec3((uv + y_offset) * t_scale, time));

    // Combined distortion intensity
    float distX = xo + xt;
    float distY = yo + yt;
    float distortion = length(vec2(distX, distY));

    // --- Barrier surface pattern ---
    // Ripple rings emanating outward
    float ripple = sin(distortion * 12.0 - time * 3.0) * 0.5 + 0.5;
    ripple = pow(ripple, 2.0);

    // Flowing veins across the barrier
    vec2 veinUV = uv * 3.0 + vec2(distX, distY) * 0.1;
    float veins = turbulence(vec3(veinUV, time * 0.5));
    veins = pow(veins, 1.5) * 0.6;

    // Hex-like energy grid
    float grid = snoise(vec3(uv * 8.0 + vec2(distX, distY) * 0.05, time * 0.3));
    grid = smoothstep(0.3, 0.5, abs(grid)) * 0.4;

    // --- Combine layers ---
    float intensity = ripple * 0.5 + veins + grid;

    // Edge glow: brighter at distortion peaks
    float edgeGlow = smoothstep(0.5, 2.0, distortion) * 0.6;
    intensity += edgeGlow;

    // Pulse
    float pulse = sin(time * 2.0) * 0.15 + 0.85;
    intensity *= pulse;

    // --- Color ---
    vec3 baseCol = vertexColor.rgb;

    // Shift hue slightly at high distortion areas
    vec3 shiftCol = baseCol.gbr;
    vec3 col = mix(baseCol, shiftCol, smoothstep(0.8, 1.5, distortion) * 0.3);

    // Brighten at ripple peaks
    col = col * intensity + baseCol * edgeGlow * 0.3;

    // --- Alpha ---
    float alpha = vertexColor.a;
    alpha *= clamp(intensity * 0.7, 0.03, 1.0);
    alpha += edgeGlow * 0.15;
    alpha = clamp(alpha, 0.0, 0.85);

    vec4 color = vec4(col, alpha) * ColorModulator;
    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
}
