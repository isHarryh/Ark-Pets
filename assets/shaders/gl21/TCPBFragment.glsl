/** Copyright (c) 2013-2023, Esoteric Software LLC
 * At Spine Runtimes License
 */

// Common Fragment Shader for TwoColorPolygonBatch.

#version 120

#ifdef GL_ES
    #define LOWP lowp
    precision mediump float;
#else
    #define LOWP
#endif

varying LOWP vec4 v_light;      // From VS
varying LOWP vec4 v_dark;       // From VS
varying vec2 v_texCoords;       // From VS
uniform float u_pma;            // From TCPB
uniform sampler2D u_texture;    // From TCPB

void main() {
    vec4 texColor = texture2D(u_texture, v_texCoords);
    gl_FragColor.a = texColor.a * v_light.a;
    gl_FragColor.rgb = ((texColor.a - 1.0) * u_pma + 1.0 - texColor.rgb) * v_dark.rgb + texColor.rgb * v_light.rgb;
}
