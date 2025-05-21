#version 300 es
/** Copyright (c) 2013-2023, Esoteric Software LLC
 * At Spine Runtimes License
 */

// Plain fragment shader for TwoColorPolygonBatch.

#ifdef GL_ES
    #define LOWP lowp
    precision mediump float;
#else
    #define LOWP
#endif

in LOWP vec4 v_light;           // From VS
in LOWP vec4 v_dark;            // From VS
in vec2 v_texCoords;            // From VS
uniform float u_pma;            // From TCPB
uniform sampler2D u_texture;    // From TCPB
uniform float u_alpha;          // Required

out vec4 fragColor;

void main() {
    vec4 texColor = texture(u_texture, v_texCoords);
    fragColor.a = texColor.a * v_light.a * u_alpha;
    fragColor.rgb = ((texColor.a - 1.0) * u_pma + 1.0 - texColor.rgb) * v_dark.rgb + texColor.rgb * v_light.rgb;
}
