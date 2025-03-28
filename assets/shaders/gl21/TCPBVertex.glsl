/** Copyright (c) 2013-2023, Esoteric Software LLC
 * At Spine Runtimes License
 */

// Common Vertex Shader for TwoColorPolygonBatch.

#version 120

attribute vec4 a_position;
attribute vec4 a_light;
attribute vec4 a_dark;
attribute vec2 a_texCoord0;
uniform mat4 u_projTrans;   // From TCPB
varying vec4 v_light;       // Transfer to FS
varying vec4 v_dark;        // Transfer to FS
varying vec2 v_texCoords;   // Transfer to FS

const float c_lightAlphaCoef = 255.0 / 254.0;

void main() {
    v_light = a_light;
    v_light.a = v_light.a * c_lightAlphaCoef;
    v_dark = a_dark;
    v_texCoords = a_texCoord0;
    gl_Position = u_projTrans * a_position;
}
