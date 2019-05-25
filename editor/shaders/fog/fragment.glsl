#version 330

in vec2 tc;

FS_OUT(colour)

tex scene;
tex depthMap;

uniform float density;
uniform float gradient;
uniform vec3 skyColour;

uniform vec3 sunDirection;
uniform vec3 sunColour;

uniform mat4 inverseProjectionMatrix;

#include "util.glsl"

vec4 calculate(vec3 position) {
	float distance = length(position);
	if (distance > 1600)
		return texture2D(scene, tc);

	vec3 direction = position / distance;
	float fogAmount = clamp(exp(-pow((distance * density), gradient)), 0, 1);
	float sunAmount = max(dot(sunDirection, direction), 0);
	vec3 colour = mix(skyColour, sunColour, pow(sunAmount, 8));

	return vec4(mix(colour, texture2D(scene, tc).xyz, fogAmount), 1);
}

void main(void) {
	out_colour = calculate(getPosition(tc));
}
