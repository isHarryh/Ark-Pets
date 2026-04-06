#version 120
/** Copyright (c) 2022-2024, Harry Huang
 * At GPL-3.0 License
 */

// Complex fragment shader for TwoColorPolygonBatch with outline and box shadow effect. (Low Quality Version)

varying vec2 v_texCoords;       // From VS
uniform sampler2D u_texture;    // From TCPB
uniform vec4 u_outlineColor;    // Required
uniform float u_outlineWidth;   // Required
uniform float u_outlineAlpha;   // Required
uniform vec4 u_shadowColor;     // Required
uniform ivec2 u_textureSize;    // Required
uniform float u_alpha;          // Required

const float c_alphaLow = 0.1;
const float c_alphaHigh = 0.9;
const float c_seamCoef = 0.6;
const float c_outlineOverstate = 10.0;
const float c_shadowOffset = 2.0;

const vec2 OFFSETS[16] = vec2[16](
    vec2(-1.000,  0.000),   // 0 (Left) - Actually 180, but starting here for comparison
    vec2(-0.924, -0.383),   // 22.5
    vec2(-0.707, -0.707),   // 45 (Down-Left)
    vec2(-0.383, -0.924),   // 67.5
    vec2( 0.000, -1.000),   // 90 (Down)
    vec2( 0.383, -0.924),   // 112.5
    vec2( 0.707, -0.707),   // 135 (Down-Right)
    vec2( 0.924, -0.383),   // 157.5
    vec2( 1.000,  0.000),   // 180 (Right) - Actually 0
    vec2( 0.924,  0.383),   // 202.5
    vec2( 0.707,  0.707),   // 225 (Up-Right)
    vec2( 0.383,  0.924),   // 247.5
    vec2( 0.000,  1.000),   // 270 (Up)
    vec2(-0.383,  0.924),   // 292.5
    vec2(-0.707,  0.707),   // 315 (Up-Left)
    vec2(-0.924,  0.383)    // 337.5
);

vec4 getSimpleSampleSum(float width) {
    vec4 sum = vec4(0.0);
    for (int i = 0; i < OFFSETS.length(); i++) {
        sum += texture2D(u_texture, v_texCoords + OFFSETS[i] * width / u_textureSize);;
    }
    return sum;
}

vec4 getOutlined(vec4 sum, vec4 texColor) {
    if (u_outlineColor.a > 0.0 && u_outlineWidth > 0.0 && u_outlineAlpha > 0.0) {
        vec2 relOutlineWidth = vec2(1.0) / u_textureSize * u_outlineWidth;
        vec4 neighbor = sum * c_outlineOverstate;
        if (neighbor.a > c_alphaLow) {
            texColor.rgb = u_outlineColor.rgb;
            texColor.a = min(1.0, neighbor.a) * u_outlineColor.a * u_outlineAlpha;
        }
    }
    return texColor;
}

vec4 getBoxShadow(vec4 texColor) {
    if (u_shadowColor.a > 0.0) {
        // Calculate the shadow offset in texture coordinate space
        vec2 shadowOffset = vec2(c_shadowOffset, -c_shadowOffset) / vec2(u_textureSize);

        // Sample the texture at the offset position to get shadow alpha
        vec4 shadowSample = texture2D(u_texture, v_texCoords - shadowOffset);

        // Only apply shadow where the current pixel is transparent/semi-transparent
        // and the offset source pixel is opaque
        if (shadowSample.a > c_alphaLow && texColor.a < c_alphaHigh) {
            float shadowAlpha = shadowSample.a * u_shadowColor.a;

            // Blend shadow with current texColor using "over" compositing
            // Shadow sits behind, so only fill where texColor is not already visible
            float blendFactor = shadowAlpha * (1.0 - texColor.a);
            texColor.rgb = mix(texColor.rgb, u_shadowColor.rgb, blendFactor);
            texColor.a   = texColor.a + blendFactor;
        }
    }
    return texColor;
}

void main() {
    vec4 texColor = texture2D(u_texture, v_texCoords);

    if (texColor.a < c_alphaHigh) {
        if (texColor.a < c_alphaLow) {
            // Outline effect
            texColor = getOutlined(getSimpleSampleSum(u_outlineWidth),texColor);
        }
        // Box shadow effect
        texColor = getBoxShadow(texColor);
    } else {
        // No effect
    }

    // Ultimate composing
    gl_FragColor = texColor;
    gl_FragColor.a *= u_alpha;
}
