/** Copyright (c) 2022-2024, Harry Huang
 * At GPL-3.0 License
 */

// Gap Seaming and Ouline Effect Fragment Shader for TwoColorPolygonBatch.

#version 120

varying vec2 v_texCoords;       // From VS
uniform sampler2D u_texture;    // From TCPB
uniform vec4 u_outlineColor;    // Required
uniform float u_outlineWidth;   // Required
uniform float u_outlineAlpha;   // Required
uniform vec4 u_shadowColor;     // Required
uniform ivec2 u_textureSize;    // Required
uniform float u_alpha;          // Required

const float c_alphaLv0 = 0.1;
const float c_alphaLv1 = 0.4;
const float c_alphaLv2 = 0.9;
const float c_seamCoef = 0.6;
const float c_outlineOverstate = 10.0;
const float c_shadowOffset = 2.0;

const float gaussianNeighborKernel[25] = float[25] (
0.0035434, 0.0158805, 0.0261825, 0.0158805, 0.0035434,
0.0158805, 0.0711714, 0.1173418, 0.0711714, 0.0158805,
0.0261825, 0.1173418, 0.0      , 0.1173418, 0.0261825,
0.0158805, 0.0711714, 0.1173418, 0.0711714, 0.0158805,
0.0035434, 0.0158805, 0.0261825, 0.0158805, 0.0035434
);

vec4[24] getSimpleNeighbors(vec2 unitLength) {
    vec4 neighbors[24];
    int i = 0;
    for (int y = -2; y <= 2; y++) {
        for (int x = -2; x <= 2; x++) {
            if (!(y == 0 && x == 0)) {
                vec2 offset = vec2(x, y) * unitLength;
                neighbors[i] = texture2D(u_texture, v_texCoords + offset);
                i++;
            }
        }
    }
    return neighbors;
}

vec4[24] getGaussianNeighbors(vec2 unitLength) {
    vec4 neighbors[24];
    int ni = 0;
    int ki = 0;
    for (int y = -2; y <= 2; y++) {
        for (int x = -2; x <= 2; x++) {
            if (!(y == 0 && x == 0)) {
                vec2 offset = vec2(x, y) * unitLength;
                neighbors[ni] = texture2D(u_texture, v_texCoords + offset) * gaussianNeighborKernel[ki];
                ni++;
            }
            ki++;
        }
    }
    return neighbors;
}

vec4 getGaussianNeighborsSum(vec2 unitLength) {
    vec4[24] neighbors = getGaussianNeighbors(unitLength);
    vec4 sum = vec4(0.0);
    for (int i = 0; i < neighbors.length(); i++) {
        sum += neighbors[i];
    }
    return sum;
}

vec4 getOutlined() {
    vec4 texColor = texture2D(u_texture, v_texCoords);
    if (u_outlineColor.a > 0.0 && u_outlineWidth > 0.0 && u_outlineAlpha > 0.0) {
        vec2 relOutlineWidth = vec2(1.0) / u_textureSize * u_outlineWidth;
        vec4 neighbor = getGaussianNeighborsSum(relOutlineWidth) * c_outlineOverstate;
        if (neighbor.a > c_alphaLv0) {
            texColor.rgb = u_outlineColor.rgb;
            texColor.a = min(1.0, neighbor.a) * u_outlineColor.a * u_outlineAlpha;
        }
    }
    return texColor;
}

vec4 getSeamed() {
    vec4 texColor = texture2D(u_texture, v_texCoords);
    vec2 relPixelSize = vec2(1.0) / u_textureSize;
    vec4[24] neighbors = getSimpleNeighbors(relPixelSize);
    vec4 sampleColor = vec4(0.0);
    int sampleSize = 0;
    for (int i = 0; i < neighbors.length(); i++) {
        if (neighbors[i].a > c_alphaLv2) {
            sampleColor += neighbors[i];
            sampleSize++;
        }
    }
    if (sampleSize > 0) {
        texColor.rgb = mix(sampleColor.rgb / sampleSize, texColor.rgb, c_seamCoef);
        texColor.a = min(1.0, texColor.a * 2.0);
    } else {
        texColor.a = c_alphaLv2;
    }
    return texColor;
}

vec4 getBoxShadow() {
    if (u_shadowColor.a <= 0.0) {
        return vec4(0.0);
    }
    vec2 relShadowOffset = vec2(c_shadowOffset) / u_textureSize;
    vec4 shadowSum = getGaussianNeighborsSum(relShadowOffset);
    return vec4(u_shadowColor.rgb, u_shadowColor.a * sqrt(shadowSum.a));
}

void main() {
    vec4 texColor = texture2D(u_texture, v_texCoords);

    if (texColor.a < c_alphaLv2) {
        if (texColor.a < c_alphaLv0) {
            // Outline effect apply on transparent areas
            texColor = getOutlined();
        } else if (texColor.a < c_alphaLv1) {
            // No effect apply on these areas
        } else {
            // Seaming apply on gap areas
            texColor = getSeamed();
        }
        // Box shadow
        texColor = mix(getBoxShadow(), texColor, texColor.a);
    } else {
        // No effect apply on other areas
    }

    // Ultimate composing
    gl_FragColor = texColor;
    gl_FragColor.a *= u_alpha;
}
