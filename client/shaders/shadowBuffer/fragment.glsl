#version 330

in vec2 tc;

tex depthMap;

uniform mat4 inverseProjectionMatrix;
uniform mat4 inverseViewMatrix;

out vec4 out_colour;

#include "shadows.glsl"
#include "util.glsl"

void main(void) {
	float shadow = performShadows(getPosition(tc));

	out_colour = vec4(shadow, 1, 1, 1);
}
