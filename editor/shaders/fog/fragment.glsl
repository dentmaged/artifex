#version 330

in vec2 tc;

FS_OUT(colour)

tex scene;
tex depthMap;

uniform float density;
uniform float gradient;
uniform vec3 skyColour;

uniform mat4 inverseProjectionMatrix;

#include "util.glsl"

void main(void) {
	float distance = length(getPosition(tc));
	if (distance > 1600)
		out_colour = texture2D(scene, tc);
	else
		out_colour = vec4(mix(skyColour, texture2D(scene, tc).xyz, clamp(exp(-pow((distance * density), gradient)), 0, 1)), 1);
}
