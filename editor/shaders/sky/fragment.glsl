#version 330

in vec3 pos;

FS_OUT(diffuse)
FS_OUT(other)
FS_OUT(albedo)

#include "functions.glsl"
#include "sky.glsl"

void main(void) {
	Colour colour = getSkyColour(normalize(pos));

	out_diffuse = colour.diffuse;
	out_diffuse.xyz *= 3;
	out_other = vec4(0, 0, colour.emissive, 0);
	out_albedo = colour.diffuse;
}
