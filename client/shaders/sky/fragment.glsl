#version 330

in vec3 pos;

FS_OUT(diffuse)
FS_OUT(other)
FS_OUT(bloom)
FS_OUT(godrays)

#include "functions.glsl"
#include "sky.glsl"

void main(void) {
	Colour colour = getSkyColour(normalize(pos));

	out_diffuse = colour.diffuse;
	out_bloom = colour.bloom;
	out_godrays = colour.godrays;
}
