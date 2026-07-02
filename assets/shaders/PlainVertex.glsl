#version 300 es
/** Copyright (c) 2013-2023, Esoteric Software LLC
 * At Spine Runtimes License
 */

// Plain vertex shader for TwoColorPolygonBatch.

in vec4 a_position;
in vec4 a_light;
in vec4 a_dark;
in vec2 a_texCoord0;
uniform mat4 u_projTrans;   // From TCPB

out vec4 v_light;           // Transfer to FS
out vec4 v_dark;            // Transfer to FS
out vec2 v_texCoords;       // Transfer to FS

const float c_lightAlphaCoef = 255.0 / 254.0;

void main() {
    v_light = a_light;
    v_light.a = v_light.a * c_lightAlphaCoef;
    v_dark = a_dark;
    v_texCoords = a_texCoord0;
    gl_Position = u_projTrans * a_position;
}
